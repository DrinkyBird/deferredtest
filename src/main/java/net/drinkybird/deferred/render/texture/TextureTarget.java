package net.drinkybird.deferred.render.texture;

import static org.lwjgl.opengl.GL33C.*;

public enum TextureTarget {
    TEXTURE_2D(GL_TEXTURE_2D),
    TEXTURE_CUBEMAP(GL_TEXTURE_CUBE_MAP);
    
    public final int value;
    
    private TextureTarget(final int value) {
        this.value = value;
    }
}
