package io.colyseus

import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema
import java.lang.reflect.Field


internal fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
internal operator fun ByteArray.get(intRange: IntRange): ByteArray = copyOfRange(intRange.first, intRange.last)
internal operator fun ByteArray.get(i: Int, j: Int): ByteArray = copyOfRange(i, j)

fun default(javaClass: Class<*>): Any? {
    return when (javaClass) {
        Byte::class.java -> 0.toByte()
        Short::class.java -> 0.toShort()
        Int::class.java -> 0
        Long::class.java -> 0.toLong()
        Float::class.java -> 0.toFloat()
        Double::class.java -> 0.toDouble()
        Boolean::class.java -> false
        String::class.java -> null
        else -> null
    }
}

public val Byte.Companion.default: Byte get() = 0
public val Short.Companion.default: Short get() = 0
public val Int.Companion.default: Int get() = 0
public val Long.Companion.default: Long get() = 0
public val Float.Companion.default: Float get() = 0f
public val Double.Companion.default: Double get() = 0.0
public val Boolean.Companion.default: Boolean get() = false
public val String.Companion.default: String? get() = null

fun isPrimary(type: String): Boolean {
    return type !in arrayOf("map", "array", "ref")
}

fun getType(type: String): Class<*>? {
    return when (type) {
        "int8" -> Byte::class.java
        "uint8" -> Short::class.java
        "int16" -> Short::class.java
        "uint16" -> Int::class.java
        "int32" -> Int::class.java
        "uint32" -> Long::class.java
        "int64" -> Long::class.java
        "uint64" -> Long::class.java
        "float32" -> Float::class.java
        "float64" -> Double::class.java
        "number" -> Float::class.java
        "string" -> String::class.java
        "boolean" -> Boolean::class.java
        "map" -> MapSchema::class.java
        "array" -> ArraySchema::class.java
        else -> null
    }
}

val Class<*>.allFields: List<Field>
    get() {
        val allFields = mutableListOf<Field>()
        var currentClass = this as Class<*>?
        while (currentClass != null) {
            val declaredFields: Array<Field> = currentClass.declaredFields
            allFields.addAll(declaredFields)
            currentClass = currentClass.superclass
        }
        return allFields
    }

operator fun Class<*>.get(name: String): Field? {
    for (field in allFields) {
        if (field.name == name) return field
    }
    return null
}