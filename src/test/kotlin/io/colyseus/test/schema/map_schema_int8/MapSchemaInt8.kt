package io.colyseus.test.schema.map_schema_int8

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.MapSchema
import io.colyseus.util.default

class MapSchemaInt8 : Schema() {
    @SchemaField("0/string")
    var status = String.default

    @SchemaField("1/map/int8")
    var mapOfInt8 = MapSchema(Byte::class.java)
}