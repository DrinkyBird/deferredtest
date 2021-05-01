package net.drinkybird.deferred.render;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RenderConstants {
    public static final String GLSL_VERSION = "330 core";
    
    public static final int MAX_LIGHTS = 32;
    
    public static final Vector3f WORLD_UP = new Vector3f(0.0f, 1.0f, 0.0f);
    public static final Vector3f WORLD_DOWN = new Vector3f(0.0f, -1.0f, 0.0f);
    
    private RenderConstants() { }
}
