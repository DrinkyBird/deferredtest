package net.drinkybird.deferred.render;

import static org.lwjgl.opengl.GL33C.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.tinylog.Logger;

import net.drinkybird.deferred.Game;
import net.drinkybird.deferred.level.Chunk;
import net.drinkybird.deferred.level.ChunkID;
import net.drinkybird.deferred.level.World;
import net.drinkybird.deferred.render.lighting.DirectionalLight;
import net.drinkybird.deferred.render.lighting.Light;
import net.drinkybird.deferred.render.mesh.Mesh;
import net.drinkybird.deferred.render.mesh.PrimitiveTopology;
import net.drinkybird.deferred.render.shader.ShaderProgram;
import net.drinkybird.deferred.render.shader.ShaderType;
import net.drinkybird.deferred.render.texture.Framebuffer;
import net.drinkybird.deferred.render.texture.FramebufferAttachmentType;
import net.drinkybird.deferred.render.texture.Renderbuffer;
import net.drinkybird.deferred.render.texture.Texture;
import net.drinkybird.deferred.render.texture.TextureFilter;
import net.drinkybird.deferred.render.texture.TextureFormat;
import net.drinkybird.deferred.render.texture.TextureLoader;
import net.drinkybird.deferred.render.texture.TextureTarget;
import net.drinkybird.deferred.util.TimeUtil;

import static org.lwjgl.opengl.ARBDirectStateAccess.*;

public class WorldRenderer {
    private static final int DEBUG_SCREEN_DOWNSCALE = 5;
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
    
    private final Game game;
    public final World world;
    
    private Framebuffer deferredFramebuffer;
    private Texture gbufferPosition, gbufferNormal, gbufferAlbedo;
    private Renderbuffer gbufferDepthStencil;
    private ShaderProgram deferredFirstPassShader = new ShaderProgram();
    private ShaderProgram deferredSecondPassShader = new ShaderProgram();
    private ShaderProgram debugUiShader = new ShaderProgram();
    private ShaderProgram debugPointShader = new ShaderProgram();
    
    private Mesh screenMesh;
    private Mesh debugScreenMesh;
    
    private Sky sky;
    
    public boolean debugCapture = false;
    
    private List<Light> lights = new ArrayList<>();
    private Mesh lightPointMesh = new Mesh(PrimitiveTopology.POINT_LIST, RenderConstants.MAX_LIGHTS, true);
    
    private Light test1, test2, test3;
    private DirectionalLight directionalLight = new DirectionalLight();
    
    private Frustum frustum = new Frustum();
    
    private float ambientLight = 0.9f;
    
    public Map<Chunk, ChunkRenderer> chunkRenderers = new HashMap<>();

    public WorldRenderer(final Game game, final World world) {
        this.game = game;
        this.world = world;
        this.world.renderer = this;
        
        createScreenMesh();
        createFramebuffer();
        
        deferredFirstPassShader.addShader(ShaderType.VERTEX, "/shaders/render.vert");
        deferredFirstPassShader.addShader(ShaderType.FRAGMENT, "/shaders/deferred.frag");
        deferredFirstPassShader.link();
        
        deferredSecondPassShader.addShader(ShaderType.VERTEX, "/shaders/screen.vert");
        deferredSecondPassShader.addShader(ShaderType.FRAGMENT, "/shaders/screen.frag");
        deferredSecondPassShader.link();
        
        debugUiShader.addShader(ShaderType.VERTEX, "/shaders/dbgui.vert");
        debugUiShader.addShader(ShaderType.FRAGMENT, "/shaders/dbgui.frag");
        debugUiShader.link();
        
        debugPointShader.addShader(ShaderType.VERTEX, "/shaders/dbgpoint.vert");
        debugPointShader.addShader(ShaderType.FRAGMENT, "/shaders/dbgpoint.frag");
        debugPointShader.link();
        
        lightPointMesh.vertices = new float[RenderConstants.MAX_LIGHTS * 3];
        lightPointMesh.colours = new float[RenderConstants.MAX_LIGHTS * 4];
        
        sky = new Sky();
        
        test1 = new Light(new Vector3f(0,2,0), new Vector3f(1,0,0), 4.0f);
        test2 = new Light(new Vector3f(16,2,16), new Vector3f(1,0,1), 32.0f);
        test3 = new Light(new Vector3f(16,2,16), new Vector3f(0,1,0), 8.0f);
        lights.add(test1);
        //lights.add(test2);
        //lights.add(test3);
        
        int n = 24;
        var rng = new Random();
        for (int i = 0; i < n; i++) {
            float r = (float)rng.nextInt(256) / 255.0f;
            float g = (float)rng.nextInt(256) / 255.0f;
            float b = (float)rng.nextInt(256) / 255.0f;
            lights.add(new Light(new Vector3f(0, 0, 0), new Vector3f(r, g, b), 16.0f));
        }
    }

    public void render() {
        glDisable(GL_MULTISAMPLE);
        
        test1.position.set(8.0f + (float)Math.sin(TimeUtil.getTimeInMs() / 1000.0) * 8.0f, 2.0f, 0.0f);
        test2.position.set(8.0f + (float)Math.cos(TimeUtil.getTimeInMs() / 1000.0) * 8.0f, 2.0f, 16.0f);
        test3.position.set(8f, (float)Math.sin(TimeUtil.getTimeInMs() / 1000.0) * 8.0f, 8f);
        for (int i = 0; i < lights.size(); i++) {
            var light = lights.get(i);
            float x = 8.0f + (float)Math.sin((TimeUtil.getTimeInMs() / 1000.0) + i) * 16.0f;
            float z = 8.0f + (float)Math.cos((TimeUtil.getTimeInMs() / 1000.0) + i) * 16.0f;
            light.position.set(x, 2.0f, z);
        }
        
        directionalLight.direction.x += 0.01f;
        while (directionalLight.direction.x > 1.0f) directionalLight.direction.x = -1.0f;
        //directionalLight.direction.normalize();
        
        test1.position.set(game.camera.centre);
        glViewport(0, 0, game.getWidth(), game.getHeight());
        glClearStencil(StencilMask.NOTHING.ordinal());
        glClear(GL_COLOR_BUFFER_BIT|GL_STENCIL_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        sky.render();
        
        GlState.enableDepthTest();
        
        // Render g-buffer
        {
            GlState.debugScopeEnter("Render g-buffer");
            
            GlState.bindFramebuffer(deferredFramebuffer);
            glViewport(0, 0, game.getWidth(), game.getHeight());
            glClearColor(1, 0, 0, 0);
            glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);
            
            GlState.bindProgram(deferredFirstPassShader);
            deferredFirstPassShader.uniform("u_projection", Game.instance.camera.getProjectionMatrix());
            deferredFirstPassShader.uniform("u_view", Game.instance.camera.getViewMatrix());
            deferredFirstPassShader.uniform("u_model", IDENTITY_MATRIX);
            
            GlState.enableCullFace();
            glCullFace(GL_BACK);
            
            renderWorld(deferredFirstPassShader, Game.instance.camera, true);
            
            GlState.debugScopeExit();
        }
        
        if (debugCapture) {
            gbufferAlbedo.save("debug_gbuffer_albedo.png", FramebufferAttachmentType.COLOUR, TextureFormat.RGBA);
            gbufferNormal.save("debug_gbuffer_normal.png", FramebufferAttachmentType.COLOUR, TextureFormat.RGBA);
            gbufferPosition.save("debug_gbuffer_position.png", FramebufferAttachmentType.COLOUR, TextureFormat.RGBA);
        }

        GlState.bindFramebuffer(null);
        GlState.disableDepthTest();
        GlState.disableBlend();
        GlState.disableCullFace();
        GlState.disableScissorTest();
        GlState.disableStencilTest();
        GlState.unbindTextures(TextureTarget.TEXTURE_2D, 0);
        
        GlState.debugScopeEnter("Deferred second pass");
        
        if (game.keys[GLFW.GLFW_KEY_F5] && ambientLight > 0.0f) ambientLight -= 0.01f; 
        if (game.keys[GLFW.GLFW_KEY_F6] && ambientLight < 1.0f) ambientLight += 0.01f; 
        
        GlState.bindProgram(deferredSecondPassShader);
        GlState.bindTexture(0, gbufferAlbedo);
        deferredSecondPassShader.uniform("u_gbufferAlbedo", 0);
        GlState.bindTexture(1, gbufferNormal);
        deferredSecondPassShader.uniform("u_gbufferNormal", 1);
        GlState.bindTexture(2, gbufferPosition);
        deferredSecondPassShader.uniform("u_gbufferPosition", 2);
        deferredSecondPassShader.uniform("u_cameraPos", Game.instance.camera.getAbsoluteEyePosition());
        deferredSecondPassShader.uniform("u_ambientLight", ambientLight);
        deferredSecondPassShader.uniform("u_directionalLightProjection", directionalLight.getProjectionMatrix());
        deferredSecondPassShader.uniform("u_directionalLightView", directionalLight.getViewMatrix());
        deferredSecondPassShader.uniform("u_directionalLightDirection", directionalLight.direction);
        deferredSecondPassShader.uniform("u_directionalLightColour", directionalLight.colour);
        deferredSecondPassShader.uniform("u_directionalLightIntensity", directionalLight.intensity);
        deferredSecondPassShader.uniform("u_renderDistance", Game.instance.camera.getViewRadius());
        deferredSecondPassShader.uniform("u_fogColour", 0.75f, 0.9f, 1.0f);
        
        int numLights = Math.min(lights.size(), RenderConstants.MAX_LIGHTS);
        deferredSecondPassShader.uniform("u_numLights", numLights);
        for (int i = 0; i < numLights; i++) {
            var light = lights.get(i);
            deferredSecondPassShader.uniform("u_lights[" + i + "].position", light.position);
            deferredSecondPassShader.uniform("u_lights[" + i + "].colour", light.colour);
            deferredSecondPassShader.uniform("u_lights[" + i + "].radius", light.radius);
        }

        {
            final int w = game.getWidth();
            final int h = game.getHeight();
            
            glBlitNamedFramebuffer(deferredFramebuffer.getName(), 0, 0, 0, w, h, 0, 0, w, h, GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT, GL_NEAREST);
        }
        
        GlState.enableBlend();
        screenMesh.draw();
        GlState.unbindTextures(TextureTarget.TEXTURE_2D, 0, 1, 2);
        
        GlState.debugScopeExit();
        
        GlState.debugScopeEnter("Render debug lights");
        
        GlState.enableDepthTest();
        debugDrawLights();
        GlState.disableDepthTest();
        
        GlState.debugScopeExit();
        
        if (Game.instance.keys[GLFW.GLFW_KEY_F3]) {
            GlState.debugScopeEnter("Render debug textures");
            
            int y = 0;
            debugDrawTexture(gbufferAlbedo, 0, y); y += Game.instance.getHeight() / DEBUG_SCREEN_DOWNSCALE;
            debugDrawTexture(gbufferNormal, 0, y); y += Game.instance.getHeight() / DEBUG_SCREEN_DOWNSCALE;
            debugDrawTexture(gbufferPosition, 0, y); y += Game.instance.getHeight() / DEBUG_SCREEN_DOWNSCALE;
            
            GlState.debugScopeExit();
        }
        
        debugCapture = false;
    }
    
    private void renderWorld(ShaderProgram program, Vector3fc eyePosition, Matrix4fc projection, Matrix4fc view, float radius, boolean queueChunks) {
        frustum.update(projection, view);
        
        Texture texture = TextureLoader.loadTexture("/textures/tiles.png", TextureFilter.NEAREST, TextureFilter.NEAREST);

        GlState.bindTexture(0, texture);
        
        int camx = (int)eyePosition.x();
        int camz = (int)eyePosition.z();
        
        int chunkRadius = (int)radius >> 4;

        int cxs = (camx / 16) - (int)chunkRadius - 1;
        int cxe = (camx / 16) + (int)chunkRadius;
        int czs = (camz / 16) - (int)chunkRadius - 1;
        int cze = (camz / 16) + (int)chunkRadius;
        
        var chunkList = new ArrayList<ChunkRenderer>();
        Vector3f pos = new Vector3f();
        
        for (int x = cxs; x < cxe; x++)
        for (int z = czs; z < cze; z++) {
            Chunk chunk = world.getChunk(x, z, queueChunks);
            if (chunk == null) {
                continue;
            }
            
            if (!frustum.chunkInFrustum(chunk)) {
                continue;
            }
            
            ChunkRenderer renderer = chunkRenderers.get(chunk);
            if (renderer == null) {
                renderer = new ChunkRenderer(chunk);
                chunkRenderers.put(chunk, renderer);
            }
            
            float chunkY = (chunk.getMinY() + chunk.getMaxY()) / 2.0f;
            pos.set(ChunkID.tileX(chunk.id) + 8.0f, chunkY, ChunkID.tileZ(chunk.id) + 8.0f);
            float dist = eyePosition.distance(pos);
            renderer.distance = dist;
            
            if (chunkList.isEmpty()) {
                chunkList.add(renderer);
            } else {
                int i;
                for (i = 0; i < chunkList.size(); i++) {
                    var re = chunkList.get(i);
                    if (re.distance > dist) {
                        break;
                    }
                }
                
                chunkList.add(i, renderer);
            }
        }
        
        for (ChunkRenderer renderer : chunkList) {
            final var chunk = renderer.chunk;
            
            Matrix4f model = new Matrix4f();
            model.translate(ChunkID.tileX(chunk.id), 0.0f, ChunkID.tileZ(chunk.id));
            program.uniform("u_model", model);
            renderer.render();
        }
    }
    
    private void renderWorld(ShaderProgram program, BaseCamera camera, boolean queueChunks) {
        renderWorld(program, camera.getEyePosition(), camera.getProjectionMatrix(), camera.getViewMatrix(), camera.getViewRadius(), queueChunks);
    }

    public void onViewportResize(int oldWidth, int oldHeight, int newWidth, int newHeight) {
        createFramebuffer();
    }
    
    private void createScreenMesh() {
        screenMesh = new Mesh(PrimitiveTopology.TRIANGLE_LIST, 6, false);
        screenMesh.vertices = new float[] { 
                -1.0f, -1.0f,  0.0f,
                -1.0f,  1.0f,  0.0f,
                 1.0f,  1.0f,  0.0f,
                 1.0f, -1.0f,  0.0f
        };
        screenMesh.elements = new short[] {
                0, 1, 2,
                2, 3, 0
        };
        screenMesh.texCoords = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
        };
        screenMesh.upload();
    }
    
    private void createFramebuffer() {
        gbufferPosition = new Texture(TextureTarget.TEXTURE_2D, game.getWidth(), game.getHeight(), TextureFilter.NEAREST, TextureFormat.RGBA_16F);
        gbufferNormal = new Texture(TextureTarget.TEXTURE_2D, game.getWidth(), game.getHeight(), TextureFilter.NEAREST, TextureFormat.RGBA_16F);
        gbufferAlbedo = new Texture(TextureTarget.TEXTURE_2D, game.getWidth(), game.getHeight(), TextureFilter.NEAREST, TextureFormat.RGBA_8);
        gbufferDepthStencil = new Renderbuffer(GL_DEPTH24_STENCIL8, game.getWidth(), game.getHeight());
        
        gbufferPosition.setLabel("G-buffer (position)");
        gbufferNormal.setLabel("G-buffer (normal)");
        gbufferAlbedo.setLabel("G-buffer (albedo)");
        gbufferDepthStencil.setLabel("G-buffer (depth/stencil)");
        
        deferredFramebuffer = new Framebuffer();
        deferredFramebuffer.setLabel("Deferred rendering framebuffer");
        deferredFramebuffer.attachTexure(FramebufferAttachmentType.COLOUR, 0, gbufferAlbedo);
        deferredFramebuffer.attachTexure(FramebufferAttachmentType.COLOUR, 1, gbufferNormal);
        deferredFramebuffer.attachTexure(FramebufferAttachmentType.COLOUR, 2, gbufferPosition);
        deferredFramebuffer.attachRenderbuffer(FramebufferAttachmentType.DEPTH_STENCIL, 0, gbufferDepthStencil);
        
        if (!deferredFramebuffer.isComplete()) {
            Game.fatalError("Deferred rendering framebuffer incomplete.");
        }
        
        int[] drawBuffers = new int[] {
                GL_COLOR_ATTACHMENT0,
                GL_COLOR_ATTACHMENT1,
                GL_COLOR_ATTACHMENT2
        };
        
        deferredFramebuffer.setDrawBuffers(drawBuffers);
        
        final float sw = (float)game.getWidth() / DEBUG_SCREEN_DOWNSCALE;
        final float sh = (float)game.getHeight() / DEBUG_SCREEN_DOWNSCALE;
        
        debugScreenMesh = new Mesh(PrimitiveTopology.TRIANGLE_LIST, 6, false);
        debugScreenMesh.vertices = new float[] {
                0.0f, 0.0f, 0.0f,
                0.0f, sh,   0.0f,
                sw,   sh,   0.0f,
                sw,   0.0f, 0.0f
        };
        debugScreenMesh.elements = new short[] {
                0, 1, 2,
                2, 3, 0
        };
        debugScreenMesh.texCoords = new float[] {
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        debugScreenMesh.upload();
        
        Logger.info("Deferred rendering framebuffer rebuilt at {}x{}", game.getWidth(), game.getHeight());
    }
    
    private void debugDrawTexture(Texture texture, int x, int y) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.ortho2D(0.0f, (float)game.getWidth(), (float)game.getHeight(), 0.0f);
        matrix4f.translate(new Vector3f(x, y, 0.0f));
        
        GlState.bindProgram(debugUiShader);
        debugUiShader.uniform("u_matrix", matrix4f);
        GlState.bindTexture(0, texture);
        debugScreenMesh.draw();
    }
    
    private void debugDrawLights() {
        Matrix4f modelView = Game.instance.camera.getViewMatrix();
        int numLights = Math.min(lights.size(), RenderConstants.MAX_LIGHTS);
        
        for (int i = 0; i < numLights; i++) {
            Light light = lights.get(i);
            
            lightPointMesh.vertices[i * 3 + 0] = light.position.x;
            lightPointMesh.vertices[i * 3 + 1] = light.position.y;
            lightPointMesh.vertices[i * 3 + 2] = light.position.z;
            lightPointMesh.colours[i * 4 + 0] = light.colour.x;
            lightPointMesh.colours[i * 4 + 1] = light.colour.y;
            lightPointMesh.colours[i * 4 + 2] = light.colour.z;
            lightPointMesh.colours[i * 4 + 3] = 1.0f;
        }
        
        lightPointMesh.upload();
        
        GlState.bindProgram(debugPointShader);
        debugPointShader.uniform("u_projection", Game.instance.camera.getProjectionMatrix());
        debugPointShader.uniform("u_modelView", modelView);
        glPointSize(3.0f);
        lightPointMesh.draw(numLights);
    }
}
