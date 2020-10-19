package io.colyseus.test.schema.backwards_forwards

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.MapSchema
import io.colyseus.util.default

class StateV2 : Schema() {
    @SchemaField("0/string")
    var str = String.default

    @SchemaField("1/map/ref", PlayerV2::class)
    var map = MapSchema(PlayerV1::class.java)

    @SchemaField("2/number")
    var countdown = Float.default
}