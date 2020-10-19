package io.colyseus.test.schema.backwards_forwards

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.util.default

class PlayerV1 : Schema() {
    @SchemaField("0/number")
    var x = Float.default

    @SchemaField("1/number")
    var y = Float.default
}