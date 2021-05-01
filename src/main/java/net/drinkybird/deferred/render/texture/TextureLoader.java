package net.drinkybird.deferred.render.texture;

import static org.lwjgl.stb.STBImage.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import net.drinkybird.deferred.Game;

public class TextureLoader {
    private record CacheKey(String path, TextureFilter minFilter, TextureFilter magFilter) {}
    
    private static Map<CacheKey, Texture> cache = new HashMap<>();
    
    public static Texture loadTexture(String path, TextureFilter minFilter, TextureFilter magFilter) {
        CacheKey key = new CacheKey(path, minFilter, magFilter);
        
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        
        try (InputStream stream = TextureLoader.class.getResourceAsStream(path);
                MemoryStack stack = MemoryStack.stackPush()) {
            if (stream == null) {
                throw new FileNotFoundException(path);
            }
            
            byte[] byteArray = IOUtils.toByteArray(stream);
            ByteBuffer buffer = MemoryUtil.memAlloc(byteArray.length);
            buffer.put(byteArray);
            buffer.flip();
            
            IntBuffer wb = stack.mallocInt(1);
            IntBuffer hb = stack.mallocInt(1);
            IntBuffer nb = stack.mallocInt(1);
            
            ByteBuffer pixels = stbi_load_from_memory(buffer, wb, hb, nb, 0);
            
            MemoryUtil.memFree(buffer);
            
            if (pixels == null) {
                Game.fatalError("Failed to load image: %s: %s", path, stbi_failure_reason());
            }
            
            int width = wb.get(0);
            int height = hb.get(0);
            int numChannels = nb.get(0);
            
            TextureFormat internalFormat = (numChannels == 3 ? TextureFormat.RGB_8 : TextureFormat.RGBA_8);
            TextureFormat format = (numChannels == 3 ? TextureFormat.RGB : TextureFormat.RGBA);
            
            Texture texture = new Texture(TextureTarget.TEXTURE_2D, width, height, minFilter, magFilter, internalFormat, format, pixels);
            cache.put(key, texture);
            
            stbi_image_free(pixels);
            
            return texture;
        } catch (IOException ex) {
            Game.handleException(ex);
        }
        
        return null;
    }
}
