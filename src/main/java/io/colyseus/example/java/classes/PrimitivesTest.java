package io.colyseus.example.java.classes;

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;

public class PrimitivesTest extends Schema {
    @SchemaField(type = "0/uint8")
    public short _uint8;

    @SchemaField(type = "1/uint16")
    public int _uint16;

    @SchemaField(type = "2/uint32")
    public long _uint32;

    @SchemaField(type = "3/uint64")
    public long _uint64;

    @SchemaField(type = "4/int8")
    public byte _int8;

    @SchemaField(type = "5/int16")
    public short _int16;

    @SchemaField(type = "6/int32")
    public int _int32;

    @SchemaField(type = "7/int64")
    public long _int64;

    @SchemaField(type = "8/float32")
    public float _float32_n;

    @SchemaField(type = "9/float32")
    public float _float32_p;

    @SchemaField(type = "10/float64")
    public double _float64_n;

    @SchemaField(type = "11/float64")
    public double _float64_p;

    @SchemaField(type = "12/boolean")
    public boolean _boolean;

    @SchemaField(type = "13/string")
    public String _string;
}
