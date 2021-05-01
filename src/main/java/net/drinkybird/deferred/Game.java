package net.drinkybird.deferred;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.KHRDebug.*;

import java.awt.Color;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import net.drinkybird.deferred.core.NativeResourceManager;
import net.drinkybird.deferred.level.ChunkID;
import net.drinkybird.deferred.level.World;
import net.drinkybird.deferred.render.Camera;
import net.drinkybird.deferred.render.GlState;
import net.drinkybird.deferred.render.WorldRenderer;
import net.drinkybird.deferred.render.shader.ShaderProgram;
import net.drinkybird.deferred.render.shader.ShaderType;
import net.drinkybird.deferred.util.ExceptionUtil;
import net.drinkybird.deferred.util.TimeUtil;

public class Game {
	public static Game instance = null;
	
	private final String[] args;
    private CommandLine commandLine;
	
	private long window = 0L;
	
	private int fps, fpsFrames = 0;
	private double lastFpsTime = 0.0;
	private float delta = 0.0f;
	
	private World world;
	private WorldRenderer worldRenderer;
	
	private int width = 1280, height = 720;
    
    public Camera camera = new Camera();
    
    public boolean[] lastKeys = new boolean[512];
    public boolean[] keys = new boolean[512];
	
	public static void main(String args[]) {
        org.lwjgl.system.Configuration.DEBUG.set(true);
        
        for (var entry : System.getProperties().entrySet()) {
			Logger.info("{} -> {}", entry.getKey(), entry.getValue());
		}
	    
		try {
            UIManager.put("control", new Color(0xFFFFFF));
            UIManager.put("nimbusBase", new Color(0x3399FF));
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
		
		long id = ChunkID.encode(1234567, (short)25996, -7891233);
		Logger.info("id = {}\n", String.format("%02x", id));
		Logger.info("x = {}", ChunkID.decodeX(id));
		Logger.info("y = {}", ChunkID.decodeY(id));
		Logger.info("z = {}", ChunkID.decodeZ(id));
		
		var g = new Game(args);
		g.init();
		g.run();
		g.shutdown();
	}
	
	public Game(String[] args) {
		instance = this;
		this.args = args;
		
		parseOptions();
	}
    
    private void parseOptions() {
        try {
            Options options = new Options();
            options.addOption(new Option(null, "assets", true, "Asset directory path"));
            
            CommandLineParser parser = new DefaultParser();
            commandLine = parser.parse(options, args);
            
            if (!commandLine.hasOption("--assets")) {
                fatalError("--assets not specified");
            }
        } catch (ParseException e) {
            handleException(e);
        }
    }
	
	private void init() {
	    glfwSetErrorCallback((code, msg) -> {
	        String description = MemoryUtil.memASCII(msg);
	        fatalError("GLFW error (%d): %s", code, description);
	    });

		if (!glfwInit()) {
			fatalError("Failed to initialise GLFW.");
		}
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwWindowHint(GLFW_RED_BITS, 8);
		glfwWindowHint(GLFW_GREEN_BITS, 8);
		glfwWindowHint(GLFW_BLUE_BITS, 8);
		glfwWindowHint(GLFW_ALPHA_BITS, 8);
		glfwWindowHint(GLFW_DEPTH_BITS, 24);
        glfwWindowHint(GLFW_STENCIL_BITS, 8);
		
		long monitor = glfwGetPrimaryMonitor();
		window = glfwCreateWindow(width, height, "Game", 0L, 0L);
		if (window == 0L) {
			fatalError("Failed to create window.");
		}
		
		{
		    GLFWVidMode vidMode = glfwGetVideoMode(monitor);
		    int x = (vidMode.width() / 2) - (width / 2);
		    int y = (vidMode.height() / 2) - (height / 2);
		    glfwSetWindowPos(window, x, y);
		}
        
        {
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            //glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        }

		glfwMakeContextCurrent(window);
		GLCapabilities capabilities = GL.createCapabilities();
		if (!capabilities.GL_ARB_direct_state_access) {
		    fatalError("Your GPU does not support the GL_ARB_direct_state_access extension");
		}
		
		GLUtil.setupDebugMessageCallback();
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_ERROR, GL_DONT_CARE, (IntBuffer)null, true);
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR, GL_DONT_CARE, (IntBuffer)null, true);
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR, GL_DONT_CARE, (IntBuffer)null, true);
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_PORTABILITY, GL_DONT_CARE, (IntBuffer)null, true);
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_PERFORMANCE, GL_DONT_CARE, (IntBuffer)null, true);
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_MARKER, GL_DONT_CARE, (IntBuffer)null, false);
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_PUSH_GROUP, GL_DONT_CARE, (IntBuffer)null, false);
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_POP_GROUP, GL_DONT_CARE, (IntBuffer)null, false);
        glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_OTHER , GL_DONT_CARE, (IntBuffer)null, true);
		
		glfwSetWindowSizeCallback(window, (w, newWidth, newHeight) -> {
		    int oldWidth = width;
		    int oldHeight = height;
            width = newWidth;
            height = newHeight;
            
			Logger.info("Window resize: {} * {}", newWidth, newHeight);
			worldRenderer.onViewportResize(oldWidth, oldHeight, newWidth, newHeight);
			
			// clean up now-gone GL objects
			System.gc();
		});
		
		glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            System.arraycopy(keys, 0, lastKeys, 0, keys.length);
            keys[key] = (action != GLFW_RELEASE);
		});

		world = new World();
		worldRenderer = new WorldRenderer(this, world);
	}
	
	private void run() {
		glfwShowWindow(window);
		
		while (!glfwWindowShouldClose(window)) {
		    double start = TimeUtil.getTimeInMs();
            
            if (keys[GLFW_KEY_F11] && !lastKeys[GLFW_KEY_F11]) {
                worldRenderer.debugCapture = true;
            }
		    
		    camera.tick();
		    world.tick(camera);
		    worldRenderer.render();
		    
			glfwSwapBuffers(window);
			glfwPollEvents();
			
			NativeResourceManager.cleanup();
			
			double end = TimeUtil.getTimeInMs();
			delta = (float)(end - start);
            glfwSetWindowTitle(window, String.format("%.4f ms / %d FPS", delta, fps));
			fpsFrames++;
			if (end - lastFpsTime >= 1000.0) {
			    fps = fpsFrames;
			    fpsFrames = 0;
			    lastFpsTime = end;
			    
			    Logger.info("FPS: {}", fps);
			}
		}
	}
	
	private void shutdown() {
		glfwDestroyWindow(window);
		glfwTerminate();
	}
	
    public int getFps() {
        return fps;
    }
    
    public float getDelta() {
        return delta;
    }

    public World getWorld() {
        return world;
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
	
	public static void fatalError(String msg) {
		if (instance != null) {
			if (instance.window != 0L) {
				glfwHideWindow(instance.window);
			}
		}
		
		JOptionPane.showMessageDialog(null, msg, "Fatal Error", JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}
	
	public static void fatalError(String fmt, Object... args) {
		fatalError(String.format(fmt, args));
	}
	
	public static void handleException(Throwable t) {
		t.printStackTrace();
		fatalError(ExceptionUtil.formatThrowable(t));
	}
}
