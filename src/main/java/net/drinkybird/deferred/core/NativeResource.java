package net.drinkybird.deferred.core;

public abstract class NativeResource {
    public abstract void destroy();
    
    @Override
    protected void finalize() throws Throwable {
        NativeResourceManager.scheduleForCleanup(this);
        super.finalize();
    }
}
