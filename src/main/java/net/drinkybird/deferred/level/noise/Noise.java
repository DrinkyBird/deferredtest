package net.drinkybird.deferred.level.noise;

public interface Noise {
    float compute(float x, float y);
    float compute(float x, float y, float z);
}
