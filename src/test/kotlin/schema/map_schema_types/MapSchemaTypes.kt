package schema.map_schema_types


import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.MapSchema


class MapSchemaTypes : Schema() {
    @SchemaField("0/map/ref", IAmAChild::class)
    public var mapOfSchemas = MapSchema(IAmAChild::class.java)

    @SchemaField("1/map/number")
    public var mapOfNumbers = MapSchema(Float::class.java)

    @SchemaField("2/map/string")
    public var mapOfStrings = MapSchema(String::class.java)

    @SchemaField("3/map/int32")
    public var mapOfInt32 = MapSchema(Int::class.java)
}