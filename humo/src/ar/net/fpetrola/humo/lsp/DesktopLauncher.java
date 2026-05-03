package ar.net.fpetrola.humo.lsp;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("ZX Spectrum 3D");
        cfg.setWindowedMode(1280, 800);
        cfg.useVsync(true);
        cfg.setForegroundFPS(60);
        new Lwjgl3Application(new ar.net.fpetrola.humo.lsp.Spectrum3DScreen(), cfg);
    }
}