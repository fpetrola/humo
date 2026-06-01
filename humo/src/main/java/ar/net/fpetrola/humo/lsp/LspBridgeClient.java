package ar.net.fpetrola.humo.lsp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class LspBridgeClient implements AutoCloseable {

  private static final Logger LOG = Logger.getLogger(LspBridgeClient.class.getName());

  private Socket socket;
  private DataOutputStream out;
  private DataInputStream in;
  private volatile boolean connected = false;
  private volatile boolean running = false;

  // Registro de handlers: comando → (payload, replyWriter)
//  private final Map<String, BiConsumer<Object, ReplyWriter>> commandHandlers = new ConcurrentHashMap<>();
  // Cambiamos el mapa
  private final Map<Class<? extends Command>, BiConsumer<Command, ReplyWriter>> commandHandlers
      = new ConcurrentHashMap<>();

  public LspBridgeClient() {
    // Registrar handlers por defecto (puedes sobreescribirlos después)
//    registerDefaultHandlers();
  }

  // -------------------------------------------------------------------------
  //  Comandos como clases internas (van a ser serializadas con Gson)
  // -------------------------------------------------------------------------

  public static abstract class Command {
    @SerializedName("cmd")
    public final String commandName;

    protected Command(String commandName) {
      this.commandName = commandName;
    }
  }

  public static class ContentCommand extends Command {
    public String uri;
    public String content;

    public ContentCommand() {
      super("CONTENT");
    }

    public ContentCommand(String uri, String content) {
      super("CONTENT");
      this.uri = uri;
      this.content = content;
    }

    int getLength() {
      return content.getBytes(StandardCharsets.UTF_8).length;
    }
  }

  public static class DiagnosticsCommand extends Command {
    public String uri;
    public String diagnostics;  // puede ser JSON string, array, etc.

    public DiagnosticsCommand() {
      super("DIAG");
    }

    public DiagnosticsCommand(String uri, String diagnostics) {
      super("DIAG");
      this.uri = uri;
      this.diagnostics = diagnostics;
    }
  }

  public static class OpenOriginDocument extends Command {
    public String uri;

    public OpenOriginDocument() {
      super("OOD");
    }

    public OpenOriginDocument(String uri) {
      super("OOD");
      this.uri = uri;
    }
  }

  // Puedes agregar más comandos aquí en el futuro...
  // public static class HoverCommand extends Command { ... }

  // -------------------------------------------------------------------------
  //  Interfaz para responder desde los handlers
  // -------------------------------------------------------------------------
  @FunctionalInterface
  public interface ReplyWriter {
    void reply(String response);

    default void ok() {
      reply("OK");
    }

    default void fail(String reason) {
      reply("FAIL " + reason);
    }
  }


  // Registro tipado
  public <T extends Command> void registerHandler(
      Class<T> commandType,
      BiConsumer<T, ReplyWriter> handler) {
    commandHandlers.put(commandType, (cmd, reply) -> {
      @SuppressWarnings("unchecked")
      T typedCmd = (T) cmd;
      handler.accept(typedCmd, reply);
    });
    LOG.info("Handler registrado para: " + commandType.getSimpleName());
  }

  //// Ejemplos de uso
  /// @param socket
//client.registerHandler(ContentCommand .class,(cmd,reply)->
//
//  {
//    // cmd ya es ContentCommand, sin casteo
//    applyContentToEditor(cmd.uri, cmd.content);
//    reply.ok();
//  });
//
//client.registerHandler(DiagnosticsCommand .class,(cmd,reply)->
//
//  {
//    applyDiagnostics(cmd.uri, cmd.diagnostics);
//    reply.ok();
//  });

  // -------------------------------------------------------------------------
  //  Registro de handlers
  // -------------------------------------------------------------------------
//  public void registerHandler2(String commandName, BiConsumer<Object, ReplyWriter> handler) {
//    commandHandlers.put(commandName.toUpperCase(), handler);
//    LOG.info("Handler registrado para comando: " + commandName);
//  }

//  private void registerDefaultHandlers() {
//    // Puedes quitar estos si prefieres que el usuario SIEMPRE configure los suyos
//    registerHandler("CONTENT", (payload, reply) -> {
//      if (payload instanceof ContentCommand cmd) {
//        applyContentToEditor(cmd.uri, cmd.content);
//        try {
//          reply.ok();
//        } catch (IOException ignored) {
//        }
//      }
//    });
//
//    registerHandler("DIAG", (payload, reply) -> {
//      if (payload instanceof DiagnosticsCommand cmd) {
//        applyDiagnostics(cmd.uri, cmd.diagnostics);
//        try {
//          reply.ok();
//        } catch (IOException ignored) {
//        }
//      }
//    });
//  }

  // -------------------------------------------------------------------------
  //  Métodos públicos para enviar comandos (lado cliente)
  // -------------------------------------------------------------------------
  public boolean connect(Socket socket) {
    try {
      this.socket = socket;
      out = new DataOutputStream(this.socket.getOutputStream());
      in = new DataInputStream(this.socket.getInputStream());
      connected = true;
      LOG.info("[bridge-client] Conectado al puerto " + MinimalLspServer.BRIDGE_PORT);
      return true;
    } catch (IOException e) {
      LOG.warning("[bridge-client] Falló conexión: " + e.getMessage());
      connected = false;
      return false;
    }
  }

  public boolean isConnected() {
    return connected && !socket.isClosed();
  }

  public void sendContent(String uri, String content) {
    sendCommand(new ContentCommand(uri, content));
  }

  public void sendDiagnostics(String uri, String diagData) {
    sendCommand(new DiagnosticsCommand(uri, diagData));
  }

  public void sendCommand(Command cmd) {
    if (!isConnected()) {
      LOG.warning("No conectado - no se envía: " + cmd.commandName);
      return;
    }
    try {
      String json = getGson().toJson(cmd);
      LOG.warning("envía: " + json);

      byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      out.writeInt(bytes.length);
      out.write(bytes);
      out.flush();
    } catch (IOException e) {
      LOG.warning("Error enviando comando " + cmd.commandName + ": " + e.getMessage());
      connected = false;
    }
  }

  private Gson getGson() {
    RuntimeTypeAdapterFactory<Command> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
        .of(Command.class, "cmd")
        .registerSubtype(DiagnosticsCommand.class, "DIAG")
        .registerSubtype(ContentCommand.class, "CONTENT")
        .registerSubtype(OpenOriginDocument.class, "OOD");

    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
        .create();
    return gson;
  }

  // -------------------------------------------------------------------------
  //  Hilo de lectura (lado servidor / bridge)
  // -------------------------------------------------------------------------
  public void startReadingThread() {
    if (running) return;
    running = true;

    Thread reader = new Thread(() -> {
      try {
        while (running && !socket.isClosed()) {
          int length = in.readInt();
          if (length < 0) break;

          byte[] buffer = new byte[length];
          in.readFully(buffer);
          String json = new String(buffer, StandardCharsets.UTF_8);

          LOG.info(json);
          Command cmd = getGson().fromJson(json, Command.class);
          if (cmd == null || cmd.commandName == null) {
            writeResponse("FAIL invalid command format");
            continue;
          }

          String cmdName = cmd.commandName.toUpperCase();
          BiConsumer<Command, ReplyWriter> handler = commandHandlers.get(cmd.getClass());

          if (handler != null) {
            ReplyWriter reply = response -> writeResponse(response);
            handler.accept(cmd, reply);
          } else {
            writeResponse("FAIL unknown command: " + cmdName);
          }
        }
      } catch (EOFException eof) {
        LOG.info("[bridge-client] Conexión cerrada por el otro extremo");
      } catch (Exception e) {
        LOG.warning("[bridge-client] Error en lectura: " + e.getMessage());
      } finally {
        running = false;
        connected = false;
      }
    }, "LspBridge-Reader");

    reader.setDaemon(true);
    reader.start();
  }

  private void writeResponse(String msg) {
    try {
      byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
      out.writeInt(bytes.length);
      out.write(bytes);
      out.flush();
    } catch (IOException e) {
      LOG.warning("Error enviando respuesta: " + e.getMessage());
      connected = false;
    }
  }

  // -------------------------------------------------------------------------
  //  Métodos de aplicación (deberían venir de afuera o ser sobrescritos)
  // -------------------------------------------------------------------------
  protected void applyContentToEditor(String uri, String content) {
    LOG.info("[bridge] Contenido recibido para " + uri + " (" + content.length() + " chars)");
    // Implementar o sobreescribir
  }

  protected void applyDiagnostics(String uri, String diagData) {
    LOG.info("[bridge] Diagnósticos recibidos para " + uri);
    // Implementar o sobreescribir
  }

  @Override
  public void close() {
    running = false;
    connected = false;
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException ignored) {
    }
  }
}