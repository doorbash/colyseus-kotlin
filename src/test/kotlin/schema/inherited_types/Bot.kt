package schema.inherited_types

import io.colyseus.annotations.SchemaField
import io.colyseus.default

class Bot : Player() {
    @SchemaField("3/number")
    var power = Float.default
}