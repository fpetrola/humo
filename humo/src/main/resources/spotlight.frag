#ifdef GL_ES
precision mediump float;
#endif

varying vec3 v_normal;
varying vec3 v_worldPos;

uniform vec3 u_spotPos;
uniform vec3 u_spotDir;
uniform vec3 u_spotColor;
uniform float u_spotCutoff;
uniform float u_spotOuterCutoff;
uniform float u_spotRange;
uniform vec4 u_baseColor;
uniform vec3 u_ambientLight;

void main() {
    vec3 norm = normalize(v_normal);
    vec3 spotToPixel = v_worldPos - u_spotPos;
    float distance = length(spotToPixel);

    // Atenuación por distancia
    float attenuation = 1.0 / (1.0 + distance * distance * 0.001);

    // Dirección desde el spotlight al píxel
    vec3 lightDir = normalize(-spotToPixel);

    // Ángulo del cono
    float theta = dot(lightDir, normalize(u_spotDir));

    // Suavidad en los bordes del cono
    float epsilon = u_spotCutoff - u_spotOuterCutoff;
    float intensity = smoothstep(u_spotOuterCutoff - 0.1, u_spotCutoff, theta);

    // Iluminación básica (Lambertian)
    float diff = max(dot(norm, lightDir), 0.0);

    // Luz del spotlight
    vec3 spotLight = u_spotColor * diff * intensity * attenuation;

    // Luz ambiental muy baja
    vec3 finalColor = u_baseColor.rgb * (u_ambientLight + spotLight);

    gl_FragColor = vec4(finalColor, 1.0);
}
