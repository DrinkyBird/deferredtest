package net.drinkybird.deferred.render.texture;

import static org.lwjgl.opengl.GL33C.*;

public enum TextureFilter {
    NEAREST(GL_NEAREST),
    LINEAR(GL_LINEAR);
    
    public final int value;
    
    private TextureFilter(final int value) {
        this.value = value;
    }
}
