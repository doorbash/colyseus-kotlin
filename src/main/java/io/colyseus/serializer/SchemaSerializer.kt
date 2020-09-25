package io.colyseus.serializer

import io.colyseus.Lock
import io.colyseus.default
import io.colyseus.getType
import io.colyseus.isPrimary
import io.colyseus.serializer.schema.Iterator
import io.colyseus.serializer.schema.ReferenceTracker
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema
import io.colyseus.serializer.schema.types.Reflection
import io.colyseus.serializer.schema.types.Reflection.Companion.schemaReflection

class SchemaSerializer {
    var state: Schema = Schema()
    var refs = ReferenceTracker()
    internal val lock = Lock()

    fun setState(data: ByteArray, offset: Int = 0) {
        lock.withLock {
            state.decode(data, Iterator(offset), refs)
        }
    }

    fun patch(data: ByteArray, offset: Int = 0) {
        lock.withLock {
            state.decode(data, Iterator(offset), refs)
        }
    }

    fun teardown() {
        // Clear all stored references.
        refs.clear()
    }

    fun handshake(bytes: ByteArray?, offset: Int = 0) {
        lock.withLock {
            val reflection = schemaReflection.clone()
            reflection.decode(bytes!!, Iterator(offset))
            Reflection.reflectionObject = reflection
            state = Schema(reflection["rootType"] as Short)
            initTypes(reflection, schema = state)
        }
    }

    private fun initTypes(reflection: Schema, schema: Schema) {
        val currentType = (reflection["types"] as ArraySchema<*>)[schema.referencedType.toInt()] as Schema
//        currentType["type"] = schema
        for (f in (currentType["fields"] as ArraySchema<*>)) {
            val fieldType = (f as Schema)["type"] as String
            if (!isPrimary(fieldType)) {
                val field = f["name"] as String
                val rt = f["referencedType"] as Short
                schema[field] = when (fieldType) {
                    "array" -> ArraySchema(Schema::class.java)
                    "map" -> MapSchema(Schema::class.java)
                    else -> Schema(rt)
                }
                if (schema[field] is Schema)
                    initTypes(reflection, schema[field] as Schema)
            }/* else {
                schema[f["name"] as String] = default(getType(fieldType)!!)
            }*/
        }
    }
}