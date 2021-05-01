package net.drinkybird.deferred.render.texture;

import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.ARBDirectStateAccess.*;
import static org.lwjgl.opengl.KHRDebug.*;

import java.awt.Image;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBColorBufferFloat;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import net.drinkybird.deferred.core.NativeResource;
import net.drinkybird.deferred.render.GlObject;
import net.drinkybird.deferred.render.GlState;

public class Texture extends NativeResource implements GlObject {
    private final TextureTarget target;
    private final int id;
    private final int width, height;
    
    Texture(TextureTarget target, int id, int width, int height) {
        this.target = target;
        this.id = id;
        this.width = width;
        this.height = height;
    }
    
    public Texture(TextureTarget target, int width, int height, TextureFilter minFilter, TextureFilter magFilter, TextureFormat internalFormat, TextureFormat format, ByteBuffer pixels) {
        this.target = target;
        this.width = width;
        this.height = height;
        this.id = glCreateTextures(target.value);
        
        glTextureParameteri(id, GL_TEXTURE_MIN_FILTER, minFilter.value); GlState.checkError("texture GL_TEXTURE_MIN_FILTER");
        glTextureParameteri(id, GL_TEXTURE_MAG_FILTER, magFilter.value); GlState.checkError("texture GL_TEXTURE_MAG_FILTER");
        glTextureStorage2D(id, 1, internalFormat.value, width, height);
        glTextureSubImage2D(id, 0, 0, 0, width, height, format.value, GL_UNSIGNED_BYTE, pixels);
    }
    
    public Texture(TextureTarget target, int width, int height, TextureFilter filter, TextureFormat internalFormat, TextureFormat format, ByteBuffer pixels) {
        this(target, width, height, filter, filter, internalFormat, format, pixels);
    }
    
    public Texture(TextureTarget target, int width, int height, TextureFilter minFilter, TextureFilter magFilter, TextureFormat format, ByteBuffer pixels) {
        this(target, width, height, minFilter, magFilter, format, format, pixels);
    }
    
    public Texture(TextureTarget target, int width, int height, TextureFilter filter, TextureFormat format, ByteBuffer pixels) {
        this(target, width, height, filter, filter, format, format, pixels);
    }
    
    public Texture(TextureTarget target, int width, int height, TextureFilter minFilter, TextureFilter magFilter, TextureFormat internalFormat) {
        this.target = target;
        this.width = width;
        this.height = height;
        this.id = glCreateTextures(target.value);
        
        glTextureParameteri(id, GL_TEXTURE_MIN_FILTER, minFilter.value); GlState.checkError("texture GL_TEXTURE_MIN_FILTER");
        glTextureParameteri(id, GL_TEXTURE_MAG_FILTER, magFilter.value); GlState.checkError("texture GL_TEXTURE_MAG_FILTER");
        glTextureStorage2D(id, 1, internalFormat.value, width, height); GlState.checkError("texture storage");
    }
    
    public Texture(TextureTarget target, int width, int height, TextureFilter filter, TextureFormat internalFormat) {
        this(target, width, height, filter, filter, internalFormat);
    }
    
    @Override
    public void destroy() {
        glDeleteTextures(id);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public TextureTarget getTarget() {
        return target;
    }

    @Override
    public int getName() {
        return id;
    }

    @Override
    public void setLabel(String label) {
        glObjectLabel(GL_TEXTURE, id, label);
    }
    
    public void save(String path, FramebufferAttachmentType attachmentType, TextureFormat format, int layer) {
        Framebuffer tempFramebuffer = new Framebuffer();
        tempFramebuffer.attachTexureLayer(attachmentType, 0, this, layer);
        
        int past = glGetInteger(GL_FRAMEBUFFER_BINDING);
        glBindFramebuffer(GL_FRAMEBUFFER, tempFramebuffer.getName());
        
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        IntBuffer pixelsInt = pixels.asIntBuffer();
        
        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        glReadPixels(0, 0, width, height, format.value, GL_UNSIGNED_BYTE, buffer);
        IntBuffer bufferInt = buffer.asIntBuffer();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int di = x + (height - y - 1) * width;
                int si = x + y * width;
                
                pixelsInt.put(di, bufferInt.get(si));
            }
        }
        MemoryUtil.memFree(buffer);
        
        STBImageWrite.stbi_write_png(path, width, height, 4, pixels, width * 4);
        
        glBindFramebuffer(GL_FRAMEBUFFER, past);
        
        Logger.info("Saved texture to {}", path);
    }
    
    public void save(String path, FramebufferAttachmentType attachmentType, TextureFormat format) {
        Framebuffer tempFramebuffer = new Framebuffer();
        tempFramebuffer.attachTexure(attachmentType, 0, this);
        
        int past = glGetInteger(GL_FRAMEBUFFER_BINDING);
        glBindFramebuffer(GL_FRAMEBUFFER, tempFramebuffer.getName());
        
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        IntBuffer pixelsInt = pixels.asIntBuffer();
        
        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        glReadPixels(0, 0, width, height, format.value, GL_UNSIGNED_BYTE, buffer);
        IntBuffer bufferInt = buffer.asIntBuffer();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int di = x + (height - y - 1) * width;
                int si = x + y * width;
                
                pixelsInt.put(di, bufferInt.get(si));
            }
        }
        MemoryUtil.memFree(buffer);
        
        STBImageWrite.stbi_write_png(path, width, height, 4, pixels, width * 4);
        
        glBindFramebuffer(GL_FRAMEBUFFER, past);
        
        Logger.info("Saved texture to {}", path);
    }
}
