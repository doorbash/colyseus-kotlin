package io.colyseus.serializer.schema

import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema

class ReferenceTracker {
    var refs = HashMap<Int, IRef?>()
    var refCounts = HashMap<Int, Int>()
    var deletedRefs = mutableListOf<Int>()

    public fun add(refId: Int, _ref: IRef, incrementCount: Boolean = true) {
        val previousCount: Int
        if (!refs.containsKey(refId)) {
            refs[refId] = _ref
            previousCount = 0
        } else {
            previousCount = refCounts[refId] ?: 0
        }
        if (incrementCount) {
            refCounts[refId] = previousCount + 1
        }
    }

    public operator fun get(refId: Int): IRef? {
        return refs[refId]
    }

    public operator fun set(refId: Int, value: IRef?) {
        refs.put(refId, value)
    }

    public fun has(refId: Int): Boolean {
        return refs.containsKey(refId)
    }

    fun remove(refId: Int): Boolean? {
        refCounts[refId] = refCounts[refId]!! - 1
        return if (!deletedRefs.contains(refId)) {
            deletedRefs.add(refId)
            true
        } else {
            false
        }
    }

    public fun garbageCollection() {
        var totalDeletedRefs = deletedRefs.size
        var i = 0
        while (i < totalDeletedRefs) {
            val refId = deletedRefs[i]

            if (refCounts[refId]!! <= 0) {
                val _ref = refs[refId]
                if (_ref is Schema) {
                    for (field in _ref.fieldChildTypes) {
                        val fieldValue = _ref[field.key]
                        if (fieldValue is IRef && remove(fieldValue.__refId)!!) {
                            totalDeletedRefs++
                        }
                    }
                } else if (_ref is ISchemaCollection && _ref.hasSchemaChild()) {
                    if (_ref is ArraySchema<*>) {
                        for (item in _ref) {
                            if (item == null) continue
                            if (remove((item as IRef).__refId)!!) {
                                totalDeletedRefs++
                            }
                        }
                    } else if (_ref is MapSchema<*>) {
                        for (item in _ref) {
                            if (item.value == null) continue
                            if (remove((item.value as IRef).__refId)!!) {
                                totalDeletedRefs++
                            }
                        }
                    }
                }
                refs.remove(refId)
                refCounts.remove(refId)
            }
            i++
        }

        deletedRefs.clear()
    }

    fun clear() {
        refs.clear()
        refCounts.clear()
        deletedRefs.clear()
    }

}