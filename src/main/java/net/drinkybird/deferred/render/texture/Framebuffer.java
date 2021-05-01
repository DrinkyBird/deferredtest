package net.drinkybird.deferred.render.texture;

import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.ARBDirectStateAccess.*;
import static org.lwjgl.opengl.KHRDebug.*;

import java.util.ArrayList;
import java.util.List;

import net.drinkybird.deferred.core.NativeResource;
import net.drinkybird.deferred.render.GlObject;

public class Framebuffer extends NativeResource implements GlObject {
    private int fbo;
    private List<GlObject> attachments = new ArrayList<>();
    
    public Framebuffer() {
        fbo = glCreateFramebuffers();
    }
    
    @Override
    public void destroy() {
        attachments.clear();
        glDeleteFramebuffers(fbo);
    }
    
    public void attachTexure(FramebufferAttachmentType type, int index, Texture texture) {
        int maxColourAttachments = glGetInteger(GL_MAX_COLOR_ATTACHMENTS);
        
        if (type != FramebufferAttachmentType.COLOUR && index != 0) {
            throw new IndexOutOfBoundsException("index for non-colour attachments can only be 0");
        } else if (index < 0 || index > maxColourAttachments) {
            throw new IndexOutOfBoundsException("index for colour attachments must be in range 0-" + maxColourAttachments);
        }
        
        glNamedFramebufferTexture(fbo, type.value + index, texture.getName(), 0);
        attachments.add(texture);
    }
    
    public void attachTexureLayer(FramebufferAttachmentType type, int index, Texture texture, int layer) {
        int maxColourAttachments = glGetInteger(GL_MAX_COLOR_ATTACHMENTS);
        
        if (type != FramebufferAttachmentType.COLOUR && index != 0) {
            throw new IndexOutOfBoundsException("index for non-colour attachments can only be 0");
        } else if (index < 0 || index > maxColourAttachments) {
            throw new IndexOutOfBoundsException("index for colour attachments must be in range 0-" + maxColourAttachments);
        }
        
        glNamedFramebufferTextureLayer(fbo, type.value + index, texture.getName(), 0, layer);
        attachments.add(texture);
    }
    
    public void attachRenderbuffer(FramebufferAttachmentType type, int index, Renderbuffer renderbuffer) {
        int maxColourAttachments = glGetInteger(GL_MAX_COLOR_ATTACHMENTS);
        
        if (type != FramebufferAttachmentType.COLOUR && index != 0) {
            throw new IndexOutOfBoundsException("index for non-colour attachments can only be 0");
        } else if (index < 0 || index > maxColourAttachments) {
            throw new IndexOutOfBoundsException("index for colour attachments must be in range 0-" + maxColourAttachments);
        }
        
        glNamedFramebufferRenderbuffer(fbo, type.value + index, GL_RENDERBUFFER, renderbuffer.getName());
        attachments.add(renderbuffer);
    }
    
    public boolean isComplete() {
        return glCheckNamedFramebufferStatus(fbo, GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE;
    }
    
    public void setDrawBuffers(int[] buffers) {
        glNamedFramebufferDrawBuffers(fbo, buffers);
    }
    
    public void setDrawBuffer(int buf) {
        glNamedFramebufferDrawBuffer(fbo, buf);
    }
    
    public void setReadBuffer(int buf) {
        glNamedFramebufferReadBuffer(fbo, buf);
    }
    
    @Override
    public int getName() {
        return fbo;
    }

    @Override
    public void setLabel(String label) {
        glObjectLabel(GL_FRAMEBUFFER, fbo, label);
    }
}
