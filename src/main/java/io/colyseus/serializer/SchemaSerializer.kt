package io.colyseus.serializer

import io.colyseus.serializer.schema.Iterator
import io.colyseus.serializer.schema.Schema
import java.lang.Exception

class SchemaSerializer<T : Schema>(type: Class<T>) {
    var state = type.getConstructor().newInstance()

    fun setState(data: ByteArray, offset: Int = 0) {
        synchronized(this) {
            state.decode(data, Iterator(offset))
        }
    }

    fun patch(data: ByteArray, offset: Int = 0) {
        synchronized(this) {
            state.decode(data, Iterator(offset))
        }
    }

    fun handshake(bytes: ByteArray?, offset: Int = 0) {
        synchronized(this) {
            val reflection = Schema.SchemaReflection()
            reflection.decode(bytes!!, Iterator(offset))
            addTypes(reflection,schema = state)
            for(rt in reflection.types) {
                Schema.Context.getInstance().setTypeId(rt.type, rt.id)
            }
        }
    }

    fun addTypes(reflection: Schema.SchemaReflection, index: Int = reflection.rootType, schema: Any) {
        val currentType : Schema.SchemaReflectionType = reflection.types[index]
        currentType.type = schema.javaClass
        currentType.fields.forEach{
            if(it.type in arrayOf("ref", "array", "map")) {
                for (field in schema.javaClass.declaredFields) {
                    field.isAccessible = true
                    if(field.name == it.name) {
                        val value = field.get(schema)
                        addTypes(reflection, it.referencedType, value)
                        break
                    }
                }
            }
        }

    }
}