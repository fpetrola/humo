#ifdef GL_ES
precision mediump float;
#endif

attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;
uniform mat3 u_normalMatrix;
uniform float u_time;

varying vec3 v_normal;
varying vec3 v_worldPos;
varying vec2 v_texCoord;
varying float v_flame;

void main() {
    v_worldPos = vec3(u_worldTrans * vec4(a_position, 1.0));
    v_normal = normalize(u_normalMatrix * a_normal);
    v_texCoord = a_texCoord0;

    // Movimiento de llama hacia arriba
    vec4 pos = vec4(a_position, 1.0);
    pos.y += sin(u_time + a_position.x * 2.0) * 0.5;
    pos.x += sin(u_time * 0.7 + a_position.y) * 0.3;

    v_flame = sin(u_time + a_position.y * 3.0) * 0.5 + 0.5;

    gl_Position = u_projTrans * u_worldTrans * pos;
}
