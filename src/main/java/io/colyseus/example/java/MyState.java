package io.colyseus.example.java;

import io.colyseus.annotations.SchemaClass;
import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;

@SchemaClass
public class MyState extends Schema {
    @SchemaField("0/ref")
    public PrimitivesTest primitives = new PrimitivesTest();

    @SchemaField("1/array/ref")
    public ArraySchema<Player> players = new ArraySchema<>(Player.class);

    @SchemaField("2/map/ref")
    public MapSchema<Cell> cells = new MapSchema<>(Cell.class);
}

@SchemaClass
class Cell extends Schema {
    @SchemaField("0/float32")
    public float x;

    @SchemaField("1/float32")
    public float y;
}

@SchemaClass
class PrimitivesTest extends Schema {
    @SchemaField("0/uint8")
    public short _uint8;

    @SchemaField("1/uint16")
    public int _uint16;

    @SchemaField("2/uint32")
    public long _uint32;

    @SchemaField("3/uint64")
    public long _uint64;

    @SchemaField("4/int8")
    public byte _int8;

    @SchemaField("5/int16")
    public short _int16;

    @SchemaField("6/int32")
    public int _int32;

    @SchemaField("7/int64")
    public long _int64;

    @SchemaField("8/float32")
    public float _float32_n;

    @SchemaField("9/float32")
    public float _float32_p;

    @SchemaField("10/float64")
    public double _float64_n;

    @SchemaField("11/float64")
    public double _float64_p;

    @SchemaField("12/boolean")
    public boolean _boolean;

    @SchemaField("13/string")
    public String _string;
}

@SchemaClass
class Player extends Schema {
    @SchemaField("0/int32")
    public int x;
}