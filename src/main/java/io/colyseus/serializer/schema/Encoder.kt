package io.colyseus.serializer.schema

import io.colyseus.Protocol
import io.colyseus.byteArrayOfInts

class Encoder {
    companion object {

        public fun getInitialBytesFromEncodedType(encodedType: ByteArray): ByteArray {
            val size = encodedType.size
            return byteArrayOfInts(Protocol.ROOM_DATA) + when {
                size < 0x20 -> byteArrayOfInts(size or 0xa0)
                size < 0x100 -> byteArrayOfInts(0xd9) + uint8(size)
                size < 0x10000 -> byteArrayOfInts(0xda) + uint16(size)
                size < 0x7fffffff -> byteArrayOfInts(0xdb) + uint32(size)
                else -> throw Exception("String too long")
            }
        }

        internal fun uint8(value: Int) = byteArrayOfInts(value and 0xFF)
        internal fun uint16(value: Int) = uint8(value) + byteArrayOfInts(value shr 8 and 0xFF)
        internal fun uint32(value: Int) = uint16(value) +
                byteArrayOfInts(value shr 16 and 0xFF, value shr 16 and 0xFF, value shr 24 and 0xFF)
    }
}