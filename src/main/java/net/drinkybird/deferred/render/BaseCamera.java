package net.drinkybird.deferred.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class BaseCamera {
    public abstract Matrix4f getProjectionMatrix();
    public abstract Matrix4f getViewMatrix();
    public abstract Vector3f getEyePosition();
    public abstract float getViewRadius();
}
