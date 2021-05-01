package net.drinkybird.deferred.render.mesh;

import static org.lwjgl.opengl.GL33C.*;

public enum PrimitiveTopology {
    POINT_LIST(GL_POINTS),
    LINE_LIST(GL_LINES),
    TRIANGLE_LIST(GL_TRIANGLES);
    
    public final int value;
    
    private PrimitiveTopology(final int value) {
        this.value = value;
    }
}
