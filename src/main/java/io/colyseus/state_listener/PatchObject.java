package io.colyseus.state_listener;

import java.util.List;

public class PatchObject {

    public List<String> path;
    public String operation;
    public Object value;

    public PatchObject(String operation, List<String> path, Object value) {
        this.operation = operation;
        this.path = path;
        this.value = value;
    }

    @Override
    public String toString() {
        return path + ", " + operation + ", " + value;
    }
}
