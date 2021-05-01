in vec2 s_texCoord;

out vec4 o_fragColour;

uniform sampler2D u_texture;

void main() {
    o_fragColour = texture(u_texture, s_texCoord);
}