package net.drinkybird.deferred.level.noise;

import java.util.Random;

public class OctaveNoise implements Noise {
    private final int numOctaves;
    private final Noise[] octaves;
    
    public OctaveNoise(Random random, int numOctaves) {
        this.numOctaves = numOctaves;
        this.octaves = new Noise[numOctaves];
        
        for (int i = 0; i < numOctaves; i++) {
            this.octaves[i] = new ImprovedNoise(random);
        }
    }
    
    @Override
    public float compute(float x, float y) {
        float amplitude = 1, frequency = 1;
        float sum = 0;

        for (int i = 0; i < numOctaves; i++) {
            sum += octaves[i].compute(x * frequency, y * frequency) * amplitude;
            amplitude *= 2.0;
            frequency *= 0.5;
        }

        return sum;
    }

    @Override
    public float compute(float x, float y, float z) {
        float amplitude = 1, frequency = 1;
        float sum = 0;

        for (int i = 0; i < numOctaves; i++) {
            sum += octaves[i].compute(x * frequency, y * frequency, z * frequency) * amplitude;
            amplitude *= 2.0;
            frequency *= 0.5;
        }

        return sum;
    }
}
