layout(location = 0) in vec3 i_position;
layout(location = 1) in vec3 i_normal;
layout(location = 2) in vec2 i_texCoord;

out vec3 s_position;
out vec3 s_normal;
out vec2 s_texCoord;

uniform mat4 u_projection;
uniform mat4 u_model;
uniform mat4 u_view;

void main() {
    gl_Position = (u_projection * u_view * u_model) * vec4(i_position, 1.0);

    s_position = (u_view * u_model * vec4(i_position, 1.0)).xyz;
    s_normal = i_normal;
    s_texCoord = i_texCoord;
}
