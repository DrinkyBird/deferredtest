package net.drinkybird.deferred.level;

import static org.lwjgl.opengl.GL33C.*;
import java.util.Random;

import net.drinkybird.deferred.level.noise.CombinedNoise;
import net.drinkybird.deferred.level.noise.Noise;
import net.drinkybird.deferred.level.noise.OctaveNoise;

import static org.lwjgl.opengl.ARBDirectStateAccess.*;

public class Chunk {
	public static final int CHUNK_SHIFT = 4;
    public static final int CHUNK_SIZE = 1 << 4;
    
    private final World world;
    public final long id;
    private float[] heightmap = new float[(CHUNK_SIZE + 1) * (CHUNK_SIZE + 1)];
    
    private byte[] tiles = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
    private byte[] tileShapes = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
    
    private float minY = 9999.0f, maxY = -9999.0f;
    
    public boolean unloaded = false;
    
    public Chunk(final World world, final long id) {
        this.world = world;
        this.id = id;
        
        Random rngRandom = new Random(world.seed);
        Noise noise = new CombinedNoise(new OctaveNoise(rngRandom, 2), new OctaveNoise(rngRandom, 4));
        
        for (int x = 0; x < CHUNK_SIZE; x++)
        for (int z = 0; z < CHUNK_SIZE; z++) {
            int x0 = x;
            int x1 = x0 + 1;
            int z0 = z;
            int z1 = z0 + 1;
            int ax0 = ChunkID.tileX(id) + x;
            int ax1 = ax0 + 1;
            int az0 = ChunkID.tileZ(id) + z;
            int az1 = az0 + 1;

            heightmap[x0 + z0 * (CHUNK_SIZE + 1)] = noise.compute(ax0 / 10.0f, az0 / 10.0f) * 2.0f;
            heightmap[x1 + z0 * (CHUNK_SIZE + 1)] = noise.compute(ax1 / 10.0f, az0 / 10.0f) * 2.0f;
            heightmap[x1 + z1 * (CHUNK_SIZE + 1)] = noise.compute(ax1 / 10.0f, az1 / 10.0f) * 2.0f;
            heightmap[x0 + z1 * (CHUNK_SIZE + 1)] = noise.compute(ax0 / 10.0f, az1 / 10.0f) * 2.0f;

            //heightmap[x0 + z0 * (CHUNK_SIZE + 1)] = (float)Math.sin((float)az0 / 10.0f);
            //heightmap[x1 + z0 * (CHUNK_SIZE + 1)] = (float)Math.sin((float)az0 / 10.0f);
            //heightmap[x1 + z1 * (CHUNK_SIZE + 1)] = (float)Math.sin((float)az1 / 10.0f);
            //heightmap[x0 + z1 * (CHUNK_SIZE + 1)] = (float)Math.sin((float)az1 / 10.0f);
        }
        
        for (int i = 0; i < heightmap.length; i++)  {
            minY = Math.min(minY, heightmap[i]);
            maxY = Math.max(maxY, heightmap[i]);
        }
    }
    
    public float getHeightmap(int x, int z) {
        return heightmap[x + z * (CHUNK_SIZE + 1)];
    }
    
    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }
}
