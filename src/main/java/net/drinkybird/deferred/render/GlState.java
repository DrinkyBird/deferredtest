package net.drinkybird.deferred.render;

import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.KHRDebug.*;

import java.util.Random;
import java.util.Stack;

import org.lwjgl.opengl.KHRDebug;

import net.drinkybird.deferred.Game;
import net.drinkybird.deferred.render.shader.ShaderProgram;
import net.drinkybird.deferred.render.texture.Framebuffer;
import net.drinkybird.deferred.render.texture.Texture;
import net.drinkybird.deferred.render.texture.TextureFormat;
import net.drinkybird.deferred.render.texture.TextureTarget;

public class GlState {
    private boolean blend = false;
    private boolean cullFace = false;
    private boolean depthTest = false;
    private boolean scissorTest = false;
    private boolean stencilTest = false;
    private static Random debugRandom = new Random();

    public GlState() { }

    public GlState(GlState other) {
        this.blend = other.blend;
        this.cullFace = other.cullFace;
        this.depthTest = other.depthTest;
        this.scissorTest = other.scissorTest;
        this.stencilTest = other.stencilTest;
    }

    public void apply() {
        if (this.blend) {
            glEnable(GL_BLEND);
        } else {
            glDisable(GL_BLEND);
        }

        if (this.cullFace) {
            glEnable(GL_CULL_FACE);
        } else {
            glDisable(GL_CULL_FACE);
        }

        if (this.depthTest) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        if (this.scissorTest) {
            glEnable(GL_SCISSOR_TEST);
        } else {
            glDisable(GL_SCISSOR_TEST);
        }

        if (this.stencilTest) {
            glEnable(GL_STENCIL_TEST);
        } else {
            glDisable(GL_STENCIL_TEST);
        }

    }

    public void apply(GlState other) {
        if (this.blend && !other.blend) {
            glEnable(GL_BLEND);
        } else if (other.blend) {
            glDisable(GL_BLEND);
        }

        if (this.cullFace && !other.cullFace) {
            glEnable(GL_CULL_FACE);
        } else if (other.cullFace) {
            glDisable(GL_CULL_FACE);
        }

        if (this.depthTest && !other.depthTest) {
            glEnable(GL_DEPTH_TEST);
        } else if (other.depthTest) {
            glDisable(GL_DEPTH_TEST);
        }

        if (this.scissorTest && !other.scissorTest) {
            glEnable(GL_SCISSOR_TEST);
        } else if (other.scissorTest) {
            glDisable(GL_SCISSOR_TEST);
        }

        if (this.stencilTest && !other.stencilTest) {
            glEnable(GL_STENCIL_TEST);
        } else if (other.stencilTest) {
            glDisable(GL_STENCIL_TEST);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GlState other) {
            return
                this.blend == other.blend &&
                this.cullFace == other.cullFace &&
                this.depthTest == other.depthTest &&
                this.scissorTest == other.scissorTest &&
                this.stencilTest == other.stencilTest;
        }

        return false;
    }

    private static GlState current;
    private static Stack<GlState> stack = new Stack<GlState>();

    static {
        current = new GlState();
        push();
        current.apply();
    }

    public static void push() {
        stack.push(current);
        current = new GlState(current);
    }

    public static void pop() {
        GlState next = stack.pop();
        next.apply(current);
        current = next;
    }

    public static void enableBlend() {
        if (!current.blend) {
            current.blend = true;
            glEnable(GL_BLEND);
        }
    }

    public static void disableBlend() {
        if (current.blend) {
            current.blend = false;
            glDisable(GL_BLEND);
        }
    }

    public static void enableCullFace() {
        if (!current.cullFace) {
            current.cullFace = true;
            glEnable(GL_CULL_FACE);
        }
    }

    public static void disableCullFace() {
        if (current.cullFace) {
            current.cullFace = false;
            glDisable(GL_CULL_FACE);
        }
    }

    public static void enableDepthTest() {
        if (!current.depthTest) {
            current.depthTest = true;
            glEnable(GL_DEPTH_TEST);
        }
    }

    public static void disableDepthTest() {
        if (current.depthTest) {
            current.depthTest = false;
            glDisable(GL_DEPTH_TEST);
        }
    }

    public static void enableScissorTest() {
        if (!current.scissorTest) {
            current.scissorTest = true;
            glEnable(GL_SCISSOR_TEST);
        }
    }

    public static void disableScissorTest() {
        if (current.scissorTest) {
            current.scissorTest = false;
            glDisable(GL_SCISSOR_TEST);
        }
    }

    public static void enableStencilTest() {
        if (!current.stencilTest) {
            current.stencilTest = true;
            glEnable(GL_STENCIL_TEST);
        }
    }

    public static void disableStencilTest() {
        if (current.stencilTest) {
            current.stencilTest = false;
            glDisable(GL_STENCIL_TEST);
        }
    }

    public static void checkError(String message) {
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            Game.fatalError("OpenGL error (%d): %s", error, message);
        }
    }
    
    public static void bindFramebuffer(Framebuffer framebuffer) {
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer == null ? 0 : framebuffer.getName());
    }
    
    public static void bindTexture(int slot, Texture texture) {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(texture.getTarget().value, texture.getName());
    }
    
    public static void unbindTexture(TextureTarget target, int slot) {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(target.value, 0);
    }
    
    public static void unbindTextures(TextureTarget target, int... slots) {
        for (int slot : slots) {
            glActiveTexture(GL_TEXTURE0 + slot);
            glBindTexture(target.value, 0);
        }
    }
    
    public static void bindProgram(ShaderProgram program) {
        glUseProgram(program == null ? 0 : program.getName());
    }
    
    public static void setStencilMask(StencilMask mask) {
        glStencilMask((byte)(mask.ordinal() & 0xFF));
        glStencilMask((byte)(mask.ordinal()));
    }
    
    public static void debugMarker(String text) {
        glDebugMessageInsert(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_MARKER, debugRandom.nextInt(), GL_DEBUG_SEVERITY_NOTIFICATION, text);
    }
    
    public static void debugMarker(String fmt, Object... args) {
        debugMarker(fmt.formatted(args));
    }
    
    public static void debugScopeEnter(String text) {
        glPushDebugGroup(GL_DEBUG_SOURCE_APPLICATION, debugRandom.nextInt(), text);
    }
    
    public static void debugScopeEnter(String fmt, Object... args) {
        debugScopeEnter(fmt.formatted(args));
    }
    
    public static void debugScopeExit() {
        glPopDebugGroup();
    }
}

