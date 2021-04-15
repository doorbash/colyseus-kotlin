package io.colyseus.test.schema.map_schema_move_nullify_type

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.MapSchema
import io.colyseus.test.schema.map_schema_types.IAmAChild

class State : Schema() {
    @SchemaField("0/map/number")
    var previous = MapSchema(Float::class.java)

    @SchemaField("1/map/number")
    var current = MapSchema(Float::class.java)
}