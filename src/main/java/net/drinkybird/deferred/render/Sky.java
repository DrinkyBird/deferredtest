package net.drinkybird.deferred.render;

import static org.lwjgl.opengl.GL33C.*;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;

import net.drinkybird.deferred.Game;
import net.drinkybird.deferred.render.mesh.Mesh;
import net.drinkybird.deferred.render.mesh.PrimitiveTopology;
import net.drinkybird.deferred.render.shader.ShaderProgram;
import net.drinkybird.deferred.render.shader.ShaderType;

public class Sky {
    private Mesh backgroundMesh;
    private ShaderProgram program;
    
    public Sky() {
        program = new ShaderProgram();
        program.addShader(ShaderType.VERTEX, "/shaders/sky.vert");
        program.addShader(ShaderType.FRAGMENT, "/shaders/sky.frag");
        program.link();

        final float bottomR = 1.0f;
        final float bottomG = 1.0f;
        final float bottomB = 1.0f;
        final float topR = 0.5f;
        final float topG = 0.8f;
        final float topB = 1.0f;
        
        backgroundMesh = new Mesh(PrimitiveTopology.TRIANGLE_LIST, 6 * 6, false);
        backgroundMesh.vertices = new float[] {
                // +Z face
                 0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f,  0.5f, 0.5f,
                 0.5f,  0.5f, 0.5f,
                
                // -Z face
                -0.5f,  0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                 0.5f, -0.5f, -0.5f,
                 0.5f,  0.5f, -0.5f,
                 
                // -X face
                -0.5f, -0.5f,  0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f,  0.5f, -0.5f,
                -0.5f,  0.5f,  0.5f,
                
                // +X face
                 0.5f,  0.5f, -0.5f,
                 0.5f, -0.5f, -0.5f,
                 0.5f, -0.5f,  0.5f,
                 0.5f,  0.5f,  0.5f,
                 
                // +Y face
                -0.5f,  0.5f,  0.5f,
                -0.5f,  0.5f, -0.5f,
                 0.5f,  0.5f, -0.5f,
                 0.5f,  0.5f,  0.5f,
                 
                // -Y face
                 0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f,  0.5f,
                 0.5f, -0.5f,  0.5f,
        };
        backgroundMesh.elements = new short[] {
                0, 1, 2, 2, 3, 0,
                4, 5, 6, 6, 7, 4,
                8, 9, 10, 10, 11, 8,
                12, 13, 14, 14, 15, 12,
                16, 17, 18, 18, 19, 16,
                20, 21, 22, 22, 23, 20
        };
        backgroundMesh.colours = new float[] {
                // +Z face
                bottomR, bottomG, bottomB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f,
                topR, topG, topB, 1.0f,
                topR, topG, topB, 1.0f,

                // -Z face
                topR, topG, topB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f,
                topR, topG, topB, 1.0f,

                // -X face
                bottomR, bottomG, bottomB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f,
                topR, topG, topB, 1.0f,
                topR, topG, topB, 1.0f,

                // +X face
                topR, topG, topB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f,
                topR, topG, topB, 1.0f,

                // +Y face
                topR, topG, topB, 1.0f,
                topR, topG, topB, 1.0f,
                topR, topG, topB, 1.0f,
                topR, topG, topB, 1.0f,

                // -Y face
                bottomR, bottomG, bottomB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f,
                bottomR, bottomG, bottomB, 1.0f
        };
        backgroundMesh.upload();
    }
    
    public void render() {
        GlState.debugScopeEnter("Render skybox");
        
        GlState.enableCullFace();
        
        final Camera camera = Game.instance.camera;
        AxisAngle4f rot = new AxisAngle4f();
        camera.getViewMatrix().getRotation(rot);
        
        Matrix4f projection = new Matrix4f();
        projection.perspective((float)Math.toRadians(90.0), 16.0f / 9.0f, 0.01f, 1000.0f);
        projection.rotate(rot);
        
        GlState.bindProgram(program);
        program.uniform("u_projection", projection);
        
        GlState.disableDepthTest();
        backgroundMesh.draw();
        
        GlState.debugScopeExit();
    }
}
