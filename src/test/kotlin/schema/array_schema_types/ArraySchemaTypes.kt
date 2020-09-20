package schema.array_schema_types


import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema


class ArraySchemaTypes : Schema() {
    @SchemaField("0/array/ref", IAmAChild::class)
    public var arrayOfSchemas = ArraySchema(IAmAChild::class.java)

    @SchemaField("1/array/number")
    public var arrayOfNumbers = ArraySchema(Float::class.java)

    @SchemaField("2/array/string")
    public var arrayOfStrings = ArraySchema(String::class.java)

    @SchemaField("3/array/int32")
    public var arrayOfInt32 = ArraySchema(Int::class.java)
}