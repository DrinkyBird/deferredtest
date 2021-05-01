package net.drinkybird.deferred.render.mesh;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33C.*;

import java.io.Serializable;

import org.joml.Vector3f;

import net.drinkybird.deferred.Game;
import net.drinkybird.deferred.core.NativeResource;

public class Mesh extends NativeResource implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public float[] vertices = null;
    public short[] elements = null;
    public float[] normals = null;
    public float[] texCoords = null;
    public float[] colours = null;
    
    public final PrimitiveTopology topology;
    public final int numVertices;
    public boolean dynamic;
    
    private int vao, vbo, ebo;
    private long lastBufferSize = -1;
    
    public Mesh(final PrimitiveTopology topology, final int numVertices, boolean dynamic) {
        this.topology = topology;
        this.numVertices = numVertices;
        this.dynamic = dynamic;
        
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
    }
    
    @Override
    public void destroy() {
        glDeleteBuffers(ebo);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
    
    public void upload() {
        if (vertices == null) {
            Game.fatalError("vertices cannot be null");
        }
        
        final int usage = dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW;
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        long bufferSize = vertices.length;
        if (normals != null) bufferSize += normals.length;
        if (texCoords != null) bufferSize += texCoords.length;
        if (colours != null) bufferSize += colours.length;
        bufferSize *= Float.BYTES;
        
        if (bufferSize != lastBufferSize) {
            glBufferData(GL_ARRAY_BUFFER, bufferSize, usage);
        }
        this.lastBufferSize = bufferSize;
        
        long off = 0;
        glBufferSubData(GL_ARRAY_BUFFER, off * Float.BYTES, vertices); off += vertices.length;
        if (normals != null) {
            glBufferSubData(GL_ARRAY_BUFFER, off * Float.BYTES, normals); off += normals.length;
        }
        if (texCoords != null) {
            glBufferSubData(GL_ARRAY_BUFFER, off * Float.BYTES, texCoords); off += texCoords.length;
        }
        if (colours != null) {
            glBufferSubData(GL_ARRAY_BUFFER, off * Float.BYTES, colours); off += colours.length;
        }
        
        if (elements != null) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, elements, usage);
        }
        
        off = 0;
        int index = 0;
        
        glVertexAttribPointer(index, 3, GL_FLOAT, false, 3 * Float.BYTES, off * Float.BYTES); off += vertices.length;
        glEnableVertexAttribArray(index);
        index++;
        
        if (normals != null) {
            glVertexAttribPointer(index, 3, GL_FLOAT, false, 3 * Float.BYTES, off * Float.BYTES); off += normals.length;
            glEnableVertexAttribArray(index);
            index++;
        }
        
        if (texCoords != null) {
            glVertexAttribPointer(index, 2, GL_FLOAT, false, 2 * Float.BYTES, off * Float.BYTES); off += texCoords.length;
            glEnableVertexAttribArray(index);
            index++;
        }
        
        if (colours != null) {
            glVertexAttribPointer(index, 4, GL_FLOAT, false, 4 * Float.BYTES, off * Float.BYTES); off += colours.length;
            glEnableVertexAttribArray(index);
            index++;
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    public void draw(int numVertices) {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        if (elements != null) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glDrawElements(topology.value, numVertices, GL_UNSIGNED_SHORT, 0L);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        } else {
            glDrawArrays(topology.value, 0, numVertices);
        }
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    public void draw() {
        this.draw(this.numVertices);
    }
    
    public void generateNormals() {
        if (topology != PrimitiveTopology.TRIANGLE_LIST) {
            Game.fatalError("Can only generate normals for triange lists");
        }
        
        final int totalVerts = (elements != null ? elements.length : vertices.length);
        
        normals = new float[vertices.length]; 
        
        Vector3f a = new Vector3f(), b = new Vector3f(), c = new Vector3f();
        Vector3f bsa = new Vector3f(), csa = new Vector3f();
        Vector3f n = new Vector3f();
        
        if (elements != null) {
            for (int i = 0; i < normals.length; i++) {
                normals[i] = 0.0f;
            }
            
            for (int i = 0; i < elements.length; i += 3) {
                a.set(vertices[elements[i + 0] * 3 + 0], vertices[elements[i + 0] * 3 + 1], vertices[elements[i + 0] * 3 + 2]);
                b.set(vertices[elements[i + 1] * 3 + 0], vertices[elements[i + 1] * 3 + 1], vertices[elements[i + 1] * 3 + 2]);
                c.set(vertices[elements[i + 2] * 3 + 0], vertices[elements[i + 2] * 3 + 1], vertices[elements[i + 2] * 3 + 2]);
                
                b.sub(a, bsa);
                c.sub(a, csa);
                bsa.cross(csa, n);

                normals[elements[i + 0] * 3 + 0] += n.x;
                normals[elements[i + 0] * 3 + 1] += n.y;
                normals[elements[i + 0] * 3 + 2] += n.z;
                normals[elements[i + 1] * 3 + 0] += n.x;
                normals[elements[i + 1] * 3 + 1] += n.y;
                normals[elements[i + 1] * 3 + 2] += n.z;
                normals[elements[i + 2] * 3 + 0] += n.x;
                normals[elements[i + 2] * 3 + 1] += n.y;
                normals[elements[i + 2] * 3 + 2] += n.z;
            }
            
            for (int i = 0; i < normals.length; i += 3) {
            	n.set(normals[i + 0], normals[i + 1], normals[i + 2]);
            	n.normalize();

            	normals[i + 0] = n.x;
            	normals[i + 1] = n.y;
            	normals[i + 2] = n.z;
            }
        } else {
            int np = 0;
            for (int i = 0; i < totalVerts; i += 9) {
                a.set(vertices[i + 0], vertices[i + 1], vertices[i + 2]);
                b.set(vertices[i + 3], vertices[i + 4], vertices[i + 5]);
                c.set(vertices[i + 6], vertices[i + 7], vertices[i + 8]);
                
                b.sub(a, bsa);
                c.sub(a, csa);
                bsa.cross(csa, n);
                n.normalize();
                
                normals[np++] = n.x;
                normals[np++] = n.y;
                normals[np++] = n.z;
                normals[np++] = n.x;
                normals[np++] = n.y;
                normals[np++] = n.z;
                normals[np++] = n.x;
                normals[np++] = n.y;
                normals[np++] = n.z;
            }
        }
    }
}
