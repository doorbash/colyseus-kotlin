package schema.primitive_types

import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema

class PrimitiveTypes : Schema() {

    @SchemaField("0/int8")
    public var int8: Byte = Byte.default

    @SchemaField("1/uint8")
    public var uint8: Short = Short.default

    @SchemaField("2/int16")
    public var int16: Short = Short.default

    @SchemaField("3/uint16")
    public var uint16: Int = Int.default

    @SchemaField("4/int32")
    public var int32: Int = Int.default

    @SchemaField("5/uint32")
    public var uint32: Long = Long.default

    @SchemaField("6/int64")
    public var int64: Long = Long.default

    @SchemaField("7/uint64")
    public var uint64: Long = Long.default

    @SchemaField("8/float32")
    public var float32: Float = Float.default

    @SchemaField("9/float64")
    public var float64: Double = Double.default

    @SchemaField("10/number")
    public var varint_int8: Float = Float.default

    @SchemaField("11/number")
    public var varint_uint8: Float = Float.default

    @SchemaField("12/number")
    public var varint_int16: Float = Float.default

    @SchemaField("13/number")
    public var varint_uint16: Float = Float.default

    @SchemaField("14/number")
    public var varint_int32: Float = Float.default

    @SchemaField("15/number")
    public var varint_uint32: Float = Float.default

    @SchemaField("16/number")
    public var varint_int64: Float = Float.default

    @SchemaField("17/number")
    public var varint_uint64: Float = Float.default

    @SchemaField("18/number")
    public var varint_float32: Float = Float.default

    @SchemaField("19/number")
    public var varint_float64: Float = Float.default

    @SchemaField("20/string")
    public var str: String? = String.default

    @SchemaField("21/boolean")
    public var boolean: Boolean = Boolean.default
}
