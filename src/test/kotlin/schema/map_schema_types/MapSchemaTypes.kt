package schema.map_schema_types

import io.colyseus.annotations.SchemaClass
import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema

@SchemaClass
class MapSchemaTypes : Schema() {
    @SchemaField("0/map/ref")
    public var mapOfSchemas = MapSchema(IAmAChild::class.java)

    @SchemaField("1/map/number")
    public var mapOfNumbers = MapSchema<Float>()

    @SchemaField("2/map/string")
    public var mapOfStrings = MapSchema<String>()

    @SchemaField("3/map/int32")
    public var mapOfInt32 = MapSchema<Int>()
}