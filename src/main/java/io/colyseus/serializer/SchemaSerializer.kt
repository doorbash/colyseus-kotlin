package io.colyseus.serializer

import io.colyseus.allFields
import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Context
import io.colyseus.serializer.schema.Iterator
import io.colyseus.serializer.schema.ReferenceTracker
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema
import io.colyseus.serializer.schema.types.SchemaReflection
import io.colyseus.serializer.schema.types.SchemaReflectionType

class SchemaSerializer<T : Schema>(val schema: Class<T>) {
    var state: T = schema.getConstructor().newInstance() as T
    var refs = ReferenceTracker()

    fun setState(data: ByteArray, offset: Int = 0) {
        synchronized(this) {
            state.decode(data, Iterator(offset), refs)
        }
    }

    fun patch(data: ByteArray, offset: Int = 0) {
        synchronized(this) {
            state.decode(data, Iterator(offset), refs)
        }
    }

    fun teardown() {
        // Clear all stored references.
        refs.clear()
        Context.instance.clear()
    }

    fun handshake(bytes: ByteArray?, offset: Int = 0) {
        synchronized(this) {
            val reflection = SchemaReflection()
            reflection.decode(bytes!!, Iterator(offset))
            Context.instance.clear()
            initTypes(reflection, schema = state)
            for (rt in reflection.types) {
                Context.instance.setTypeId(rt?.type!!, rt.id)
            }
        }
    }

    private fun initTypes(reflection: SchemaReflection, index: Int = reflection.rootType, schema: Any) {
        val currentType: SchemaReflectionType? = reflection.types[index]
        currentType?.type = when (schema) {
            is ArraySchema<*> -> schema.ct
            is MapSchema<*> -> schema.ct
            else -> schema::class.java
        }
        for (f in currentType?.fields!!) {
            if (f?.type in arrayOf("ref", "array", "map")) {
                for (field in schema::class.java.allFields) {
                    if (!field.isAnnotationPresent(SchemaField::class.java)) continue
                    field.isAccessible = true
                    if (field.name == f?.name) {
                        var value = field.get(schema)
                        initTypes(reflection, f?.referencedType!!, value)
                        break
                    }
                }
            }
        }
    }
}