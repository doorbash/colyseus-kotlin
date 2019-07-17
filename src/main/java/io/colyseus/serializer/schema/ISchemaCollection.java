package io.colyseus.serializer.schema;

public interface ISchemaCollection<K, T> {
    void invokeOnAdd(T item, K index);

    void invokeOnChange(T item, K index);

    void invokeOnRemove(T item, K index);

    Object getItems();

    void setItems(Object items);

    void triggerAll();

    Class<?> getChildType();

    boolean hasSchemaChild();

    int count();

    T get(K key);

    void set(K key, T item);

    ISchemaCollection _clone();
}
