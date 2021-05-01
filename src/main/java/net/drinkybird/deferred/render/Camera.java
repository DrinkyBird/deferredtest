package net.drinkybird.deferred.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;
import org.tinylog.Logger;

import net.drinkybird.deferred.Game;
import net.drinkybird.deferred.util.TimeUtil;

public class Camera extends BaseCamera {
    private static Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);
    
    public Vector3f centre = new Vector3f(8.0f, 0.0f, 8.0f);
    public Vector3f pos = new Vector3f(0.0f, 15.0f, 15.0f);
    public Vector3f forward = new Vector3f(0.0f, 0.0f, 1.0f);
    public Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);
    private Matrix4f viewMatrix = new Matrix4f();
    
    public Camera() {
        
    }
    
    public void tick() {
        var xlat = new Vector3f();
        float speed = (Game.instance.keys[GLFW.GLFW_KEY_LEFT_SHIFT] ? 10.0f : 1.0f);

        if (Game.instance.keys[GLFW.GLFW_KEY_W]) {
            forward.mul(-0.1f * speed * Game.instance.getDelta(), xlat);
            centre.add(xlat);
        }
        
        if (Game.instance.keys[GLFW.GLFW_KEY_S]) {
            forward.mul(0.1f * speed * Game.instance.getDelta(), xlat);
            centre.add(xlat);
        }

        if (Game.instance.keys[GLFW.GLFW_KEY_A]) {
            right.mul(-0.1f * speed *  Game.instance.getDelta(), xlat);
            centre.add(xlat);
        }

        if (Game.instance.keys[GLFW.GLFW_KEY_D]) {
            right.mul(0.1f * speed * Game.instance.getDelta(), xlat);
            centre.add(xlat);
        }

        if (Game.instance.keys[GLFW.GLFW_KEY_R]) {
            pos.add(0.0f, 0.1f * speed * Game.instance.getDelta(), 0.0f);
        }

        if (Game.instance.keys[GLFW.GLFW_KEY_F]) {
            pos.sub(0.0f, 0.1f * speed * Game.instance.getDelta(), 0.0f);
        }
    }
    
    @Override
    public Matrix4f getProjectionMatrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.perspective((float)Math.toRadians(90.0), 16.0f / 9.0f, 0.01f, 1000.0f);
        
        return matrix;
    }

    @Override
    public Matrix4f getViewMatrix() {
        var eye = getEyePosition();
        //eye.negate();
        viewMatrix.setLookAt(eye, centre, UP);
        
        return viewMatrix;
    }
    
    public Vector3f getAbsoluteEyePosition() {
        Vector3f eye = new Vector3f();
        centre.add(pos, eye);
        return eye;
    }
    
    @Override
    public Vector3f getEyePosition() {
        Vector3f eye = new Vector3f(getAbsoluteEyePosition());
        return eye;
    }
    
    @Override
    public float getViewRadius() {
        return 16.0f * 16.0f;
    }
}
