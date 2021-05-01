package net.drinkybird.deferred.render;

import org.tinylog.Logger;

import net.drinkybird.deferred.level.Chunk;
import net.drinkybird.deferred.render.mesh.Mesh;
import net.drinkybird.deferred.render.mesh.PrimitiveTopology;

public class ChunkRenderer {
    public final Chunk chunk;
    public final Mesh mesh;
    
    public float distance;
    
    public ChunkRenderer(final Chunk chunk) {
        this.chunk = chunk;
        
        int numVertices = (Chunk.CHUNK_SIZE + 1) * (Chunk.CHUNK_SIZE + 1) * 6;
        this.mesh = new Mesh(PrimitiveTopology.TRIANGLE_LIST, numVertices, false);
        generateMesh();
    }
    
    private void generateMesh() {
        mesh.vertices = new float[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * 4 * 3];
        mesh.texCoords = new float[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * 4 * 2];
        mesh.elements = new short[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * 6];
        
        int vi = 0;
        short ii = 0;
        int ti = 0;
        int ei = 0;
        
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++)
        for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
            int x0 = x;
            int x1 = x0 + 1;
            int z0 = z;
            int z1 = z0 + 1;
            float y00 = chunk.getHeightmap(x0, z0);
            float y01 = chunk.getHeightmap(x0, z1);
            float y11 = chunk.getHeightmap(x1, z1);
            float y10 = chunk.getHeightmap(x1, z0);
            float u0 = 16.0f / 256.0f;
            float u1 = u0 + (16.0f / 256.0f);
            float v0 = 16.0f / 256.0f;
            float v1 = v0 + (16.0f / 256.0f);
            
            short e00 = ii++;
            mesh.vertices[vi++] = x0;
            mesh.vertices[vi++] = y00;
            mesh.vertices[vi++] = z0;
            mesh.texCoords[ti++] = u0;
            mesh.texCoords[ti++] = v0;

            short e01 = ii++;
            mesh.vertices[vi++] = x0;
            mesh.vertices[vi++] = y01;
            mesh.vertices[vi++] = z1;
            mesh.texCoords[ti++] = u0;
            mesh.texCoords[ti++] = v1;

            short e11 = ii++;
            mesh.vertices[vi++] = x1;
            mesh.vertices[vi++] = y11;
            mesh.vertices[vi++] = z1;
            mesh.texCoords[ti++] = u1;
            mesh.texCoords[ti++] = v1;

            short e10 = ii++;
            mesh.vertices[vi++] = x1;
            mesh.vertices[vi++] = y10;
            mesh.vertices[vi++] = z0;
            mesh.texCoords[ti++] = u1;
            mesh.texCoords[ti++] = v0;
            
            mesh.elements[ei++] = e00;
            mesh.elements[ei++] = e01;
            mesh.elements[ei++] = e11;
            
            mesh.elements[ei++] = e11;
            mesh.elements[ei++] = e10;
            mesh.elements[ei++] = e00;
        }
        
        mesh.generateNormals();
        mesh.upload();
    }
    
    public void render() {
        mesh.draw();
    }
}
