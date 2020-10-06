package io.colyseus.serializer

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Context
import io.colyseus.serializer.schema.Iterator
import io.colyseus.serializer.schema.ReferenceTracker
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.SchemaReflection
import io.colyseus.serializer.schema.types.SchemaReflectionType
import io.colyseus.util.Lock
import io.colyseus.util.allFields

class SchemaSerializer<T : Schema>(val schema: Class<T>) {
    var state: T = schema.getConstructor().newInstance() as T
    var refs = ReferenceTracker()
    val lock = Lock()

    fun setState(data: ByteArray, offset: Int = 0) {
//        lock.withLock {
        state.decode(data, Iterator(offset), refs)
//        }
    }

    fun patch(data: ByteArray, offset: Int = 0) {
//        lock.withLock {
        state.decode(data, Iterator(offset), refs)
//        }
    }

    fun teardown() {
        // Clear all stored references.
        refs.clear()
        Context.instance.clear()
    }

    fun handshake(bytes: ByteArray?, offset: Int = 0) {
//        lock.withLock {
        val reflection = SchemaReflection()
        reflection.decode(bytes!!, Iterator(offset))
        Context.instance.clear()
        initTypes(reflection, schema = schema as Class<Any>)
        for (rt in reflection.types) {
            Context.instance.setTypeId(rt?.type!!, rt.id)
        }
//        }
    }

    private fun initTypes(reflection: SchemaReflection, index: Int = reflection.rootType, schema: Class<out Any>) {
        val currentType: SchemaReflectionType? = reflection.types[index]
        currentType?.type = schema
        for (f in currentType?.fields!!) {
            if (f?.type in arrayOf("ref", "array", "map")) {
                for (field in schema.allFields) {
                    if (!field.isAnnotationPresent(SchemaField::class.java)) continue
                    field.isAccessible = true
                    if (field.name == f?.name) {
                        val v2 = field.getAnnotation(SchemaField::class.java).v2
                        if (v2 == Any::class) throw Exception("Schema error at: ${schema.simpleName}.${field.name}")
                        initTypes(reflection, f?.referencedType!!, v2.java)
                        break
                    }
                }
            }
        }
    }
}