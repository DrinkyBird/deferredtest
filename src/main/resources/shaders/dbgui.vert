layout(location = 0) in vec3 i_position;
layout(location = 1) in vec2 i_texCoord;

out vec2 s_texCoord;

uniform mat4 u_matrix;

void main() {
    gl_Position = u_matrix * vec4(i_position, 1.0);

    s_texCoord = i_texCoord;
}
