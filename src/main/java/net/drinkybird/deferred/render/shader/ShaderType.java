package net.drinkybird.deferred.render.shader;

import static org.lwjgl.opengl.GL33C.*;

public enum ShaderType {
    VERTEX(GL_VERTEX_SHADER),
    GEOMETRY(GL_GEOMETRY_SHADER),
    FRAGMENT(GL_FRAGMENT_SHADER);
    
    public final int value;
    
    private ShaderType(int type) {
        this.value = type;
    }
}
