package io.colyseus.example.java.classes;

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.types.ArraySchema;
import io.colyseus.serializer.schema.types.MapSchema;

public class MyState extends Schema {
    @SchemaField(type = "0/ref", ref = PrimitivesTest.class)
    public PrimitivesTest primitives = new PrimitivesTest();

    @SchemaField(type = "1/array/ref", ref = Player.class)
    public ArraySchema<Player> players = new ArraySchema<>(Player.class);

    @SchemaField(type = "2/map/ref", ref = Cell.class)
    public MapSchema<Cell> cells = new MapSchema<>(Cell.class);
}

