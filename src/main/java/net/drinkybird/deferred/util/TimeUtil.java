package net.drinkybird.deferred.util;

public class TimeUtil {
    private TimeUtil() { }
    
    public static double getTimeInMs() {
        return (double)System.nanoTime() / 1000000.0;
    }
}
