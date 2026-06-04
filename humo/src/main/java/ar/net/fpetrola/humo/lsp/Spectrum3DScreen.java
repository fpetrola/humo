package ar.net.fpetrola.humo.lsp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Spectrum3DScreen extends ApplicationAdapter {
    public static final int W = 256;
    public static final int H = 192;
    private static final float PIXEL = 1f;
    private static final float MAX_SPACING = 3f;

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment env;
    private Model cubeModel;
    private Array<ModelInstance> pixels;
    private ColorAttribute[] pixelColors;
    private int[] paletteIndices;
    private float spacing = 0f;

    private OrbitCameraController controller;

    private int currentScene = 0;
    private static final int SCENE_COUNT = 4;
    private float sceneTimer = 0f;
    private static final float SCENE_DURATION = 8f;

    private Array<PointLight> multiLights;
    private Array<ModelInstance> multiBulbs;
    private float[] multiLightTimers;
    private static final int NUM_LIGHTS = 5;

    private static final Color[] LIGHT_COLORS = {
        new Color(1f, 0f, 0f, 1f),      // Rojo
        new Color(0f, 1f, 0f, 1f),      // Verde
        new Color(0f, 0f, 1f, 1f),      // Azul
        new Color(1f, 1f, 0f, 1f),      // Amarillo
        new Color(1f, 0f, 1f, 1f),      // Magenta
        new Color(0f, 1f, 1f, 1f),      // Cian
        new Color(1f, 0.5f, 0f, 1f),    // Naranja
        new Color(1f, 0.2f, 0.8f, 1f),  // Rosa
        new Color(0f, 1f, 0.5f, 1f),    // Verde claro
        new Color(0.3f, 0.7f, 1f, 1f)   // Azul claro
    };

    private static final float[] LIGHT_SPEEDS = {
        0.08f, 0.12f, 0.06f, 0.14f, 0.10f,
        0.09f, 0.13f, 0.07f, 0.11f, 0.15f
    };

    private static final float[] LIGHT_RADII_X = {
        120f, 100f, 110f, 90f, 130f,
        105f, 95f, 115f, 85f, 125f
    };

    private static final float[] LIGHT_RADII_Y = {
        85f, 70f, 80f, 95f, 75f,
        88f, 82f, 78f, 92f, 72f
    };

    private float fogIntensity = 0f;
    private static final float FOG_OSCILLATION_SPEED = 0.5f;

    private static final Color[] PALETTE = {
        new Color(0,    0,    0,    1),
        new Color(0,    0,    0.85f,1),
        new Color(0.85f,0,    0,    1),
        new Color(0.85f,0,    0.85f,1),
        new Color(0,    0.85f,0,    1),
        new Color(0,    0.85f,0.85f,1),
        new Color(0.85f,0.85f,0,    1),
        new Color(0.85f,0.85f,0.85f,1)
    };

    @Override
    public void create() {
        camera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 8000f;

        modelBatch = new ModelBatch();

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0f, 0f, 0f, 1f));

        multiLights = new Array<>();
        multiBulbs = new Array<>();
        multiLightTimers = new float[NUM_LIGHTS];

        // Crear 10 luces con diferentes colores
        ModelBuilder mb2 = new ModelBuilder();
        for (int i = 0; i < NUM_LIGHTS; i++) {
            Color color = LIGHT_COLORS[i];

            // Crear PointLight
            PointLight light = new PointLight();
            light.set(color.r * 3.5f, color.g * 3.5f, color.b * 3.5f,
                W / 2f, H / 2f, 0, 3000f);
            multiLights.add(light);
            env.add(light);

            // Crear esfera luminosa
            Material bulbMaterial = new Material();
            bulbMaterial.set(ColorAttribute.createDiffuse(color));
            bulbMaterial.set(ColorAttribute.createEmissive(
                new Color(color.r * 2.5f, color.g * 2.5f, color.b * 2.5f, 1f)));
            Model bulbModel = mb2.createSphere(8f, 8f, 8f, 32, 32,
                bulbMaterial, Usage.Position | Usage.Normal);
            ModelInstance bulb = new ModelInstance(bulbModel);
            multiBulbs.add(bulb);

            multiLightTimers[i] = 0f;
        }

        ModelBuilder mb = new ModelBuilder();
        cubeModel = mb.createBox(PIXEL, PIXEL, PIXEL,
            new Material(ColorAttribute.createDiffuse(Color.WHITE)),
            Usage.Position | Usage.Normal);

        pixels = new Array<>(W * H);
        pixelColors = new ColorAttribute[W * H];
        paletteIndices = new int[W * H];

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                ModelInstance inst = new ModelInstance(cubeModel);
                inst.transform.setToTranslation(x, (H - 1) - y, 0);
                Material mat = new Material(ColorAttribute.createDiffuse(Color.BLACK));
                inst.nodes.get(0).parts.get(0).material = mat;
                pixels.add(inst);
                pixelColors[y * W + x] = (ColorAttribute) mat.get(ColorAttribute.Diffuse);
            }
        }

        renderScene(0);

        controller = new OrbitCameraController(camera, new Vector3(W / 2f, H / 2f, 0));
        controller.setDistance(350f);
        Gdx.input.setInputProcessor(controller);
    }

    public void setPixel(int x, int y, int paletteIndex, boolean bright) {
        if (x < 0 || x >= W || y < 0 || y >= H) return;
        paletteIndices[y * W + x] = bright ? (paletteIndex & 7) + 8 : (paletteIndex & 7);
        Color c = PALETTE[paletteIndex & 7];
        ColorAttribute attr = pixelColors[y * W + x];
        if (bright) {
            attr.color.set(
                Math.min(c.r * 1.176f, 1f),
                Math.min(c.g * 1.176f, 1f),
                Math.min(c.b * 1.176f, 1f), 1f);
        } else {
            attr.color.set(c);
        }
    }

    public void updateFromBuffer(int[] indices) {
        for (int i = 0; i < indices.length; i++) {
            int idx = indices[i];
            paletteIndices[i] = idx;
            boolean bright = idx >= 8;
            Color c = PALETTE[(bright ? idx - 8 : idx) & 7];
            ColorAttribute attr = pixelColors[i];
            if (bright) {
                attr.color.set(
                    Math.min(c.r * 1.176f, 1f),
                    Math.min(c.g * 1.176f, 1f),
                    Math.min(c.b * 1.176f, 1f), 1f);
            } else {
                attr.color.set(c);
            }
        }
    }

    private void updatePositions() {
        float scale = 1f + spacing;
        float cx = W / 2f;
        float cy = H / 2f;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int i = y * W + x;
                float px = (x - cx) * scale + cx;
                float py = ((H - 1 - y) - cy) * scale + cy;
                pixels.get(i).transform.setToTranslation(px, py, 0);
            }
        }
        if (controller != null) controller.setTarget(new Vector3(cx, cy, 0));
    }

    private void renderScene(int scene) {
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                setPixel(x, y, 0, false);

        switch (scene) {
            case 0: renderLandscapeScene(); break;
            case 1: renderSpaceScene(); break;
            case 2: renderGeometricScene(); break;
            case 3: renderCityScene(); break;
        }
        updatePositions();
    }

    private void renderLandscapeScene() {
        for (int x = 0; x < W; x++)
            for (int t = 0; t < 3; t++) {
                setPixel(x, t, 1, true);
                setPixel(x, H - 1 - t, 1, true);
            }
        for (int y = 0; y < H; y++)
            for (int t = 0; t < 3; t++) {
                setPixel(t, y, 1, true);
                setPixel(W - 1 - t, y, 1, true);
            }

        for (int x = 3; x < W - 3; x++) {
            int hillLine = 130 + (int)(8 * Math.sin(x * 0.025)) + (int)(5 * Math.sin(x * 0.06));
            for (int y = hillLine; y < H - 3; y++)
                setPixel(x, y, 4, y > 155);
        }

        for (int y = 165; y < H - 3; y++)
            for (int x = 90; x < 190; x++) {
                int wave = (int)(3 * Math.sin(x * 0.08 + y * 0.2));
                int px = x + wave;
                if (px >= 3 && px < W - 3)
                    setPixel(px, y, 5, y % 4 < 2);
            }

        int sunCx = 205, sunCy = 35, sunR = 16;
        for (int dy = -sunR; dy <= sunR; dy++)
            for (int dx = -sunR; dx <= sunR; dx++)
                if (dx * dx + dy * dy <= sunR * sunR)
                    setPixel(sunCx + dx, sunCy + dy, 6, true);

        for (int a = 0; a < 360; a += 30)
            for (int r = sunR + 3; r < sunR + 12; r++)
                setPixel(
                    sunCx + (int)(r * Math.cos(Math.toRadians(a))),
                    sunCy + (int)(r * Math.sin(Math.toRadians(a))),
                    6, true);

        int rcx = 128, rcy = 115;
        int[] rainbowColors = {2, 6, 4, 5, 1, 3};
        for (int band = 0; band < rainbowColors.length; band++) {
            int outerR = 85 - band * 5;
            int innerR = outerR - 4;
            for (int a = 0; a < 180; a++) {
                double rad = Math.toRadians(a);
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);
                for (int r = innerR; r <= outerR; r++) {
                    int px = rcx + (int)(r * cos);
                    int py = rcy - (int)(r * sin);
                    if (px >= 3 && px < W - 3 && py >= 3 && py < rcy)
                        setPixel(px, py, rainbowColors[band], true);
                }
            }
        }

        for (int y = 108; y < 148; y++)
            for (int x = 25; x < 75; x++)
                setPixel(x, y, 2, false);

        int roofPeak = 83, roofBase = 108, roofMid = 50;
        for (int y = roofPeak; y < roofBase; y++) {
            float progress = (float)(y - roofPeak) / (roofBase - roofPeak);
            int half = (int)(25 * progress);
            for (int x = roofMid - half; x <= roofMid + half; x++)
                setPixel(x, y, 2, true);
        }

        for (int y = 128; y < 148; y++)
            for (int x = 43; x < 57; x++)
                setPixel(x, y, 6, false);

        for (int y = 115; y < 124; y++) {
            for (int x = 30; x < 40; x++) setPixel(x, y, 5, true);
            for (int x = 60; x < 70; x++) setPixel(x, y, 5, true);
        }
    }

    private void renderSpaceScene() {
        for (int i = 0; i < 80; i++) {
            int sx = (i * 37 + 13) % W;
            int sy = (i * 23 + 7) % H;
            setPixel(sx, sy, 7, true);
        }

        int cx = 128, cy = 96, r = 30;
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r * r) {
                    int color = d2 < (r * r / 4) ? 5 : (d2 < (r * r * 3 / 4) ? 1 : 4);
                    boolean bright = d2 < (r * r / 2);
                    setPixel(cx + dx, cy + dy, color, bright);
                }
            }

        int ringR = 45;
        for (int a = 0; a < 360; a++) {
            double rad = Math.toRadians(a);
            int rx = cx + (int)(ringR * Math.cos(rad));
            int ry = cy + (int)(ringR * 0.3 * Math.sin(rad));
            if (rx >= 0 && rx < W && ry >= 0 && ry < H)
                setPixel(rx, ry, 6, true);
            rx = cx + (int)((ringR + 1) * Math.cos(rad));
            ry = cy + (int)((ringR + 1) * 0.3 * Math.sin(rad));
            if (rx >= 0 && rx < W && ry >= 0 && ry < H)
                setPixel(rx, ry, 6, false);
        }

        int mx = 40, my = 50, mr = 20;
        for (int dy = -mr; dy <= mr; dy++)
            for (int dx = -mr; dx <= mr; dx++)
                if (dx * dx + dy * dy <= mr * mr)
                    setPixel(mx + dx, my + dy, 7, false);
        int mx2 = 48, my2 = 44, mr2 = 16;
        for (int dy = -mr2; dy <= mr2; dy++)
            for (int dx = -mr2; dx <= mr2; dx++)
                if (dx * dx + dy * dy <= mr2 * mr2)
                    setPixel(mx2 + dx, my2 + dy, 0, false);

        int sx = 200, sy = 140;
        for (int dy = -8; dy <= 8; dy++)
            for (int dx = -20; dx <= 20; dx++) {
                float ellipse = (dx * dx) / 400f + (dy * dy) / 64f;
                if (ellipse <= 1f)
                    setPixel(sx + dx, sy + dy, 2, true);
            }
        for (int i = -3; i <= 3; i++) {
            for (int t = 1; t <= 15; t++) {
                setPixel(sx - 20 - t, sy + i + t / 3, 6, true);
                setPixel(sx - 20 - t, sy + i - t / 3, 6, true);
            }
        }

        for (int i = 0; i < 5; i++) {
            int px = 180 + i * 3;
            int py = 50 + i * 2;
            setPixel(px, py, 3, true);
            setPixel(px + 1, py, 3, true);
        }
    }

    private void renderGeometricScene() {
        int cx = W / 2, cy = H / 2;

        for (int r = 80; r > 10; r -= 10) {
            int color = ((80 - r) / 10) % 7 + 1;
            for (int a = 0; a < 360; a++) {
                double rad = Math.toRadians(a);
                int px = cx + (int)(r * Math.cos(rad));
                int py = cy + (int)(r * Math.sin(rad));
                if (px >= 0 && px < W && py >= 0 && py < H)
                    setPixel(px, py, color, r % 20 == 0);
            }
        }

        for (int a = 0; a < 360; a += 45) {
            double rad = Math.toRadians(a);
            int color = (a / 45) % 7 + 1;
            for (int r = 0; r < 85; r++) {
                int px = cx + (int)(r * Math.cos(rad));
                int py = cy + (int)(r * Math.sin(rad));
                if (px >= 0 && px < W && py >= 0 && py < H)
                    setPixel(px, py, color, true);
            }
        }

        int triSize = 40;
        for (int row = 0; row < triSize; row++) {
            int lx = 10 + row / 2;
            int rx = 10 + triSize - row / 2;
            int ty = 20 + row;
            for (int x = lx; x <= rx; x++)
                if (x == lx || x == rx || row == triSize - 1)
                    setPixel(x, ty, 4, true);
        }

        for (int y = 20; y < 70; y++)
            for (int x = 195; x < 245; x++)
                if (y == 20 || y == 69 || x == 195 || x == 244)
                    setPixel(x, y, 2, true);
        for (int y = 30; y < 60; y++)
            for (int x = 205; x < 235; x++)
                if (y == 30 || y == 59 || x == 205 || x == 234)
                    setPixel(x, y, 6, true);

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 - 90);
            int hx = cx + (int)(50 * Math.cos(angle));
            int hy = cy + (int)(50 * Math.sin(angle));
            double nextAngle = Math.toRadians((i + 1) * 60 - 90);
            int nx = cx + (int)(50 * Math.cos(nextAngle));
            int ny = cy + (int)(50 * Math.sin(nextAngle));
            int steps = 60;
            for (int s = 0; s < steps; s++) {
                float t = s / (float) steps;
                int px = (int)(hx + (nx - hx) * t);
                int py = (int)(hy + (ny - hy) * t);
                if (px >= 0 && px < W && py >= 0 && py < H)
                    setPixel(px, py, 3, true);
            }
        }
    }

    private void renderCityScene() {
        for (int x = 0; x < W; x++)
            for (int y = 0; y < 60; y++) {
                if (y < 20) setPixel(x, y, 1, false);
                else if (y < 40) setPixel(x, y, 1, true);
                else setPixel(x, y, 3, false);
            }

        int[][] buildings = {
            {10, 60, 30, 130}, {35, 40, 25, 130}, {65, 50, 20, 130},
            {90, 30, 35, 130}, {130, 55, 22, 130}, {155, 35, 28, 130},
            {188, 45, 30, 130}, {222, 38, 30, 130}
        };
        int[] buildingColors = {7, 5, 2, 6, 7, 4, 5, 2};

        for (int b = 0; b < buildings.length; b++) {
            int bx = buildings[b][0], by = buildings[b][1];
            int bw = buildings[b][2], bh = buildings[b][3];
            int color = buildingColors[b];

            for (int y = by; y <= bh; y++)
                for (int x = bx; x < bx + bw; x++)
                    if (x >= 0 && x < W && y >= 0 && y < H) {
                        if (y == by || y == bh || x == bx || x == bx + bw - 1)
                            setPixel(x, y, color, true);
                        else
                            setPixel(x, y, color, false);
                    }

            for (int wy = by + 5; wy < bh - 5; wy += 10)
                for (int wx = bx + 4; wx < bx + bw - 4; wx += 7)
                    for (int dy = 0; dy < 5; dy++)
                        for (int dx = 0; dx < 3; dx++)
                            if (wx + dx < bx + bw - 2 && wx + dx < W && wy + dy < H)
                                setPixel(wx + dx, wy + dy, 6, (wy + wx) % 3 == 0);
        }

        for (int y = 131; y < H; y++)
            for (int x = 0; x < W; x++)
                setPixel(x, y, 7, false);

        for (int x = 0; x < W; x += 20) {
            for (int y = 140; y < 145; y++)
                for (int dx = 0; dx < 10; dx++)
                    if (x + dx < W)
                        setPixel(x + dx, y, 6, true);
        }

        for (int i = 0; i < 6; i++) {
            int sx = 20 + i * 45;
            int sy = 15;
            setPixel(sx, sy, 7, true);
            setPixel(sx + 1, sy, 7, true);
            setPixel(sx - 1, sy, 7, true);
            setPixel(sx, sy + 1, 7, true);
            setPixel(sx, sy - 1, 7, true);
        }

        for (int y = 148; y < 155; y++)
            for (int x = 80; x < 180; x++)
                if (y == 148 || y == 154 || x == 80 || x == 179)
                    setPixel(x, y, 5, true);

        for (int dx = 0; dx < 6; dx++) {
            setPixel(125 + dx, 155, 7, false);
            setPixel(125 + dx, 156, 7, false);
        }
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        boolean changed = false;

        if (Gdx.input.isKeyPressed(Input.Keys.X) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            spacing = Math.min(spacing + 2f * dt, MAX_SPACING);
            changed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Z) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spacing = Math.max(spacing - 2f * dt, 0f);
            changed = true;
        }
        if (changed) updatePositions();

        sceneTimer += dt;
        if (sceneTimer >= SCENE_DURATION) {
            sceneTimer = 0f;
            currentScene = (currentScene + 1) % SCENE_COUNT;
            renderScene(currentScene);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            currentScene = (currentScene + 1) % SCENE_COUNT;
            sceneTimer = 0f;
            renderScene(currentScene);
        }

        // Actualizar posiciones de todas las 10 luces
        for (int i = 0; i < NUM_LIGHTS; i++) {
            multiLightTimers[i] += dt * LIGHT_SPEEDS[i];
            float angle = multiLightTimers[i] * MathUtils.PI2;

            // Cada luz tiene una órbita única
            float bulbX = W / 2f + MathUtils.cos(angle + i * 0.628f) * LIGHT_RADII_X[i];
            float bulbY = H / 2f + MathUtils.sin(angle * (1f + i * 0.1f)) * LIGHT_RADII_Y[i];
            float bulbZ = 70f * MathUtils.sin(angle + i * 0.3f);

            multiLights.get(i).position.set(bulbX, bulbY, bulbZ);
            multiBulbs.get(i).transform.setToTranslation(bulbX, bulbY, bulbZ);
        }

        controller.update();
        ScreenUtils.clear(0f, 0f, 0f, 1f, true);

        modelBatch.begin(camera);
        modelBatch.render(pixels, env);
        for (ModelInstance bulb : multiBulbs) {
            modelBatch.render(bulb, env);
        }
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        cubeModel.dispose();
    }
}
