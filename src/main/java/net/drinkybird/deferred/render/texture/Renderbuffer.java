package net.drinkybird.deferred.render.texture;

import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.ARBDirectStateAccess.*;
import static org.lwjgl.opengl.KHRDebug.*;

import net.drinkybird.deferred.core.NativeResource;
import net.drinkybird.deferred.render.GlObject;

public class Renderbuffer extends NativeResource implements GlObject {
    private int rbo;
    
    public Renderbuffer(int format, int width, int height) {
        rbo = glCreateRenderbuffers();
        glNamedRenderbufferStorage(rbo, format, width, height);
    }
    
    @Override
    public void destroy() {
        glDeleteRenderbuffers(rbo);
    }

    @Override
    public int getName() {
        return rbo;
    }

    @Override
    public void setLabel(String label) {
        glObjectLabel(GL_RENDERBUFFER, rbo, label);
    }
}
