package ar.net.fpetrola.humo.lsp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class OrbitCameraController extends InputAdapter {
    private final PerspectiveCamera camera;
    private final Vector3 target;

    private float distance = 300f;
    private float azimuth   = 0f;   // grados, alrededor de Y
    private float elevation = 15f;  // grados, sobre el plano XZ

    private static final float MIN_DIST = 10f;
    private static final float MAX_DIST = 2000f;
    private static final float ROT_SPEED = 0.4f;
    private static final float ZOOM_FACTOR = 1.12f;

    private int lastX, lastY;
    private boolean dragging = false;

    public OrbitCameraController(PerspectiveCamera camera, Vector3 target) {
        this.camera = camera;
        this.target = new Vector3(target);
        apply();
    }

    public void setDistance(float d) {
        distance = MathUtils.clamp(d, MIN_DIST, MAX_DIST);
        apply();
    }

    public void setTarget(Vector3 t) {
        target.set(t);
        apply();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCtrl()) {
            lastX = screenX;
            lastY = screenY;
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        dragging = false;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!dragging || !isCtrl()) { dragging = false; return false; }
        int dx = screenX - lastX;
        int dy = screenY - lastY;
        lastX = screenX;
        lastY = screenY;

        azimuth   -= dx * ROT_SPEED;
        elevation += dy * ROT_SPEED;
        elevation = MathUtils.clamp(elevation, -89f, 89f);
        apply();
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // amountY > 0 = rueda hacia atrás = zoom out
        if (amountY > 0) distance *= ZOOM_FACTOR;
        else distance /= ZOOM_FACTOR;
        distance = MathUtils.clamp(distance, MIN_DIST, MAX_DIST);
        apply();
        return true;
    }

    public void update() { /* slot para inercia si querés */ }

    private void apply() {
        float az = azimuth * MathUtils.degreesToRadians;
        float el = elevation * MathUtils.degreesToRadians;
        float cosEl = MathUtils.cos(el);
        float x = distance * cosEl * MathUtils.sin(az);
        float y = distance * MathUtils.sin(el);
        float z = distance * cosEl * MathUtils.cos(az);

        camera.position.set(target.x + x, target.y + y, target.z + z);
        camera.up.set(0, 1, 0);
        camera.lookAt(target);
        camera.update();
    }

    private boolean isCtrl() {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }
}