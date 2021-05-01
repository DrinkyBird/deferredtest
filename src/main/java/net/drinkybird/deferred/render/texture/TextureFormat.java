package net.drinkybird.deferred.render.texture;

import static org.lwjgl.opengl.GL33C.*;

public enum TextureFormat {
    RGB(GL_RGB, 3),
    RGBA(GL_RGBA, 4),
    BGR(GL_BGR, 3),
    BGRA(GL_BGRA, 4),
    DEPTH_COMPONENT(GL_DEPTH_COMPONENT, 3),
    RGB_8(GL_RGB8, 3),
    RGBA_8(GL_RGBA8, 4),
    RGBA_16F(GL_RGBA16F, 8),
    RGBA_32F(GL_RGBA32F, 16),
    DEPTH_COMPONENT_24(GL_DEPTH_COMPONENT24, 3);
    
    public final int value;
    public final int numBytes;
    
    private TextureFormat(final int value, final int numBytes) {
        this.value = value;
        this.numBytes = numBytes;
    }
}
