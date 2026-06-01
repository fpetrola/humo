package ar.net.fpetrola.humo.lsp;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3GL20Factory;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Spectrum3DPanel extends JPanel {
    private AWTGLCanvas glCanvas;
    private final ApplicationListener listener;
    private boolean created = false;
    private long lastFrameTime;
    private float deltaTime;
    private final boolean[] keys = new boolean[256];
    private final boolean[] justPressed = new boolean[256];
    private InputProcessor inputProcessor;
    private volatile boolean running = true;

    public Spectrum3DPanel(ApplicationListener listener) {
        this.listener = listener;
        new SharedLibraryLoader().load("gdx");
        setLayout(new BorderLayout());
        initCanvas();
    }

    private void initCanvas() {
        GLData data = new GLData();
        data.majorVersion = 3;
        data.minorVersion = 2;
        data.profile = GLData.Profile.COMPATIBILITY;
        data.samples = 4;

        glCanvas = new AWTGLCanvas(data) {
            @Override
            public void initGL() {
                GL.createCapabilities();
                setupGdx();
                listener.create();
                listener.resize(getWidth(), getHeight());
                created = true;
                lastFrameTime = System.nanoTime();
            }

            @Override
            public void paintGL() {
                long now = System.nanoTime();
                deltaTime = (now - lastFrameTime) / 1_000_000_000f;
                lastFrameTime = now;
                listener.render();
                swapBuffers();
                java.util.Arrays.fill(justPressed, false);
            }
        };

        glCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = mapKeyCode(e.getKeyCode());
                if (code >= 0 && code < keys.length) {
                    if (!keys[code]) justPressed[code] = true;
                    keys[code] = true;
                }
                if (inputProcessor != null) inputProcessor.keyDown(code);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = mapKeyCode(e.getKeyCode());
                if (code >= 0 && code < keys.length) keys[code] = false;
                if (inputProcessor != null) inputProcessor.keyUp(code);
            }
        });

        glCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                glCanvas.requestFocusInWindow();
                if (inputProcessor != null)
                    inputProcessor.touchDown(e.getX(), e.getY(), 0, mapButton(e.getButton()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (inputProcessor != null)
                    inputProcessor.touchUp(e.getX(), e.getY(), 0, mapButton(e.getButton()));
            }
        });

        glCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (inputProcessor != null)
                    inputProcessor.touchDragged(e.getX(), e.getY(), 0);
            }
        });

        glCanvas.addMouseWheelListener(e -> {
            if (inputProcessor != null)
                inputProcessor.scrolled(0, (float) e.getPreciseWheelRotation());
        });

        glCanvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (created) listener.resize(glCanvas.getWidth(), glCanvas.getHeight());
            }
        });

        add(glCanvas, BorderLayout.CENTER);
        startRenderLoop();
    }

    private void startRenderLoop() {
        Thread renderThread = new Thread(() -> {
            while (running) {
                if (glCanvas.isValid()) {
                    glCanvas.render();
                }
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignored) {}
            }
        }, "GL-Render");
        renderThread.setDaemon(true);
        renderThread.start();
    }

    public void dispose() {
        running = false;
        if (created) listener.dispose();
    }

    private void setupGdx() {
        GL20 gl20 = Lwjgl3GL20Factory.create();
        Gdx.gl = gl20;
        Gdx.gl20 = gl20;
        Gdx.app = new PanelApp();
        Gdx.graphics = new PanelGraphics();
        Gdx.input = new PanelInput();
        Gdx.files = new Lwjgl3Files();
    }

    private int mapButton(int awtButton) {
        return switch (awtButton) {
            case MouseEvent.BUTTON1 -> Input.Buttons.LEFT;
            case MouseEvent.BUTTON2 -> Input.Buttons.MIDDLE;
            case MouseEvent.BUTTON3 -> Input.Buttons.RIGHT;
            default -> Input.Buttons.LEFT;
        };
    }

    private int mapKeyCode(int awtKey) {
        return switch (awtKey) {
            case KeyEvent.VK_LEFT -> Input.Keys.LEFT;
            case KeyEvent.VK_RIGHT -> Input.Keys.RIGHT;
            case KeyEvent.VK_UP -> Input.Keys.UP;
            case KeyEvent.VK_DOWN -> Input.Keys.DOWN;
            case KeyEvent.VK_SPACE -> Input.Keys.SPACE;
            case KeyEvent.VK_X -> Input.Keys.X;
            case KeyEvent.VK_Z -> Input.Keys.Z;
            case KeyEvent.VK_CONTROL -> Input.Keys.CONTROL_LEFT;
            default -> awtKey;
        };
    }

    private class PanelApp implements Application {
        public int getVersion() { return 0; }
        public ApplicationType getType() { return ApplicationType.Desktop; }
        public com.badlogic.gdx.Graphics getGraphics() { return Gdx.graphics; }
        public Audio getAudio() { return null; }
        public Input getInput() { return Gdx.input; }
        public Files getFiles() { return null; }
        public Net getNet() { return null; }
        public ApplicationListener getApplicationListener() { return listener; }
        public void log(String tag, String message) { System.out.println(tag + ": " + message); }
        public void log(String tag, String message, Throwable exception) { log(tag, message); }
        public void debug(String tag, String message) {}
        public void debug(String tag, String message, Throwable exception) {}
        public void error(String tag, String message) { System.err.println(tag + ": " + message); }
        public void error(String tag, String message, Throwable exception) { error(tag, message); }
        public int getLogLevel() { return Application.LOG_INFO; }
        public void setLogLevel(int logLevel) {}
        public void setApplicationLogger(ApplicationLogger applicationLogger) {}
        public ApplicationLogger getApplicationLogger() { return null; }
        public com.badlogic.gdx.utils.Clipboard getClipboard() { return null; }
        public void postRunnable(Runnable runnable) { SwingUtilities.invokeLater(runnable); }
        public void exit() {}
        public void addLifecycleListener(LifecycleListener listener) {}
        public void removeLifecycleListener(LifecycleListener listener) {}
        public Preferences getPreferences(String name) { return null; }
        public long getJavaHeap() { return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); }
        public long getNativeHeap() { return getJavaHeap(); }
    }

    private class PanelGraphics implements com.badlogic.gdx.Graphics {
        public boolean isGL30Available() { return false; }
        public boolean isGL31Available() { return false; }
        public boolean isGL32Available() { return false; }
        public GL20 getGL20() { return Gdx.gl20; }
        public com.badlogic.gdx.graphics.GL30 getGL30() { return null; }
        public com.badlogic.gdx.graphics.GL31 getGL31() { return null; }
        public com.badlogic.gdx.graphics.GL32 getGL32() { return null; }
        public void setGL20(GL20 gl20) {}
        public void setGL30(com.badlogic.gdx.graphics.GL30 gl30) {}
        public void setGL31(com.badlogic.gdx.graphics.GL31 gl31) {}
        public void setGL32(com.badlogic.gdx.graphics.GL32 gl32) {}
        public int getWidth() { return glCanvas.getWidth(); }
        public int getHeight() { return glCanvas.getHeight(); }
        public int getBackBufferWidth() { return glCanvas.getWidth(); }
        public int getBackBufferHeight() { return glCanvas.getHeight(); }
        public float getDeltaTime() { return deltaTime; }
        public float getRawDeltaTime() { return deltaTime; }
        public int getFramesPerSecond() { return 60; }
        public com.badlogic.gdx.Graphics.GraphicsType getType() { return com.badlogic.gdx.Graphics.GraphicsType.LWJGL3; }
        public GLVersion getGLVersion() { return new GLVersion(Application.ApplicationType.Desktop, org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_VERSION), org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_VENDOR), org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER)); }
        public float getBackBufferScale() { return 1f; }
        public float getPpiX() { return 96; }
        public float getPpiY() { return 96; }
        public float getPpcX() { return 37.8f; }
        public float getPpcY() { return 37.8f; }
        public float getDensity() { return 1; }
        public boolean supportsDisplayModeChange() { return false; }
        public com.badlogic.gdx.Graphics.DisplayMode[] getDisplayModes() { return new com.badlogic.gdx.Graphics.DisplayMode[0]; }
        public com.badlogic.gdx.Graphics.DisplayMode[] getDisplayModes(com.badlogic.gdx.Graphics.Monitor monitor) { return new com.badlogic.gdx.Graphics.DisplayMode[0]; }
        public com.badlogic.gdx.Graphics.DisplayMode getDisplayMode() { return null; }
        public com.badlogic.gdx.Graphics.DisplayMode getDisplayMode(com.badlogic.gdx.Graphics.Monitor monitor) { return null; }
        public com.badlogic.gdx.Graphics.Monitor getPrimaryMonitor() { return null; }
        public com.badlogic.gdx.Graphics.Monitor getMonitor() { return null; }
        public com.badlogic.gdx.Graphics.Monitor[] getMonitors() { return new com.badlogic.gdx.Graphics.Monitor[0]; }
        public boolean setFullscreenMode(com.badlogic.gdx.Graphics.DisplayMode displayMode) { return false; }
        public boolean setWindowedMode(int width, int height) { return false; }
        public void setTitle(String title) {}
        public void setUndecorated(boolean undecorated) {}
        public void setResizable(boolean resizable) {}
        public void setVSync(boolean vsync) {}
        public void setForegroundFPS(int fps) {}
        public com.badlogic.gdx.Graphics.BufferFormat getBufferFormat() {
            return new com.badlogic.gdx.Graphics.BufferFormat(8, 8, 8, 8, 24, 8, 4, false);
        }
        public boolean supportsExtension(String extension) { return false; }
        public boolean isContinuousRendering() { return true; }
        public void setContinuousRendering(boolean isContinuous) {}
        public void requestRendering() {}
        public boolean isFullscreen() { return false; }
        public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) { return null; }
        public void setCursor(Cursor cursor) {}
        public void setSystemCursor(Cursor.SystemCursor systemCursor) {}
        public long getFrameId() { return 0; }
        public int getSafeInsetLeft() { return 0; }
        public int getSafeInsetTop() { return 0; }
        public int getSafeInsetBottom() { return 0; }
        public int getSafeInsetRight() { return 0; }
    }

    private class PanelInput implements Input {
        public float getAccelerometerX() { return 0; }
        public float getAccelerometerY() { return 0; }
        public float getAccelerometerZ() { return 0; }
        public float getGyroscopeX() { return 0; }
        public float getGyroscopeY() { return 0; }
        public float getGyroscopeZ() { return 0; }
        public int getMaxPointers() { return 1; }
        public int getX() { return 0; }
        public int getX(int pointer) { return 0; }
        public int getDeltaX() { return 0; }
        public int getDeltaX(int pointer) { return 0; }
        public int getY() { return 0; }
        public int getY(int pointer) { return 0; }
        public int getDeltaY() { return 0; }
        public int getDeltaY(int pointer) { return 0; }
        public boolean isTouched() { return false; }
        public boolean justTouched() { return false; }
        public boolean isTouched(int pointer) { return false; }
        public float getPressure() { return 0; }
        public float getPressure(int pointer) { return 0; }
        public boolean isButtonPressed(int button) { return false; }
        public boolean isButtonJustPressed(int button) { return false; }
        public boolean isKeyPressed(int key) {
            return key >= 0 && key < keys.length && keys[key];
        }
        public boolean isKeyJustPressed(int key) {
            return key >= 0 && key < justPressed.length && justPressed[key];
        }
        public void getTextInput(TextInputListener listener, String title, String text, String hint) {}
        public void getTextInput(TextInputListener listener, String title, String text, String hint, OnscreenKeyboardType type) {}
        public void setOnscreenKeyboardVisible(boolean visible) {}
        public void setOnscreenKeyboardVisible(boolean visible, OnscreenKeyboardType type) {}
        public void vibrate(int milliseconds) {}
        public void vibrate(int milliseconds, boolean fallback) {}
        public void vibrate(int milliseconds, int amplitude, boolean fallback) {}
        public void vibrate(VibrationType vibrationType) {}
        public float getAzimuth() { return 0; }
        public float getPitch() { return 0; }
        public float getRoll() { return 0; }
        public void getRotationMatrix(float[] matrix) {}
        public long getCurrentEventTime() { return System.nanoTime(); }
        public void setCatchBackKey(boolean catchBack) {}
        public boolean isCatchBackKey() { return false; }
        public void setCatchMenuKey(boolean catchMenu) {}
        public boolean isCatchMenuKey() { return false; }
        public void setCatchKey(int keycode, boolean catchKey) {}
        public boolean isCatchKey(int keycode) { return false; }
        public void setInputProcessor(InputProcessor processor) { inputProcessor = processor; }
        public InputProcessor getInputProcessor() { return inputProcessor; }
        public boolean isPeripheralAvailable(Peripheral peripheral) { return false; }
        public int getRotation() { return 0; }
        public Orientation getNativeOrientation() { return Orientation.Landscape; }
        public void setCursorCatched(boolean catched) {}
        public boolean isCursorCatched() { return false; }
        public void setCursorPosition(int x, int y) {}
    }
}
