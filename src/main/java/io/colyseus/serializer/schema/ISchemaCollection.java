package io.colyseus.serializer.schema;

public interface ISchemaCollection<K, T> {
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
