package io.colyseus.test.schema.inherited_types

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.util.default

open class Entity : Schema() {
    @SchemaField("0/number")
    var x = Float.default

    @SchemaField("1/number")
    var y = Float.default
}