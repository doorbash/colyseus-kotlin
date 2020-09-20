package schema.filtered_types

import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema

class Player : Schema() {
    @SchemaField("0/string")
    var name = String.default
}