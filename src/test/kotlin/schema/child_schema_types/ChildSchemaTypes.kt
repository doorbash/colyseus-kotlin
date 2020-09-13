package schema.child_schema_types

import io.colyseus.annotations.SchemaClass
import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema

@SchemaClass
class ChildSchemaTypes : Schema() {
    @SchemaField("0/ref")
    public var child = IAmAChild()

    @SchemaField("1/ref")
    public var secondChild = IAmAChild()
}