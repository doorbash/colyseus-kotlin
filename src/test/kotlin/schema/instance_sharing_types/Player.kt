package schema.instance_sharing_types

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema

class Player : Schema() {
    @SchemaField("0/ref", Position::class)
    var position = Position()
}