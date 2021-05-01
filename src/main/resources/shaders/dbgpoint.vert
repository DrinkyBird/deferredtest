layout(location = 0) in vec3 i_position;
layout(location = 1) in vec4 i_colour;

out vec4 s_colour;

uniform mat4 u_projection;
uniform mat4 u_modelView;

void main() {
    gl_Position = u_projection * u_modelView * vec4(i_position, 1.0);

    s_colour = i_colour;
}
