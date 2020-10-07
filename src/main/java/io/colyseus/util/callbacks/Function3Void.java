package io.colyseus.util.callbacks;

public interface Function3Void<T, K, R> {
    void invoke(T p1, K p2, R p3);
}