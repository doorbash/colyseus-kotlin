package schema.child_schema_types


import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.util.default


class IAmAChild : Schema() {
    @SchemaField("0/number")
    public var x: Float = Float.default

    @SchemaField("1/number")
    public var y: Float = Float.default
}