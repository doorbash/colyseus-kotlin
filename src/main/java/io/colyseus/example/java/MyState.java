package io.colyseus.example.java;

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.types.ArraySchema;
import io.colyseus.serializer.schema.types.MapSchema;

public class MyState extends Schema {
    @SchemaField(v1 = "0/ref", v2 = PrimitivesTest.class)
    public PrimitivesTest primitives = new PrimitivesTest();

    @SchemaField(v1 = "1/array/ref", v2 = Player.class)
    public ArraySchema<Player> players = new ArraySchema<>(Player.class);

    @SchemaField(v1 = "2/map/ref", v2 = Cell.class)
    public MapSchema<Cell> cells = new MapSchema<>(Cell.class);
}

class Cell extends Schema {
    @SchemaField(v1 = "0/float32")
    public float x;

    @SchemaField(v1 = "1/float32")
    public float y;
}

class PrimitivesTest extends Schema {
    @SchemaField(v1 = "0/uint8")
    public short _uint8;

    @SchemaField(v1 = "1/uint16")
    public int _uint16;

    @SchemaField(v1 = "2/uint32")
    public long _uint32;

    @SchemaField(v1 = "3/uint64")
    public long _uint64;

    @SchemaField(v1 = "4/int8")
    public byte _int8;

    @SchemaField(v1 = "5/int16")
    public short _int16;

    @SchemaField(v1 = "6/int32")
    public int _int32;

    @SchemaField(v1 = "7/int64")
    public long _int64;

    @SchemaField(v1 = "8/float32")
    public float _float32_n;

    @SchemaField(v1 = "9/float32")
    public float _float32_p;

    @SchemaField(v1 = "10/float64")
    public double _float64_n;

    @SchemaField(v1 = "11/float64")
    public double _float64_p;

    @SchemaField(v1 = "12/boolean")
    public boolean _boolean;

    @SchemaField(v1 = "13/string")
    public String _string;
}

class Player extends Schema {
    @SchemaField(v1 = "0/int32")
    public int x;
}