package io.colyseus.serializer.schema

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object Decoder {
    fun decodePrimitiveType(type: String?, bytes: ByteArray, it: Iterator): Any {
        return when (type) {
            "string" -> return decodeString(bytes, it)
            "number" -> return decodeNumber(bytes, it)
            "int8" -> return decodeInt8(bytes, it)
            "uint8" -> return decodeUint8(bytes, it)
            "int16" -> return decodeInt16(bytes, it)
            "uint16" -> return decodeUint16(bytes, it)
            "int32" -> return decodeInt32(bytes, it)
            "uint32" -> return decodeUint32(bytes, it)
            "int64" -> return decodeInt64(bytes, it)
            "uint64" -> return decodeUint64(bytes, it)
            "float32" -> return decodeFloat32(bytes, it)
            "float64" -> return decodeFloat64(bytes, it)
            "boolean" -> return decodeBoolean(bytes, it)
            else -> return Any()
        }
    }

    fun decodeNumber(bytes: ByteArray, it: Iterator): Float {
        val prefix: Int = bytes[it.offset++].toInt() and 0xFF
        if (prefix < 128) {
            // positive fixint
            return prefix.toFloat()
        } else if (prefix == 0xca) {
            // float 32
            return decodeFloat32(bytes, it)
        } else if (prefix == 0xcb) {
            // float 64
            return decodeFloat64(bytes, it).toFloat()
        } else if (prefix == 0xcc) {
            // uint 8
            return decodeUint8(bytes, it).toFloat()
        } else if (prefix == 0xcd) {
            // uint 16
            return decodeUint16(bytes, it).toFloat()
        } else if (prefix == 0xce) {
            // uint 32
            return decodeUint32(bytes, it).toFloat()
        } else if (prefix == 0xcf) {
            // uint 64
            return decodeUint64(bytes, it).toFloat()
        } else if (prefix == 0xd0) {
            // int 8
            return decodeInt8(bytes, it).toFloat()
        } else if (prefix == 0xd1) {
            // int 16
            return decodeInt16(bytes, it).toFloat()
        } else if (prefix == 0xd2) {
            // int 32
            return decodeInt32(bytes, it).toFloat()
        } else if (prefix == 0xd3) {
            // int 64
            return decodeInt64(bytes, it).toFloat()
        } else if (prefix > 0xdf) {
            // negative fixint
            return ((0xff - prefix + 1) * -1).toFloat()
        }
        return Float.NaN
    }

    fun decodeInt8(bytes: ByteArray, it: Iterator): Byte {
        return bytes[it.offset++]
    }

    fun decodeUint8(bytes: ByteArray, it: Iterator): Short {
        return (bytes[it.offset++].toInt() and 0xFF).toShort()
    }

    fun decodeInt16(bytes: ByteArray?, it: Iterator): Short {
        val ret = ByteBuffer.wrap(bytes, it.offset, 2).order(ByteOrder.LITTLE_ENDIAN).short
        it.offset += 2
        return ret
    }

    fun decodeUint16(bytes: ByteArray?, it: Iterator): Int {
        val ret: Int = ByteBuffer.wrap(bytes, it.offset, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xffff
        it.offset += 2
        return ret
    }

    fun decodeInt32(bytes: ByteArray?, it: Iterator): Int {
        val ret = ByteBuffer.wrap(bytes, it.offset, 4).order(ByteOrder.LITTLE_ENDIAN).int
        it.offset += 4
        return ret
    }

    fun decodeUint32(bytes: ByteArray?, it: Iterator): Long {
        val ret = (ByteBuffer.wrap(bytes, it.offset, 4).order(ByteOrder.LITTLE_ENDIAN).int.toLong() and 0xffffffffL)
        it.offset += 4
        return ret
    }

    fun decodeFloat32(bytes: ByteArray?, it: Iterator): Float {
        val ret = ByteBuffer.wrap(bytes, it.offset, 4).order(ByteOrder.LITTLE_ENDIAN).float
        it.offset += 4
        return ret
    }

    fun decodeFloat64(bytes: ByteArray?, it: Iterator): Double {
        val ret = ByteBuffer.wrap(bytes, it.offset, 8).order(ByteOrder.LITTLE_ENDIAN).double
        it.offset += 8
        return ret
    }

    fun decodeInt64(bytes: ByteArray?, it: Iterator): Long {
        val ret = ByteBuffer.wrap(bytes, it.offset, 8).order(ByteOrder.LITTLE_ENDIAN).long
        it.offset += 8
        return ret
    }

    fun decodeUint64(bytes: ByteArray?, it: Iterator): Long {
        // There is no ulong type in Java so let's use long instead ¯\_(ツ)_/¯
        val ret = ByteBuffer.wrap(bytes, it.offset, 8).order(ByteOrder.LITTLE_ENDIAN).long
        it.offset += 8
        return ret
    }

    fun decodeBoolean(bytes: ByteArray, it: Iterator): Boolean {
        return decodeUint8(bytes, it) > 0
    }

    fun decodeString(bytes: ByteArray, it: Iterator): String {
        val prefix: Int = bytes[it.offset++].toInt() and 0xFF
        var length = 0
        if (prefix < 0xc0) {
            // fixstr
            length = prefix and 0x1f
        } else if (prefix == 0xd9) {
            length = decodeUint8(bytes, it).toInt()
        } else if (prefix == 0xda) {
            length = decodeUint16(bytes, it)
        } else if (prefix == 0xdb) {
            length = decodeUint32(bytes, it).toInt()
        }
        val _bytes = ByteArray(length)
        System.arraycopy(bytes, it.offset, _bytes, 0, length)
        val str = String(_bytes, StandardCharsets.UTF_8)
        it.offset += length
        return str
    }

    fun switchStructureCheck(bytes: ByteArray, it: Iterator): Boolean {
        return bytes[it.offset].toInt() and 0xFF == SPEC.SWITCH_TO_STRUCTURE.value
    }

    fun numberCheck(bytes: ByteArray, it: Iterator): Boolean {
        val prefix: Int = bytes[it.offset].toInt() and 0xFF
        return prefix < 0x80 || prefix in 0xca..0xd3
    }
}