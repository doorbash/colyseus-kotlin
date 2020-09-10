package io.colyseus

internal fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
internal operator fun ByteArray.get(intRange: IntRange): ByteArray = copyOfRange(intRange.first, intRange.last)
internal operator fun ByteArray.get(i:Int, j:Int): ByteArray = copyOfRange(i, j)