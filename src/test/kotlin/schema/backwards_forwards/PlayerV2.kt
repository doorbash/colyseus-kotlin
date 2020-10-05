package schema.backwards_forwards

import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema

class PlayerV2 : Schema() {
    @SchemaField("0/number")
    var x = Float.default

    @SchemaField("1/number")
    var y = Float.default

    @SchemaField("2/string")
    var name = String.default

    @SchemaField("3/array/string")
    var arrayOfStrings = ArraySchema(String::class.java)
}