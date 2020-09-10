package io.colyseus.serializer.schema

interface ISchemaCollection<K, T> {
    fun triggerAll()
    fun _set(key: K, item: T)
    fun hasSchemaChild(): Boolean
    fun count(): Int
    fun _clone(): ISchemaCollection<*, *>
}