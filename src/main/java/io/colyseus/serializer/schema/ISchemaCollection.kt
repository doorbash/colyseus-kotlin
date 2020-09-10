package io.colyseus.serializer.schema;

public interface ISchemaCollection<K, T> {
    void invokeOnAdd(T item, K index);

    void invokeOnChange(T item, K index);

    void invokeOnRemove(T item, K index);

    void triggerAll();

    void _set(K key, T item);

    Class<?> getChildType();

    boolean hasSchemaChild();

    int count();

    ISchemaCollection _clone();
}
