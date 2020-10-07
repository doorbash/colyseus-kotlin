package io.colyseus.util.callbacks;

public interface Function2Void<T, K> {
    void invoke(T p1, K p2);
}