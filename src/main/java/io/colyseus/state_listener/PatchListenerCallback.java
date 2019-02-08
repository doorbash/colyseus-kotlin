package io.colyseus.state_listener;

public abstract class PatchListenerCallback {

    /**
     * Called when a specific change happens to the room state
     */
    protected abstract void callback(DataChange change);
}
