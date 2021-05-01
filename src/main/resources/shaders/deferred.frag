layout (location = 0) out vec4 o_albedo;
layout (location = 1) out vec3 o_normal;
layout (location = 2) out vec3 o_position;

in vec3 s_position;
in vec3 s_normal;
in vec2 s_texCoord;

uniform sampler2D u_texture;

void main() {
    o_position = s_position;
    o_normal = normalize(s_normal);
    o_albedo.rgb = texture(u_texture, s_texCoord).rgb;
    o_albedo.a = 1.0;
}
