package io.colyseus.test.schema.filtered_types

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.util.default

class Player : Schema() {
    @SchemaField("0/string")
    var name = String.default
}