package schema.map_schema_types

import io.colyseus.annotations.SchemaClass
import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema

@SchemaClass
class IAmAChild : Schema() {
    @SchemaField("0/number")
    public var x: Float = 0f

    @SchemaField("1/number")
    public var y: Float = 0f
}