package net.drinkybird.deferred.render.shader;

import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.KHRDebug.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joml.Matrix2fc;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.joml.Vector4i;
import org.joml.Vector4ic;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import net.drinkybird.deferred.Game;
import net.drinkybird.deferred.core.NativeResource;
import net.drinkybird.deferred.render.GlObject;
import net.drinkybird.deferred.render.RenderConstants;

public class ShaderProgram extends NativeResource implements GlObject {
    private int program;
    private boolean linked = false;
    
    private static String globalSource = null;
    
    private List<Integer> shaders = new ArrayList<>();
    
    public ShaderProgram() {
        this.program = glCreateProgram();
    }
    
    @Override
    public void destroy() {
        destroyShaders();
        glDeleteProgram(program);
    }

    private void destroyShaders() {
        for (int shader : shaders) {
            glDetachShader(program, shader);
            glDeleteShader(shader);
        }
        
        shaders.clear();
    }
    
    public void addShader(ShaderType type, String path) {
        try (InputStream stream = ShaderProgram.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new FileNotFoundException(path);
            }
            
            String global = getGlobalSource();
            String source = IOUtils.toString(stream, StandardCharsets.UTF_8);
            
            Logger.info("Compiling {} shader: {}", type, path);
            
            int shader = glCreateShader(type.value);
            glShaderSource(shader, global, source);
            glCompileShader(shader);
            
            int status = glGetShaderi(shader, GL_COMPILE_STATUS);
            if (status == 0) {
                String msg = glGetShaderInfoLog(shader);
                throw new RuntimeException(msg);
            }
            
            glAttachShader(program, shader);
            shaders.add(shader);
        } catch (IOException e) {
            Game.handleException(new RuntimeException("Failed to load shader " + path, e));
        }
    }
    
    private static String getGlobalSource() throws IOException {
        if (globalSource == null) {
            try {
                StringBuffer buffer = new StringBuffer();
                buffer.append("#version ");
                buffer.append(RenderConstants.GLSL_VERSION);
                buffer.append('\n');
                
                var fields = RenderConstants.class.getDeclaredFields();
                for (var field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                        var type = field.getType();
                        String glslType;
                        String glslValue;
                        if (type == Float.class || type == float.class) {
                            float value = (float)field.get(null);
                            glslType = "float";
                            glslValue = String.format("%.15fF", value);
                        } else if (type == Integer.class || type == int.class) {
                            int value = (int)field.get(null);
                            glslType = "int";
                            glslValue = Integer.toString(value);
                        } else if (type == Boolean.class || type == boolean.class) {
                            boolean value = (boolean)field.get(null);
                            glslType = "bool";
                            glslValue = Boolean.toString(value);
                        } else if (type.isInstance(Vector2fc.class) || type == Vector2f.class) {
                            Vector2fc value = (Vector2fc)field.get(null);
                            glslType = "vec2";
                            glslValue = String.format("vec2(%.15fF, %.15fF)", value.x(), value.y());
                        } else if (type.isInstance(Vector2ic.class) || type == Vector2i.class) {
                            Vector2ic value = (Vector2ic)field.get(null);
                            glslType = "ivec2";
                            glslValue = String.format("ivec2(%d, %d)", value.x(), value.y());
                        } else if (type.isInstance(Vector3fc.class) || type == Vector3f.class) {
                            Vector3fc value = (Vector3fc)field.get(null);
                            glslType = "vec3";
                            glslValue = String.format("vec3(%.15fF, %.15fF, %.15fF)", value.x(), value.y(), value.z());
                        } else if (type.isInstance(Vector3ic.class) || type == Vector3i.class) {
                            Vector3ic value = (Vector3ic)field.get(null);
                            glslType = "ivec3";
                            glslValue = String.format("ivec3(%d, %d, %d)", value.x(), value.y(), value.z());
                        } else if (type.isInstance(Vector4fc.class) || type == Vector4f.class) {
                            Vector4fc value = (Vector4fc)field.get(null);
                            glslType = "vec4";
                            glslValue = String.format("vec4(%.15fF, %.15fF, %.15fF, %.15fF)", value.x(), value.y(), value.z(), value.w());
                        } else if (type.isInstance(Vector4ic.class) || type == Vector4i.class) {
                            Vector4ic value = (Vector4ic)field.get(null);
                            glslType = "ivec4";
                            glslValue = String.format("ivec4(%d, %d, %d, %d)", value.x(), value.y(), value.z(), value.w());
                        } else {
                            continue;
                        }
                        
                        buffer.append("const ");
                        buffer.append(glslType);
                        buffer.append(' ');
                        buffer.append(field.getName());
                        buffer.append(" = ");
                        buffer.append(glslValue);
                        buffer.append(";\n");
                    }
                }
                
                globalSource = buffer.toString();
                Logger.info(globalSource);
            } catch (IllegalAccessException e) {
                Game.handleException(e);
            }
        }
        
        return globalSource;
    }
    
    public void link() {
        glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);
        
        if (status == 0) {
            String msg = glGetProgramInfoLog(program);
            Game.fatalError("Error linking program: " + msg);
        }
        
        destroyShaders();
        
        linked = true;
    }

    @Override
    public int getName() {
        return program;
    }

    @Override
    public void setLabel(String label) {
        glObjectLabel(GL_PROGRAM, program, label);
    }
    
    public boolean isLinked() {
        return linked;
    }
    
    private int getUniformLocation(String name) {
        return glGetUniformLocation(program, name);
    }
    
    public void uniform(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }
    
    public void uniform(String name, float value) {
        glUniform1f(getUniformLocation(name), value);
    }
    
    public void uniform(String name, Vector2fc value) {
        glUniform2f(getUniformLocation(name), value.x(), value.y());
    }
    
    public void uniform(String name, float x, float y) {
        glUniform2f(getUniformLocation(name), x, y);
    }
    
    public void uniform(String name, Vector2ic value) {
        glUniform2i(getUniformLocation(name), value.x(), value.y());
    }
    
    public void uniform(String name, int x, int y) {
        glUniform2i(getUniformLocation(name), x, y);
    }
    
    public void uniform(String name, Vector3fc value) {
        glUniform3f(getUniformLocation(name), value.x(), value.y(), value.z());
    }
    
    public void uniform(String name, float x, float y, float z) {
        glUniform3f(getUniformLocation(name), x, y, z);
    }
    
    public void uniform(String name, Vector3ic value) {
        glUniform3i(getUniformLocation(name), value.x(), value.y(), value.z());
    }
    
    public void uniform(String name, int x, int y, int z) {
        glUniform3i(getUniformLocation(name), x, y, z);
    }
    
    public void uniform(String name, Vector4fc value) {
        glUniform4f(getUniformLocation(name), value.x(), value.y(), value.z(), value.w());
    }
    
    public void uniform(String name, float x, float y, float z, float w) {
        glUniform4f(getUniformLocation(name), x, y, z, w);
    }
    
    public void uniform(String name, Vector4ic value) {
        glUniform4i(getUniformLocation(name), value.x(), value.y(), value.z(), value.w());
    }
    
    public void uniform(String name, int x, int y, int z, int w) {
        glUniform4i(getUniformLocation(name), x, y, z, w);
    }
    
    public void uniform(String name, Matrix2fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4);
            matrix.get(buffer);
            glUniformMatrix2fv(getUniformLocation(name), false, buffer);
        }
    }
    
    public void uniform(String name, Matrix3fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(9);
            matrix.get(buffer);
            glUniformMatrix3fv(getUniformLocation(name), false, buffer);
        }
    }
    
    public void uniform(String name, Matrix4fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(getUniformLocation(name), false, buffer);
        }
    }
}
