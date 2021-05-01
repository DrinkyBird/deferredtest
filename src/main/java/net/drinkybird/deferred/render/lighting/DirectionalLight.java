package net.drinkybird.deferred.render.lighting;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33C;

import net.drinkybird.deferred.render.BaseCamera;
import net.drinkybird.deferred.render.texture.Framebuffer;
import net.drinkybird.deferred.render.texture.FramebufferAttachmentType;
import net.drinkybird.deferred.render.texture.Texture;
import net.drinkybird.deferred.render.texture.TextureFilter;
import net.drinkybird.deferred.render.texture.TextureFormat;
import net.drinkybird.deferred.render.texture.TextureTarget;

public class DirectionalLight extends BaseCamera {
    public Vector3f direction = new Vector3f(1f, 1f, 1f);
    public Vector3f colour = new Vector3f(1.0f, 0.5f, 0.0f);
    public float intensity = 0.5f;
    
    public DirectionalLight() {
    }
    
    @Override
    public Matrix4f getProjectionMatrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.ortho(-40.0f, 40.0f, -40.0f, 40.0f, 1.0f, 1000.0f);
        
        return matrix;
    }

    @Override
    public Matrix4f getViewMatrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.lookAt(
                direction.x, direction.y, direction.z, 
                0.0f, 0.0f, 0.0f, 
                0.0f, 1.0f, 0.0f);
        
        return matrix;
    }

    @Override
    public Vector3f getEyePosition() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public float getViewRadius() {
        return 16.0f;
    }
}
