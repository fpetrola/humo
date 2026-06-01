package ar.net.fpetrola.humo.lsp;

import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAP server for Humo.
 * Connects to the LSP server's bridge port to push document updates
 * into IntelliJ's editor via workspace/applyEdit.
 */

public class HumoDebugAdapter implements IDebugProtocolServer {

  private static final Logger LOG = Logger.getLogger(HumoDebugAdapter.class.getName());
  private static final int THREAD_ID = 1;

  private IDebugProtocolClient client;
  private SteppableHumoInterpreter interpreter;
  private Thread interpreterThread;
  private String sourcePath;
  private String sourceUri;  // file:/// URI for LSP bridge
  private volatile String currentSource;
  private int stepCount = 0;
  private volatile boolean launched = false;
  private volatile String lastPhase = "";
  private volatile String lastPattern = "";
  private volatile String lastProduction = "";
  private volatile int lastHighlightStart = 0;
  private volatile int lastHighlightEnd = 0;
  private volatile List<SteppableHumoInterpreter.StackFrame> lastStack = Collections.emptyList();

  // Bridge to LSP server
  private LspBridgeClient bridge;

  // DAP lifecycle
  // =====================

  @Override
  public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
    LOG.info("DAP initialize");
    Capabilities caps = new Capabilities();
    caps.setSupportsConfigurationDoneRequest(true);
    caps.setSupportsTerminateRequest(true);
    caps.setSupportsHitConditionalBreakpoints(false);
    caps.setSupportsConditionalBreakpoints(false);

    CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) {
      }
      if (client != null) client.initialized();
    });

    return CompletableFuture.completedFuture(caps);
  }

  @Override
  public CompletableFuture<Void> launch(Map<String, Object> args) {
    LOG.info("DAP launch: " + args);
    Object prog = args.get("program");
    String program = prog != null ? prog.toString() : null;
    if (program == null || program.isEmpty()) {
      sendOutput("stderr", "Error: 'program' argument required.\n");
      return CompletableFuture.completedFuture(null);
    }

    sourcePath = program;
    sourceUri = Path.of(sourcePath).toUri().toString();

    try {
      currentSource = Files.readString(Path.of(sourcePath));
    } catch (IOException e) {
      sendOutput("stderr", "Error reading: " + sourcePath + "\n");
      return CompletableFuture.completedFuture(null);
    }

    // Connect to LSP server bridge
    bridge = new LspBridgeClient();
    Socket socket1 = null;
    try {
      socket1 = new Socket("127.0.0.1", MinimalLspServer.BRIDGE_PORT);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (bridge.connect(socket1)) {
      sendOutput("console", "Connected to LSP bridge – editor will update in real-time.\n");
    } else {
      sendOutput("console", "WARNING: Could not connect to LSP bridge (port "
                            + MinimalLspServer.BRIDGE_PORT + "). Editor won't update live.\n");
    }

    sendOutput("console", "Loaded " + sourcePath + " (" + currentSource.length() + " chars)\n");
    launched = true;
    String s = ".origin.humo";
    String pathname = sourcePath + s;

    try {
//      File file = new File(pathname);
//      file.createNewFile();
      Files.copy(Path.of(sourcePath), Path.of(pathname), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    LspBridgeClient.OpenOriginDocument cmd = new LspBridgeClient.OpenOriginDocument(sourceUri + s);
    bridge.sendCommand(cmd);

    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
    if (launched) startInterpreter();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
    SetBreakpointsResponse resp = new SetBreakpointsResponse();

    // Clear old breakpoints for this source
    if (interpreter != null) interpreter.clearBreakpoints();

    SourceBreakpoint[] srcBps = args.getBreakpoints();
    if (srcBps == null || srcBps.length == 0) {
      resp.setBreakpoints(new org.eclipse.lsp4j.debug.Breakpoint[0]);
      LOG.info("Breakpoints cleared");
      return CompletableFuture.completedFuture(resp);
    }

    // Convert line numbers to offsets in the current source
    String src = currentSource != null ? currentSource : "";
    int[] lineOffsets = computeLineOffsets(src);

    org.eclipse.lsp4j.debug.Breakpoint[] result = new org.eclipse.lsp4j.debug.Breakpoint[srcBps.length];
    for (int i = 0; i < srcBps.length; i++) {
      int line = srcBps[i].getLine();  // 1-based
      int offset = (line - 1 < lineOffsets.length) ? lineOffsets[line - 1] : src.length();

      org.eclipse.lsp4j.debug.Breakpoint bp = new org.eclipse.lsp4j.debug.Breakpoint();
      bp.setId(i + 1);
      bp.setVerified(true);
      bp.setLine(line);

      if (interpreter != null) {
        SteppableHumoInterpreter.Breakpoint ibp = interpreter.addBreakpoint(offset);
        bp.setId(ibp.id);
        bp.setMessage("Breakpoint set: line " + line + " → offset " + offset);
        LOG.info("Breakpoint set: line " + line + " → offset " + offset);
      }

      Source source = new Source();
      source.setPath(sourcePath);
      bp.setSource(source);
      result[i] = bp;
    }

    resp.setBreakpoints(result);
    return CompletableFuture.completedFuture(resp);
  }

  /**
   * Compute the start offset of each line (0-based line index).
   */
  private static int[] computeLineOffsets(String src) {
    List<Integer> offsets = new ArrayList<>();
    offsets.add(0);  // line 0 starts at offset 0
    for (int i = 0; i < src.length(); i++) {
      if (src.charAt(i) == '\n') {
        offsets.add(i + 1);
      }
    }
    return offsets.stream().mapToInt(Integer::intValue).toArray();
  }

  private void startInterpreter() {
    stepCount = 0;
    interpreter = new SteppableHumoInterpreter();
    StringBuilder sb = new StringBuilder(currentSource);

    interpreter.setListener(new SteppableHumoInterpreter.StepListener() {

      @Override
      public void onScan(String src, int current, int last, List<SteppableHumoInterpreter.StackFrame> stack) {
        currentSource = src;
      }

      @Override
      public void onDefinition(String src, String pattern, String production,
                               int patStart, int braceOpen, int braceClose,
                               List<SteppableHumoInterpreter.StackFrame> stack) {
        currentSource = src;
      }

      @Override
      public void onBeforeSubstitute(String src, String pattern, String production,
                                     int matchStart, int matchEnd,
                                     SteppableHumoInterpreter.DefLocation defLoc, List<SteppableHumoInterpreter.StackFrame> stack) {
        stepCount++;
        lastPhase = "match";
        lastPattern = pattern;
        lastProduction = production;
        lastHighlightStart = matchStart;
        lastHighlightEnd = matchEnd;
        lastStack = new ArrayList<>(stack);
        currentSource = src;

        // Push to editor via LSP bridge
        pushToEditor(src);

        // Send diagnostics: match + definition origin
        StringBuilder diagData = new StringBuilder();
        diagData.append("2|").append(matchStart).append("|").append(matchEnd)
            .append("| MATCH: \"").append(trunc(pattern, 60))
            .append("\" → \"").append(trunc(production, 80))
            .append("\" step=").append(stepCount);
        if (defLoc != null) {
          int defEnd = Math.min(defLoc.braceClose + 1, src.length());
          diagData.append("|3|").append(defLoc.patternStart).append("|").append(defEnd)
              .append("| Origin: \"").append(trunc(pattern, 30))
              .append("\" → \"").append(trunc(defLoc.value, 50)).append("\"");
        }
        pushDiagnostics(diagData.toString());

        sendOutput("console", "▶ Step " + stepCount + " MATCH: \""
                              + trunc(pattern, 60) + "\" → \"" + trunc(production, 80) + "\"\n");

        // Check if any breakpoint was hit
        String reason = "step";
        if (interpreter != null) {
          for (SteppableHumoInterpreter.Breakpoint bp : interpreter.getBreakpoints()) {
            if (bp.hit) {
              reason = "breakpoint";
              bp.hit = false;  // reset for next time
              sendOutput("console", "● Breakpoint hit at offset " + bp.offset + " bp.id "
                                    + bp.id);
              break;
            }
          }
        }
        fireStoppedEvent(reason);
      }

      @Override
      public void onAfterSubstitute(String src, int insertStart, int insertEnd,
                                    List<SteppableHumoInterpreter.StackFrame> stack) {
        stepCount++;
        lastPhase = "replaced";
        lastHighlightStart = insertStart;
        lastHighlightEnd = insertEnd;
        lastStack = new ArrayList<>(stack);
        currentSource = src;

        String inserted = src.substring(insertStart, Math.min(insertEnd, src.length()));
        pushToEditor(src);

        String diagData = "4|" + insertStart + "|" + insertEnd
                          + "| REPLACED: \"" + trunc(inserted, 60) + "\" step=" + stepCount;
        pushDiagnostics(diagData);

        sendOutput("console", "▼ Step " + stepCount + " REPLACED: \""
                              + trunc(inserted, 80) + "\"\n");

        fireStoppedEvent("step");
      }

      @Override
      public void onFinished(String src) {
        currentSource = src;
        pushToEditor(src);
        pushDiagnostics("0|0|0|✔ FINISHED – " + stepCount + " steps. git checkout to restore.");
        sendOutput("console", "✔ Done – " + stepCount + " steps.\n");
        fireTerminatedEvent();
      }
    });

    interpreterThread = new Thread(() -> interpreter.parseFromStart(sb), "Humo-DAP-Interp");
    interpreterThread.setDaemon(true);
    interpreterThread.start();
  }

  private void pushToEditor(String content) {
    // Write to disk always
    try {
      Files.writeString(Path.of(sourcePath), content);
    } catch (IOException ignored) {
    }

    // Push via bridge to LSP server
    if (bridge != null && bridge.isConnected()) {
      bridge.sendContent(sourceUri, content);
    }
  }

  private void pushDiagnostics(String diagData) {
    if (bridge != null && bridge.isConnected()) {
      bridge.sendDiagnostics(sourceUri, diagData);
    }
  }

  // DAP: threads, stack, scopes, variables
  // =====================

  @Override
  public CompletableFuture<ThreadsResponse> threads() {
    ThreadsResponse resp = new ThreadsResponse();
    org.eclipse.lsp4j.debug.Thread t = new org.eclipse.lsp4j.debug.Thread();
    t.setId(THREAD_ID);
    t.setName("Humo Interpreter");
    resp.setThreads(new org.eclipse.lsp4j.debug.Thread[]{t});
    return CompletableFuture.completedFuture(resp);
  }

  @Override
  public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
    StackTraceResponse resp = new StackTraceResponse();
    List<SteppableHumoInterpreter.StackFrame> stack = lastStack;
    org.eclipse.lsp4j.debug.StackFrame[] frames = new org.eclipse.lsp4j.debug.StackFrame[stack.size()];
    for (int i = 0; i < stack.size(); i++) {
      SteppableHumoInterpreter.StackFrame sf = stack.get(i);
      org.eclipse.lsp4j.debug.StackFrame frame = new org.eclipse.lsp4j.debug.StackFrame();
      frame.setId(sf.id);
      frame.setName(sf.pattern.isEmpty() ? "(root)" : sf.pattern + " → " + trunc(sf.production, 60));
      Source source = new Source();
      source.setPath(sourcePath);
      source.setName(Path.of(sourcePath).getFileName().toString());
      frame.setSource(source);
      int[] lc = offsetToLineCol(currentSource, lastHighlightStart);
      frame.setLine(lc[0] + 1);
      frame.setColumn(lc[1] + 1);
      frames[i] = frame;
    }
    resp.setStackFrames(frames);
    resp.setTotalFrames(frames.length);
    return CompletableFuture.completedFuture(resp);
  }

  @Override
  public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
    ScopesResponse resp = new ScopesResponse();
    Scope prodScope = new Scope();
    prodScope.setName("Productions");
    prodScope.setVariablesReference(1000);
    prodScope.setExpensive(false);
    Scope infoScope = new Scope();
    infoScope.setName("Step Info");
    infoScope.setVariablesReference(2000);
    infoScope.setExpensive(false);
    resp.setScopes(new Scope[]{prodScope, infoScope});
    return CompletableFuture.completedFuture(resp);
  }

  @Override
  public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
    VariablesResponse resp = new VariablesResponse();
    int ref = args.getVariablesReference();
    if (ref == 1000 && interpreter != null) {
      Map<String, String> prods = interpreter.getProductions();
      Variable[] vars = new Variable[prods.size()];
      int i = 0;
      for (Map.Entry<String, String> e : new TreeMap<>(prods).entrySet()) {
        Variable v = new Variable();
        v.setName(e.getKey());
        v.setValue(e.getValue());
        v.setVariablesReference(0);
        vars[i++] = v;
      }
      resp.setVariables(vars);
    } else if (ref == 2000) {
      List<Variable> vars = new ArrayList<>();
      vars.add(makeVar("step", String.valueOf(stepCount)));
      vars.add(makeVar("phase", lastPhase));
      vars.add(makeVar("pattern", lastPattern));
      vars.add(makeVar("production", lastProduction));
      vars.add(makeVar("highlight", "(" + lastHighlightStart + ".." + lastHighlightEnd + ")"));
      vars.add(makeVar("depth", String.valueOf(interpreter != null ? interpreter.getCurrentDepth() : 0)));
      if (interpreter != null) {
        List<SteppableHumoInterpreter.Breakpoint> bps = interpreter.getBreakpoints();
        for (SteppableHumoInterpreter.Breakpoint bp : bps) {
          vars.add(makeVar("bp#" + bp.id,
              "offset=" + bp.offset + " (orig=" + bp.originalOffset + ")"
              + (bp.hit ? " HIT" : "")));
        }
      }
      resp.setVariables(vars.toArray(new Variable[0]));
    } else {
      resp.setVariables(new Variable[0]);
    }
    return CompletableFuture.completedFuture(resp);
  }

  // DAP execution control
  // =====================

  @Override
  public CompletableFuture<Void> next(NextArguments args) {
    if (interpreter != null) interpreter.resume();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> stepIn(StepInArguments args) {
    if (interpreter != null) interpreter.resume();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> stepOut(StepOutArguments args) {
    if (interpreter != null) interpreter.stepOut();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<ContinueResponse> continue_(ContinueArguments args) {
    if (interpreter != null) interpreter.runAll();
    ContinueResponse resp = new ContinueResponse();
    resp.setAllThreadsContinued(true);
    return CompletableFuture.completedFuture(resp);
  }

  @Override
  public CompletableFuture<Void> terminate(TerminateArguments args) {
    stopInterpreter();
    fireTerminatedEvent();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> disconnect(DisconnectArguments args) {
    stopInterpreter();
    if (bridge != null) bridge.close();
    return CompletableFuture.completedFuture(null);
  }

  // Helpers
  // =====================

  public void setClient(IDebugProtocolClient client) {
    this.client = client;
  }

  private void stopInterpreter() {
    if (interpreter != null) {
      interpreter.stop();
      interpreter = null;
    }
    if (interpreterThread != null) {
      interpreterThread.interrupt();
      interpreterThread = null;
    }
  }

  private void fireStoppedEvent(String reason) {
    if (client == null) return;
    StoppedEventArguments evt = new StoppedEventArguments();
    evt.setReason(reason);
    evt.setThreadId(THREAD_ID);
    evt.setAllThreadsStopped(true);
    evt.setDescription("Step " + stepCount + " (" + lastPhase + ")");
    client.stopped(evt);
  }

  private void fireTerminatedEvent() {
    if (client == null) return;
    client.terminated(new TerminatedEventArguments());
  }

  private void sendOutput(String category, String text) {
    if (client == null) return;
    OutputEventArguments evt = new OutputEventArguments();
    evt.setCategory(category);
    evt.setOutput(text);
    client.output(evt);
  }

  private static Variable makeVar(String name, String value) {
    Variable v = new Variable();
    v.setName(name);
    v.setValue(value != null ? value : "null");
    v.setVariablesReference(0);
    return v;
  }

  private static int[] offsetToLineCol(String text, int offset) {
    if (text == null) return new int[]{0, 0};
    int line = 0, col = 0;
    for (int i = 0; i < offset && i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        line++;
        col = 0;
      } else col++;
    }
    return new int[]{line, col};
  }

  private static String trunc(String s, int max) {
    if (s == null) return "";
    s = s.replace("\n", " ").replace("\r", " ").replace("\"", "'");
    return s.length() > max ? s.substring(0, max) + "..." : s;
  }

  // Main
  // =====================

  public static void main(String[] args) throws Exception {
    Logger root = Logger.getLogger("");
    for (Handler h : root.getHandlers()) root.removeHandler(h);
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel(Level.ALL);
    root.addHandler(ch);
    root.setLevel(Level.INFO);

    int port = -1;
    for (int i = 0; i < args.length; i++) {
      if ("--port".equals(args[i]) && i + 1 < args.length) {
        port = Integer.parseInt(args[i + 1]);
      }
    }

    if (port > 0) {
      LOG.info("Humo DAP listening on port " + port);
      try (ServerSocket ss = new ServerSocket(port)) {
        Socket sock = ss.accept();
        LOG.info("Client connected");
        startDap(sock.getInputStream(), sock.getOutputStream());
      }
    } else {
      LOG.info("Humo DAP starting in STDIO mode");
      startDap(System.in, System.out);
    }
  }

  private static void startDap(InputStream in, OutputStream out) {
    HumoDebugAdapter adapter = new HumoDebugAdapter();
    PrintWriter trace = new PrintWriter(System.err, true);
    org.eclipse.lsp4j.jsonrpc.Launcher<IDebugProtocolClient> launcher =
        DSPLauncher.createServerLauncher(adapter, in, out, true, trace);
    adapter.setClient(launcher.getRemoteProxy());
    launcher.startListening();
  }

}