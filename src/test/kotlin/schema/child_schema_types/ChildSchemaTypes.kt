package schema.child_schema_types

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema


class ChildSchemaTypes : Schema() {
    @SchemaField("0/ref", IAmAChild::class)
    public var child = IAmAChild()

    @SchemaField("1/ref", IAmAChild::class)
    public var secondChild = IAmAChild()
}