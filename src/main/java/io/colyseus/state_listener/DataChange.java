package io.colyseus.state_listener;

import java.util.LinkedHashMap;

public class DataChange {
    public LinkedHashMap<String, String> path;
    public String operation; // : "add" | "remove" | "replace";
    public Object value;

    @Override
    public String toString() {
        return path + ", " + operation + ", " + value;
    }
}
