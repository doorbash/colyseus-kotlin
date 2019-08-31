package io.colyseus.serializer;

import io.colyseus.serializer.schema.Schema;

import java.lang.reflect.InvocationTargetException;

public class SchemaSerializer<T extends Schema> {

    public T state;

    public SchemaSerializer(Class<T> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.state = type.getConstructor().newInstance();
    }

    public void setState(byte[] data) throws Exception {
        state.decode(data);
    }

    public void patch(byte[] data) throws Exception {
        state.decode(data);
    }

    public void handshake(byte[] bytes) throws Exception {
        Schema.SchemaReflection reflection = new Schema.SchemaReflection();
        reflection.decode(bytes);
    }
}