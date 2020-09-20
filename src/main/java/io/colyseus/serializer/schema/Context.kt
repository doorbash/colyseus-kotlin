package io.colyseus.serializer.schema

import java.util.LinkedHashMap

class Context {
    private var typeIds = LinkedHashMap<Int, Class<*>>()

    operator fun get(typeid: Int): Class<*>? {
        return typeIds[typeid]
    }

    internal fun setTypeId(type: Class<*>, typeid: Int) {
        typeIds[typeid] = type
    }

    fun clear() {
        typeIds.clear()
    }

    companion object {
        var instance = Context()
            protected set
    }
}