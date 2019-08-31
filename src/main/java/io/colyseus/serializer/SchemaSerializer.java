package io.colyseus.serializer;

import io.colyseus.serializer.schema.Schema;

import java.lang.reflect.InvocationTargetException;

public class SchemaSerializer<T extends Schema> {

    public T state;

    public SchemaSerializer(Class<T> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.state = type.getConstructor().newInstance();
    }

    public void setState(byte[] data) throws NullPointerException, ArrayIndexOutOfBoundsException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException {
//        System.out.println("\n----------setState----------\n");
        state.decode(data);
    }

    public void patch(byte[] data) throws NullPointerException, ArrayIndexOutOfBoundsException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException {
//        System.out.println("\n----------patch----------\n");
        state.decode(data);
    }

    public void handshake(byte[] bytes) throws NullPointerException, ArrayIndexOutOfBoundsException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Schema.SchemaReflection reflection = new Schema.SchemaReflection();
        reflection.decode(bytes);
    }
}