package schema.array_schema_types

import io.colyseus.annotations.SchemaClass
import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema

@SchemaClass
class ArraySchemaTypes : Schema() {
    @SchemaField("0/array/ref")
    public var arrayOfSchemas = ArraySchema(IAmAChild::class.java)

    @SchemaField("1/array/number")
    public var arrayOfNumbers = ArraySchema<Float>()

    @SchemaField("2/array/string")
    public var arrayOfStrings = ArraySchema<String>()

    @SchemaField("3/array/int32")
    public var arrayOfInt32 = ArraySchema<Int>()
}