package io.colyseus.serializer.schema.types

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema


class SchemaReflectionField : Schema() {
    @SchemaField("0/string", String::class)
    var name: String? = null

    @SchemaField("1/string", String::class)
    var type: String? = null

    @SchemaField("2/uint8", Int::class)
    var referencedType = 0
}


class SchemaReflectionType : Schema() {
    @SchemaField(v1 = "0/uint8", Int::class)
    var id = 0

    @SchemaField("1/array/ref", SchemaReflectionField::class)
    var fields = ArraySchema(SchemaReflectionField::class.java)

    var type: Class<*>? = null
}


class SchemaReflection : Schema() {
    @SchemaField("0/array/ref", SchemaReflectionType::class)
    var types = ArraySchema(SchemaReflectionType::class.java)

    @SchemaField("1/uint8", Int::class)
    var rootType = 0
}