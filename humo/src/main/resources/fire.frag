#ifdef GL_ES
precision mediump float;
#endif

varying vec3 v_normal;
varying vec3 v_worldPos;
varying vec2 v_texCoord;
varying float v_flame;

uniform float u_time;

// Función simple de ruido
float noise(vec2 p) {
    return fract(sin(p.x * 12.9898 + p.y * 78.233) * 43758.5453);
}

void main() {
    // Crear patrón de fuego
    vec2 uv = v_texCoord;
    uv.y += u_time * 0.5;

    // Múltiples capas de ruido
    float n1 = noise(uv * 3.0);
    float n2 = noise(uv * 5.0 + u_time);
    float n3 = noise(uv * 7.0 - u_time * 0.7);

    // Combinar ruidos
    float flame = n1 * 0.5 + n2 * 0.3 + n3 * 0.2;

    // Gradiente de fuego (rojo abajo, naranja en medio, amarillo arriba)
    vec3 flameColor;
    if (uv.y < 0.3) {
        // Rojo oscuro
        flameColor = mix(vec3(0.0, 0.0, 0.0), vec3(1.0, 0.2, 0.0), flame);
    } else if (uv.y < 0.6) {
        // Naranja
        flameColor = mix(vec3(1.0, 0.2, 0.0), vec3(1.0, 0.7, 0.0), flame);
    } else {
        // Amarillo y blanco
        flameColor = mix(vec3(1.0, 0.7, 0.0), vec3(1.0, 1.0, 0.8), flame);
    }

    // Opacidad: más opaco en el medio, transparente en los bordes
    float alpha = (1.0 - abs(v_texCoord.x - 0.5) * 2.0) * flame;

    gl_FragColor = vec4(flameColor, alpha);
}
