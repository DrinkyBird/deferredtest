package net.drinkybird.deferred.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.tinylog.Logger;

import net.drinkybird.deferred.Game;
import net.drinkybird.deferred.render.BaseCamera;
import net.drinkybird.deferred.render.WorldRenderer;

public class World {
    public Chunk[] chunks = new Chunk[4096];
    private List<Long> chunkLoadQueue = new ArrayList<>();
    
    public final long seed = System.nanoTime();
    public final Random random = new Random();
    
    public WorldRenderer renderer = null;
    
    public World() {
        
    }
    
    public void tick(BaseCamera camera) {
        unloadChunks(camera);
        sortLoadQueue(camera);
        doChunkLoad();
    }
    
    public Chunk findChunk(int x, int z) {
        return findChunk(ChunkID.encode(x, (short)0, z));
    }
    
    public Chunk findChunk(long id) {
        for (int i = 0; i < chunks.length; i++) {
            Chunk chunk = chunks[i];
            if (chunk == null) {
                continue;
            }
            
            if (chunk.id == id) {
                return chunk;
            }
        }
        
        return null;
    }
    
    public Chunk getChunk(int x, int z, boolean queueForLoad) {
        Chunk chunk = findChunk(x, z);
        if (chunk == null) {
            long id = ChunkID.encode(x, (short)0, z);
            
            if (queueForLoad && !isChunkQueued(id)) {
                chunkLoadQueue.add(id);
            }
            
            return null;
        }
        
        return chunk;
    }
    
    private boolean isChunkQueued(long id) {
        for (long queuedId : chunkLoadQueue) {
            if (queuedId == id) {
                return true;
            }
        }
        
        return false;
    }
    
    private void sortLoadQueue(BaseCamera camera) {
        if (chunkLoadQueue.isEmpty()) {
            return;
        }
        
        List<Long> toRemove = new ArrayList<>();
        
        Vector3f chunkPos = new Vector3f();
        var eye = camera.getEyePosition();
        
        for (long pos : chunkLoadQueue) {
            chunkPos.set(ChunkID.tileX(pos) + (Chunk.CHUNK_SIZE / 2.0f), eye.y, ChunkID.tileZ(pos) + (Chunk.CHUNK_SIZE / 2.0f));
            if (chunkPos.distance(eye) > camera.getViewRadius()) {
                toRemove.add(pos);
            }
        }
        
        for (long pos : toRemove) {
            chunkLoadQueue.remove(pos);
        }
        
        chunkLoadQueue.sort((a, b) -> {
            chunkPos.set(ChunkID.tileX(a) + (Chunk.CHUNK_SIZE / 2.0f), eye.y, ChunkID.tileZ(a) + (Chunk.CHUNK_SIZE / 2.0f));
            float distA = chunkPos.distance(eye);
            chunkPos.set(ChunkID.tileX(b) + (Chunk.CHUNK_SIZE / 2.0f), eye.y, ChunkID.tileZ(b) + (Chunk.CHUNK_SIZE / 2.0f));
            float distB = chunkPos.distance(eye);
            
            if (distA > distB) {
                return 1;
            } else if (distA < distB) {
                return -1;
            } else {
                return 0;
            }
        });
    }
    
    private void doChunkLoad() {
        if (chunkLoadQueue.isEmpty()) {
            return;
        }
        
        long pos = chunkLoadQueue.get(0);
        chunkLoadQueue.remove(0);
        if (findChunk(pos) != null) {
            return;
        }
        
        //Logger.info("Load chunk {}, {}", pos.x(), pos.z());
        int index = -1;
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i] == null) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            Game.fatalError("Out of chunk space");
        }
        
        chunks[index] = new Chunk(this, pos);
    }
    
    private void unloadChunks(BaseCamera camera) {
        Vector3f chunkPos = new Vector3f();
        var eye = camera.getEyePosition();
        float y = eye.y;
        
        for (int i = 0; i < chunks.length; i++) {
            Chunk chunk = chunks[i];
            if (chunk == null) {
                continue;
            }
            
            chunkPos.set(ChunkID.tileX(chunk.id) + (Chunk.CHUNK_SIZE / 2.0f), y, ChunkID.tileZ(chunk.id) + (Chunk.CHUNK_SIZE / 2.0f));
            
            if (chunkPos.distance(eye) > camera.getViewRadius()) {
                chunks[i].unloaded = true;
                if (renderer != null) {
                    renderer.chunkRenderers.remove(chunk);
                }
                chunks[i] = null;
            }
        }
    }
}
