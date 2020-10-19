package io.colyseus.test.schema.inherited_types

import io.colyseus.annotations.SchemaField
import io.colyseus.util.default

class Bot : Player() {
    @SchemaField("3/number")
    var power = Float.default
}