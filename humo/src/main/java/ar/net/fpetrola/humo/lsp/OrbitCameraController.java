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
    private static final float PAN_SPEED = 1.5f;

    private int lastX, lastY;
    private int mouseX, mouseY;
    private boolean rotating = false;
    private boolean panning = false;

    public OrbitCameraController(PerspectiveCamera camera, Vector3 target) {
        this.camera = camera;
        this.target = new Vector3(target);
        this.mouseX = 0;
        this.mouseY = 0;
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
        if (button == Input.Buttons.LEFT) {
            lastX = screenX;
            lastY = screenY;
            if (isCtrl()) {
                rotating = true;
            } else {
                panning = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            panning = false;
            rotating = false;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        mouseX = screenX;
        mouseY = screenY;

        int dx = screenX - lastX;
        int dy = screenY - lastY;
        lastX = screenX;
        lastY = screenY;

        if (panning) {
            panCamera(dx, dy);
            return true;
        } else if (rotating) {
            azimuth   -= dx * ROT_SPEED;
            elevation += dy * ROT_SPEED;
            elevation = MathUtils.clamp(elevation, -89f, 89f);
            apply();
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Calcular posición del cursor normalizada (0-1)
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float cursorNormX = (mouseX - width / 2f) / (width / 2f);
        float cursorNormY = (mouseY - height / 2f) / (height / 2f);

        // Cambiar distancia
        float oldDistance = distance;
        if (amountY > 0) distance *= ZOOM_FACTOR;
        else distance /= ZOOM_FACTOR;
        distance = MathUtils.clamp(distance, MIN_DIST, MAX_DIST);

        // Calcular cuánto se movió la distancia
        float distanceDelta = distance - oldDistance;

        // Mover el target hacia el cursor
        // El FOV es 60 grados, así que tan(30°) = 0.577
        float worldScale = 2f * distance * MathUtils.tan(MathUtils.degreesToRadians * 30f);
        float moveX = cursorNormX * worldScale / 2f * (distanceDelta / distance);
        float moveY = -cursorNormY * worldScale / 2f * (distanceDelta / distance);

        target.x += moveX;
        target.y += moveY;

        apply();
        return true;
    }

    public void update() { /* slot para inercia si querés */ }

    private void panCamera(int screenDx, int screenDy) {
        // Movimiento simple: convertir pixels a mundo basado en la distancia de la cámara
        // El FOV es 60 grados, así que tan(30°) = 0.577
        float worldPerPixelX = (distance * 2f * MathUtils.tan(MathUtils.degreesToRadians * 30f)) / camera.viewportWidth;
        float worldPerPixelY = (distance * 2f * MathUtils.tan(MathUtils.degreesToRadians * 30f)) / camera.viewportHeight;

        // Convertir el movimiento del mouse a movimiento del mundo
        // Necesito considerar la rotación de la cámara
        float moveX = -screenDx * worldPerPixelX;
        float moveY = screenDy * worldPerPixelY;

        // Aplicar rotación del mundo basada en azimuth
        float az = azimuth * MathUtils.degreesToRadians;
        float cos = MathUtils.cos(az);
        float sin = MathUtils.sin(az);

        target.x += moveX * cos - moveY * sin;
        target.z += moveX * sin + moveY * cos;

        apply();
    }

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