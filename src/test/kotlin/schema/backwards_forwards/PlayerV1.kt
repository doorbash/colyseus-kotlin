package schema.backwards_forwards

import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema

class PlayerV1 : Schema() {
    @SchemaField("0/number")
    var x = Float.default

    @SchemaField("1/number")
    var y = Float.default
}