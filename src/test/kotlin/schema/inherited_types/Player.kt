package schema.inherited_types

import io.colyseus.annotations.SchemaField
import io.colyseus.default

open class Player : Entity() {
    @SchemaField("2/string")
    var name = String.default
}