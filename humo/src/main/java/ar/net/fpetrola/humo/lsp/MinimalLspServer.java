package ar.net.fpetrola.humo.lsp;

/*
 * LSP server for Humo with a bridge port for DAP communication.
 *
 * The DAP adapter (separate source process) connects to this server on a TCP port
 * and sends the updated source content. This server then uses workspace/applyEdit
 * to push the change into IntelliJ's editor in real-time.
 *
 * Bridge protocol (TCP, line-based):
 * DAP sends: "CONTENT <uri> <length>\n<content bytes>"
 * LSP replies: "OK\n" or "FAIL <reason>\n"
 */

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinimalLspServer implements LanguageServer, LanguageClientAware {
  private static final Logger LOG = Logger.getLogger(MinimalLspServer.class.getName());
  static final int BRIDGE_PORT = 19877;

  private LanguageClient client;
  private final HumoTextDocumentService textDocService;
  private final HumoWorkspaceService wsService;
  private int errorCode = 1;
  private ServerSocket bridgeServer;
  private String originUri;
  protected static String content;

  public MinimalLspServer() {
    this.textDocService = new HumoTextDocumentService();
    this.wsService = new HumoWorkspaceService();
  }

  // LSP lifecycle
  // =====================

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    LOG.info("Humo LSP server starting");
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setHoverProvider(true);

    // Start the bridge listener for DAP communication
    startBridge();

    // Configuración básica que ya tenés (ejemplo)
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental); // o Full
    // ... otras capacidades como completionProvider, hoverProvider, etc.

    // Activar foldingRange
    FoldingRangeProviderOptions foldingOptions = new FoldingRangeProviderOptions();
    // Opcional: límites que el cliente puede respetar
    foldingOptions.setWorkDoneProgress(true);           // si querés progress
//    foldingOptions.setRangeLimit(500);                  // máx ranges por doc (sugerencia al cliente)
//    foldingOptions.setLineFoldingOnly(false);           // permite folding por character, no solo línea

    capabilities.setFoldingRangeProvider(foldingOptions);

    // Alternativa más simple si no necesitás opciones extras:
    // capabilities.setFoldingRangeProvider(Boolean.TRUE);

    SemanticTokensWithRegistrationOptions semanticOptions = new SemanticTokensWithRegistrationOptions();
    semanticOptions.setFull(true);
    semanticOptions.setRange(false); // opcional, si solo usás full

    SemanticTokensLegend legend = new SemanticTokensLegend();
    legend.setTokenTypes(List.of(
        "namespace", "type", "class", "enum", "interface", "struct", "typeParameter",
        "parameter", "variable", "property", "enumMember", "event", "function",
        "method", "macro", "keyword", "modifier", "comment", "string",
        // Tus custom
        "activeExpansion",      // la zona actual (brillante)
        "inactiveExpansion"     // gris / dimmed
    ));
    legend.setTokenModifiers(List.of("declaration", "definition", "readonly", "deprecated"));

    semanticOptions.setLegend(legend);
    capabilities.setSemanticTokensProvider(semanticOptions);

    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    stopBridge();
    errorCode = 0;
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
    System.exit(errorCode);
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return textDocService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return wsService;
  }

  @Override
  public void connect(LanguageClient c) {
    this.client = c;
  }

  // Bridge: TCP server that DAP adapter connects to
  //
  private void startBridge() {

    LspBridgeClient client = new LspBridgeClient();

    client.registerHandler(LspBridgeClient.ContentCommand.class, (payload, reply) -> {
      content = payload.content;
      String uri = payload.uri;
      LOG.info("[bridge] Received CONTENT for " + uri + " (" + payload.getLength() + " bytes)");
      applyContentToEditor(uri, content);
      applyContentToEditor(originUri, content);
      reply.ok();
    });

    client.registerHandler(LspBridgeClient.DiagnosticsCommand.class, (payload, reply) -> {
      String uri = payload.uri;
      String diagJson = payload.diagnostics;
      LOG.info("[bridge] Received DIAG for " + uri);
      applyDiagnostics(uri, diagJson);
      reply.ok();
    });

    client.registerHandler(LspBridgeClient.OpenOriginDocument.class, (payload, reply) -> {
      originUri = payload.uri;
      LOG.info("[bridge] Received OpenOriginDocument for " + originUri);
      openOriginDocument(originUri);
      reply.ok();
    });


    Thread bridgeThread = new Thread(() -> {
      try {
        bridgeServer = new ServerSocket(); // Create an unbound socket
        bridgeServer.setReuseAddress(true); // Enable address reuse
        bridgeServer.bind(new InetSocketAddress(BRIDGE_PORT)); // Bind to the desired port


//        bridgeServer = new ServerSocket(BRIDGE_PORT);
        LOG.info("[bridge] Listening on port " + BRIDGE_PORT);
        while (!bridgeServer.isClosed()) {
          Socket sock = bridgeServer.accept();
          LOG.info("[bridge] DAP client connected");

          client.connect(sock);
          client.startReadingThread();
//          handleBridgeClient(sock);
        }
      } catch (IOException e) {
        e.printStackTrace();
        if (!e.getMessage().contains("closed")) {
          LOG.warning("[bridge] Error: " + e.getMessage());
          LOG.warning("[bridge] Port: " + BRIDGE_PORT);
        }
      }
    }, "LSP-Bridge");
    bridgeThread.setDaemon(true);
    bridgeThread.start();
  }

  private void stopBridge() {
    try {
      if (bridgeServer != null) bridgeServer.close();
    } catch (IOException ignored) {
    }
  }

  /**
   * Apply content change to the editor via workspace/applyEdit.
   * Called from the bridge when DAP sends new content.
   */
  private void applyContentToEditor(String uri, String newContent) {
    if (client == null) {
      LOG.warning("[bridge] No LSP client connected");
      return;
    }

    // Get old content to compute end range
    String old = textDocService.documents.getOrDefault(uri, "");
    int endLine = 0, endCol = 0;
    for (int i = 0; i < old.length(); i++) {
      if (old.charAt(i) == '\n') {
        endLine++;
        endCol = 0;
      } else endCol++;
    }

    TextEdit replace = new TextEdit(
        new Range(new Position(0, 0), new Position(endLine, endCol)),
        newContent
    );

    WorkspaceEdit wsEdit = new WorkspaceEdit();
    Map<String, List<TextEdit>> changes = new HashMap<>();
    changes.put(uri, Collections.singletonList(replace));
    wsEdit.setChanges(changes);

    try {
      ApplyWorkspaceEditResponse resp = client.applyEdit(
          new ApplyWorkspaceEditParams(wsEdit)
      ).get(5, TimeUnit.SECONDS);

      if (resp != null && resp.isApplied()) {
        LOG.info("[bridge] applyEdit OK");
        textDocService.documents.put(uri, newContent);
      } else {
        LOG.warning("[bridge] applyEdit NOT applied");
      }
    } catch (Exception e) {
      LOG.warning("[bridge] applyEdit error: " + e.getMessage());
    }
  }

  private void applyDiagnostics(String uri, String diagJson) {
    if (client == null) return;
    // Parse simple diagnostic format: "severity|startOff|endOff|message"
    // Multiple diags separated by "|"
    List<Diagnostic> diags = new ArrayList<>();
    String content = textDocService.documents.getOrDefault(uri, "");

    for (String part : diagJson.split("\\|")) {
      String[] fields = part.split("\\|", 4);
      if (fields.length < 4) continue;
      try {
        DiagnosticSeverity sev = DiagnosticSeverity.forValue(Integer.parseInt(fields[0]));
        int startOff = Integer.parseInt(fields[1]);
        int endOff = Integer.parseInt(fields[2]);
        String msg = fields[3];
        Position s = offsetToPos(content, Math.min(startOff, content.length()));
        Position e = offsetToPos(content, Math.min(endOff, content.length()));
        diags.add(new Diagnostic(new Range(s, e), msg, sev, "humo-debug"));
      } catch (Exception ignored) {
      }
    }

    client.publishDiagnostics(new PublishDiagnosticsParams(uri, diags));
  }

  static Position offsetToPos(String text, int offset) {
    int line = 0, col = 0;
    for (int i = 0; i < offset && i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        line++;
        col = 0;
      } else col++;
    }
    return new Position(line, col);
  }

  public void openOriginDocument(String originUri) {
    // originUri ejemplo: "file:///tmp/humo-origen-view.humo" o "humo://runtime/origin.humo"

    ShowDocumentParams params = new ShowDocumentParams();
    params.setUri(originUri);
    params.setExternal(false);      // importante para que no reemplace la pestaña actual
    params.setTakeFocus(false);    // no robar foco del documento principal

    // Opcional: selección inicial
    // params.setSelection(new Range(new Position(0,0), new Position(0,0)));

    client.showDocument(params).thenAccept(result -> {
      if (result.isSuccess()) {
        System.out.println("Documento origen abierto: " + originUri);
      } else {
        System.err.println("Fallo al abrir documento origen");
      }
    });
  }
  // Main
  // =====================

  public static void main(String[] args) {
    Logger root = Logger.getLogger("");
    for (Handler h : root.getHandlers()) root.removeHandler(h);
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel(Level.ALL);
    root.addHandler(ch);
    root.setLevel(Level.INFO);

    MinimalLspServer server = new MinimalLspServer();
    Launcher<LanguageClient> launcher = new Launcher.Builder<LanguageClient>()
        .setLocalService(server).setRemoteInterface(LanguageClient.class)
        .setInput(System.in).setOutput(System.out).create();
    server.connect(launcher.getRemoteProxy());
    launcher.startListening();
  }

  // TextDocumentService
  // =====================

  static class HumoTextDocumentService implements TextDocumentService {
    final Map<String, String> documents = new HashMap<>();

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
      String uri = params.getTextDocument().getUri();

      if (!isOriginUri(uri)) {
        // Para el documento principal podés devolver semantic tokens normales o vacíos
        return CompletableFuture.completedFuture(new SemanticTokens(List.of()));
      }

      // Aquí tenés que tokenizar TODO el documento cada vez (o usar delta si implementás semanticTokensDelta)
      // Esto es costoso si el archivo es grande → considera cachear y actualizar deltas

      ExecutionContext ctx = executionContext; // tu estado
      Range activeRange = ctx.getCurrentActiveRange();     // la variable que se está expandiendo
      int activeStartLine = activeRange.getStart().getLine();
      int activeEndLine = activeRange.getEnd().getLine();

      List<Integer> data = new ArrayList<>(); // el array plano de semantic tokens

      // Suponiendo que tenés el texto completo del documento origen
      String fullText = content; // implementá esto (cache o lsp4j document manager)
      List<String> lines = List.of(fullText.split("\n"));

      int currentLine = 0;
      int currentChar = 0;
      int prevLine = 0;
      int prevStartChar = 0;

      for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
        String lineText = lines.get(lineIdx);
        int lineLength = lineText.length();

        // Decidir token type según posición relativa a activeRange
        int tokenTypeIdx;
        if (lineIdx >= activeStartLine && lineIdx <= activeEndLine) {
          tokenTypeIdx = legendTokenTypeIndex("activeExpansion"); // brillante, normal o custom
        } else {
          tokenTypeIdx = legendTokenTypeIndex("inactiveExpansion"); // gris
        }

        // Token simple: toda la línea con el mismo tipo (para simplicidad y velocidad)
        // Podés refinarlo por palabra o por caracteres si querés más precisión

        if (lineLength > 0) {
          // delta encoding
          int deltaLine = lineIdx - prevLine;
          int deltaStart = (deltaLine == 0) ? currentChar - prevStartChar : currentChar;

          data.add(deltaLine);
          data.add(deltaStart);
          data.add(lineLength);           // longitud del token
          data.add(tokenTypeIdx);         // tipo
          data.add(0);                    // modifiers (0 = ninguno)

          prevLine = lineIdx;
          prevStartChar = currentChar;
        }

        currentChar = 0; // reset para próxima línea
      }

      return CompletableFuture.completedFuture(new SemanticTokens(data));
    }

    private int legendTokenTypeIndex(String typeName) {
      // Cacheá esto o usa un Map<String, Integer>
      // El índice es la posición en la lista de tokenTypes que declaraste en initialize
      return switch (typeName) {
        case "activeExpansion" -> 16;   // ajustá según tu lista
        case "inactiveExpansion" -> 17;
        default -> 0; // fallback
      };
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams p) {
      documents.put(p.getTextDocument().getUri(), p.getTextDocument().getText());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams p) {
      documents.put(p.getTextDocument().getUri(), p.getContentChanges().get(0).getText());
    }

    @Override
    public void didClose(DidCloseTextDocumentParams p) {
      documents.remove(p.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams p) {
    }

    private ExecutionContext executionContext = new ExecutionContext(); // tu estado con:

    @Override
    public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
      LOG.info("RANGES: " + params);
      String uri = params.getTextDocument().getUri();

      // Solo aplicamos folding especial en el documento origen
      if (!isOriginUri(uri)) {
        // Para el documento principal podés devolver folding estático o vacío
        return CompletableFuture.completedFuture(Collections.emptyList());
      }

      List<FoldingRange> ranges = new ArrayList<>();

      // 1. Colapsar bloques grandes lejanos (opcional)
      addFarAwayContextFolds(ranges);

      // 2. Variables inmediatamente anteriores (últimas 2–3)
      List<VariableInfo> prev = executionContext.getPreviousVars();
      for (int i = Math.max(0, prev.size() - 3); i < prev.size(); i++) {
        VariableInfo v = prev.get(i);
        FoldingRange fr = createFoldForVar(v, "prev: ");
        if (fr != null) ranges.add(fr);
      }

      // 3. NO colapsar la variable actual → queda visible

      // 4. Variables inmediatamente posteriores (próximas 1–3)
      List<VariableInfo> next = executionContext.getNextVars();
      for (int i = 0; i < Math.min(3, next.size()); i++) {
        VariableInfo v = next.get(i);
        FoldingRange fr = createFoldForVar(v, "next: ");
        if (fr != null) ranges.add(fr);
      }

      // 5. Colapsar niveles "estériles" o wrappers profundos (opcional)
      addSterileBlockFolds(ranges);
      LOG.info("RANGES: " + ranges);

      return CompletableFuture.completedFuture(ranges);
    }

    private void addSterileBlockFolds(List<FoldingRange> ranges) {

    }

    private boolean isOriginUri(String uri) {
      return uri.contains("origin");
    }

    private FoldingRange createFoldForVar(VariableInfo v, String prefix) {
      if (v.getRange() == null) return null;

      Position start = v.getRange().getStart();
      Position end = v.getRange().getEnd();

      FoldingRange fr = new FoldingRange();
      fr.setStartLine(start.getLine());
      fr.setStartCharacter(start.getCharacter());
      fr.setEndLine(end.getLine());
      fr.setEndCharacter(end.getCharacter());
      fr.setKind(FoldingRangeKind.Region);

      // Resumen corto – ajustá según lo que tengas en summary
      String collapsed = prefix + v.getName();
      if (v.getSummary() != null && !v.getSummary().isEmpty()) {
        collapsed += " = " + shorten(v.getSummary(), 40);
      }
      fr.setCollapsedText(collapsed);

      return fr;
    }

    private String shorten(String s, int max) {
      if (s.length() <= max) return s;
      return s.substring(0, max - 3) + "...";
    }

    // Ejemplo de colapsar bloques lejanos o estériles
    private void addFarAwayContextFolds(List<FoldingRange> ranges) {
      // Lógica según tu estructura: colapsar desde línea 0 hasta antes de prevs
      // o desde después de nexts hasta el final
      // ...
    }
  }

  // WorkspaceService
  // =====================

  static class HumoWorkspaceService implements WorkspaceService {
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams p) {
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams p) {
    }
  }
}
  