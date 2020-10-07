package io.colyseus.example.java.classes;

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;

public class Player extends Schema {
    @SchemaField(type = "0/int32")
    public int x;
}
