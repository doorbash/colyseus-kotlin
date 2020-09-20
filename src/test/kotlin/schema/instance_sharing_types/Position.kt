package schema.instance_sharing_types

import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema

class Position : Schema() {
    @SchemaField("0/number")
    var x = Float.default

    @SchemaField("1/number")
    var y = Float.default
}