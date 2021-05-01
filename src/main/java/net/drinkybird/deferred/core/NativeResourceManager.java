package net.drinkybird.deferred.core;

import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;

public class NativeResourceManager {
    private static volatile List<NativeResource> resources = new ArrayList<>();
    
    static void scheduleForCleanup(NativeResource resource) {
        synchronized (resources) {
            resources.add(resource);
        }
    }
    
    public static void cleanup() {
        int n = 0;
        
        synchronized (resources) {
            for (NativeResource resource : resources) {
                resource.destroy();
                n++;
            }
            
            resources.clear();
        }
        
        if (n > 0) {
            Logger.info("Cleaned up {} resources", n);
        }
    }
}
