package io.colyseus.state_listener;

import java.util.LinkedHashMap;

public class DataChange {
    LinkedHashMap<String, String> path;
    String operation; // : "add" | "remove" | "replace";
    Object value;

    @Override
    public String toString() {
        return path + ", " + operation + ", " + value;
    }
}
