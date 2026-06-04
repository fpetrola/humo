#ifdef GL_ES
precision mediump float;
#endif

attribute vec3 a_position;
attribute vec3 a_normal;

uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;
uniform mat4 u_viewTrans;
uniform mat3 u_normalMatrix;

varying vec3 v_normal;
varying vec3 v_worldPos;
varying float v_depth;

void main() {
    v_worldPos = vec3(u_worldTrans * vec4(a_position, 1.0));
    v_normal = normalize(u_normalMatrix * a_normal);

    // Calcular profundidad para fog
    vec4 viewPos = u_viewTrans * u_worldTrans * vec4(a_position, 1.0);
    v_depth = length(viewPos.xyz);

    gl_Position = u_projTrans * vec4(a_position, 1.0);
}
