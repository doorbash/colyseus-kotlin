package schema.primitive_types

import io.colyseus.annotations.SchemaClass
import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema

@SchemaClass
class PrimitiveTypes : Schema() {

    @SchemaField("0/int8")
    public var int8: Byte = 0

    @SchemaField("1/uint8")
    public var uint8: Short = 0

    @SchemaField("2/int16")
    public var int16: Short = 0

    @SchemaField("3/uint16")
    public var uint16: Int = 0

    @SchemaField("4/int32")
    public var int32: Int = 0

    @SchemaField("5/uint32")
    public var uint32: Long = 0

    @SchemaField("6/int64")
    public var int64: Long = 0

    @SchemaField("7/uint64")
    public var uint64: Long = 0

    @SchemaField("8/float32")
    public var float32: Float = 0f

    @SchemaField("9/float64")
    public var float64: Double = 0.0

    @SchemaField("10/number")
    public var varint_int8: Float = 0f

    @SchemaField("11/number")
    public var varint_uint8: Float = 0f

    @SchemaField("12/number")
    public var varint_int16: Float = 0f

    @SchemaField("13/number")
    public var varint_uint16: Float = 0f

    @SchemaField("14/number")
    public var varint_int32: Float = 0f

    @SchemaField("15/number")
    public var varint_uint32: Float = 0f

    @SchemaField("16/number")
    public var varint_int64: Float = 0f

    @SchemaField("17/number")
    public var varint_uint64: Float = 0f

    @SchemaField("18/number")
    public var varint_float32: Float = 0f

    @SchemaField("19/number")
    public var varint_float64: Float = 0f

    @SchemaField("20/string")
    public var str: String? = null

    @SchemaField("21/boolean")
    public var boolean: Boolean = false;
}
