package io.colyseus.state_listener;

public abstract class PatchListenerCallback {
    protected abstract void callback(DataChange change);
}
