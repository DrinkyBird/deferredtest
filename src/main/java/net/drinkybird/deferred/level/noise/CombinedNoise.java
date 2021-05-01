package net.drinkybird.deferred.level.noise;

public class CombinedNoise implements Noise {
    private final Noise n1, n2;
    
    public CombinedNoise(Noise n1, Noise n2) {
        this.n1 = n1;
        this.n2 = n2;
    }
    
    @Override
    public float compute(float x, float y) {
        float offset = n2.compute(x, y);
        return n1.compute(x + offset, y);
    }

    @Override
    public float compute(float x, float y, float z) {
        float offset = n2.compute(x, y, z);
        return n1.compute(x + offset, y, z);
    }
}
