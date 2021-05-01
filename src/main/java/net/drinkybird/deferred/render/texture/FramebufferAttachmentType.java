package net.drinkybird.deferred.render.texture;

import static org.lwjgl.opengl.GL33C.*;

public enum FramebufferAttachmentType {
    COLOUR(GL_COLOR_ATTACHMENT0),
    DEPTH(GL_DEPTH_ATTACHMENT),
    STENCIL(GL_STENCIL_ATTACHMENT),
    DEPTH_STENCIL(GL_DEPTH_STENCIL_ATTACHMENT);
    
    public final int value;
    
    private FramebufferAttachmentType(final int value) {
        this.value = value;
    }
}
