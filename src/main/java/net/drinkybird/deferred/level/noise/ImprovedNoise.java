package net.drinkybird.deferred.level.noise;

import java.util.Random;

public class ImprovedNoise implements Noise {
    private int[] p = new int[512];
    
    public ImprovedNoise(Random random) {
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }

        for (int i = 0; i < 256; i++) {
            p[i + 256] = p[i];
        }
    }
    
    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }
    
    private float grad(int hash, float x, float y, float z) {
        int h = hash & 15;
        float u = h < 8 ? x : y,
            v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
    
    public float compute(float x, float y, float z) {
        int xFloor = x >= 0 ? (int) x : (int) x - 1;
        int yFloor = y >= 0 ? (int) y : (int) y - 1;
        int zFloor = z >= 0 ? (int) z : (int) z - 1;
        int X = xFloor & 0xFF;
        int Y = yFloor & 0xFF;
        int Z = zFloor & 0xFF;
        
        x -= xFloor;
        y -= yFloor;
        z -= zFloor;
        
        float u = fade(x);
        float v = fade(y);
        float w = fade(z);
        int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z;
        int B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;
        
        return lerp(w, lerp(v, lerp(u, grad(p[AA  ], x  , y  , z   ),
                                       grad(p[BA  ], x-1, y  , z   )),
                               lerp(u, grad(p[AB  ], x  , y-1, z   ),
                                       grad(p[BB  ], x-1, y-1, z   ))),
                       lerp(v, lerp(u, grad(p[AA+1], x  , y  , z-1 ),
                                       grad(p[BA+1], x-1, y  , z-1 )),
                               lerp(u, grad(p[AB+1], x  , y-1, z-1 ),
                                       grad(p[BB+1], x-1, y-1, z-1 ))));
    }
    
    public float compute(float x, float y) {
        return compute(x, y, 0.0f);
    }
}
