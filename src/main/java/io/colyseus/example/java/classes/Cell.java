package io.colyseus.example.java.classes;

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;

public class Cell extends Schema {
    @SchemaField(type = "0/float32")
    public float x;

    @SchemaField(type = "1/float32")
    public float y;
}
