package io.colyseus.serializer.schema

class Change {
    var field: String? = null
    var value: Any? = null
    var previousValue: Any? = null
    override fun toString(): String {
        return "$field: $previousValue --> $value"
    }
}