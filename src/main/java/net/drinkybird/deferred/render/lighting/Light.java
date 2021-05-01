package net.drinkybird.deferred.render.lighting;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.drinkybird.deferred.render.BaseCamera;

public class Light extends BaseCamera {
    public Vector3f position;
    public Vector3f colour;
    public float radius;
    
    public Light(Vector3f position, Vector3f colour, float radius) {
        this.position = position;
        this.colour = colour;
        this.radius = radius;
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return null;
    }

    @Override
    public Matrix4f getViewMatrix() {
        return null;
    }

    @Override
    public Vector3f getEyePosition() {
        return position;
    }
    
    @Override
    public float getViewRadius() {
        return radius;
    }
}
