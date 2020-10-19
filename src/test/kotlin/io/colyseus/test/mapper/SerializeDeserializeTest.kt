package io.colyseus.test.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import io.colyseus.test.mapper.types.PrimitivesTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.msgpack.jackson.dataformat.MessagePackFactory

class SerializeDeserializeTest {
    @Test
    fun `json serialize test`() {
        val objectMapper = ObjectMapper()

        PrimitivesTest().apply {
            _uint8 = 0xFF
            _uint16 = 0xFFFF
            _uint32 = 0xFFFFFF
            _uint64 = 0xFFFFFFFF
            _int8 = Byte.MAX_VALUE
            _int16 = Short.MAX_VALUE
            _int32 = Int.MAX_VALUE
            _int64 = Long.MAX_VALUE
            _float32_1 = Float.MIN_VALUE
            _float32_2 = Float.MAX_VALUE
            _float64_1 = Double.MIN_VALUE
            _float64_2 = Double.MAX_VALUE
            _boolean = true
            _string1 = "hello world!"
            _string2 = null
        }.apply {
            val serialized = objectMapper.writeValueAsString(this)

            println(serialized)

            val deserialized = objectMapper.readValue(serialized, LinkedHashMap::class.java)

            assertEquals(15, deserialized.keys.size)
            assertEquals(0xFF, deserialized["_uint8"])
            assertEquals(0xFFFF, deserialized["_uint16"])
            assertEquals(0xFFFFFF, deserialized["_uint32"])
            assertEquals(0xFFFFFFFF, deserialized["_uint64"])
            assertEquals(Byte.MAX_VALUE.toInt(), deserialized["_int8"])
            assertEquals(Short.MAX_VALUE.toInt(), deserialized["_int16"])
            assertEquals(Int.MAX_VALUE, deserialized["_int32"])
            assertEquals(Long.MAX_VALUE, deserialized["_int64"])
            assertEquals(Float.MIN_VALUE.toString().toDouble(), deserialized["_float32_1"])
            assertEquals(Float.MAX_VALUE.toString().toDouble(), deserialized["_float32_2"])
            assertEquals(Double.MIN_VALUE, deserialized["_float64_1"])
            assertEquals(Double.MAX_VALUE, deserialized["_float64_2"])
            assertEquals(true, deserialized["_boolean"])
            assertEquals("hello world!", deserialized["_string1"])
            assertEquals(null, deserialized["_string2"])
        }
    }

    @Test
    fun `json deserialize test`() {
        val objectMapper = ObjectMapper()
        val serialized = """
                {
                   "_uint8":255,
                   "_uint16":65535,
                   "_uint32":16777215,
                   "_uint64":4294967295,
                   "_int8":127,
                   "_int16":32767,
                   "_int32":2147483647,
                   "_int64":9223372036854775807,
                   "_float32_1":1.4E-45,
                   "_float32_2":3.4028235E38,
                   "_float64_1":4.9E-324,
                   "_float64_2":1.7976931348623157E308,
                   "_boolean":true,
                   "_string1":"hello world!",
                   "_string2":null
                }""".trimIndent()

        with(objectMapper.readValue(serialized, PrimitivesTest::class.java)) {
            assertEquals(0xFF, _uint8)
            assertEquals(0xFFFF, _uint16)
            assertEquals(0xFFFFFF, _uint32)
            assertEquals(0xFFFFFFFF, _uint64)
            assertEquals(Byte.MAX_VALUE, _int8)
            assertEquals(Short.MAX_VALUE, _int16)
            assertEquals(Int.MAX_VALUE, _int32)
            assertEquals(Long.MAX_VALUE, _int64)
            assertEquals(Float.MIN_VALUE, _float32_1)
            assertEquals(Float.MAX_VALUE, _float32_2)
            assertEquals(Double.MIN_VALUE, _float64_1)
            assertEquals(Double.MAX_VALUE, _float64_2)
            assertEquals(true, _boolean)
            assertEquals("hello world!", _string1)
            assertEquals(null, _string2)
        }
    }

    @Test
    fun `messagepack serialize test`() {
        val objectMapper = ObjectMapper(MessagePackFactory())

        PrimitivesTest().apply {
            _uint8 = 0xFF
            _uint16 = 0xFFFF
            _uint32 = 0xFFFFFF
            _uint64 = 0xFFFFFFFF
            _int8 = Byte.MAX_VALUE
            _int16 = Short.MAX_VALUE
            _int32 = Int.MAX_VALUE
            _int64 = Long.MAX_VALUE
            _float32_1 = Float.MIN_VALUE
            _float32_2 = Float.MAX_VALUE
            _float64_1 = Double.MIN_VALUE
            _float64_2 = Double.MAX_VALUE
            _boolean = true
            _string1 = "hello world!"
            _string2 = null
        }.apply {
            val serialized = objectMapper.writeValueAsBytes(this)

            println(serialized.contentToString())

            val deserialized = objectMapper.readValue(serialized, LinkedHashMap::class.java)

            assertEquals(15, deserialized.keys.size)
            assertEquals(0xFF, deserialized["_uint8"])
            assertEquals(0xFFFF, deserialized["_uint16"])
            assertEquals(0xFFFFFF, deserialized["_uint32"])
            assertEquals(0xFFFFFFFF, deserialized["_uint64"])
            assertEquals(Byte.MAX_VALUE.toInt(), deserialized["_int8"])
            assertEquals(Short.MAX_VALUE.toInt(), deserialized["_int16"])
            assertEquals(Int.MAX_VALUE, deserialized["_int32"])
            assertEquals(Long.MAX_VALUE, deserialized["_int64"])
            assertEquals(Float.MIN_VALUE.toDouble(), deserialized["_float32_1"])
            assertEquals(Float.MAX_VALUE.toDouble(), deserialized["_float32_2"])
            assertEquals(Double.MIN_VALUE, deserialized["_float64_1"])
            assertEquals(Double.MAX_VALUE, deserialized["_float64_2"])
            assertEquals(true, deserialized["_boolean"])
            assertEquals("hello world!", deserialized["_string1"])
            assertEquals(null, deserialized["_string2"])
        }
    }

    @Test
    fun `messagepack deserialize test`() {
        val objectMapper = ObjectMapper(MessagePackFactory())
        val serialized = byteArrayOf(-113, -90, 95, 117, 105, 110, 116, 56, -52, -1, -89, 95, 117, 105, 110, 116, 49, 54, -51, -1, -1, -89, 95, 117, 105, 110, 116, 51, 50, -50, 0, -1, -1, -1, -89, 95, 117, 105, 110, 116, 54, 52, -50, -1, -1, -1, -1, -91, 95, 105, 110, 116, 56, 127, -90, 95, 105, 110, 116, 49, 54, -51, 127, -1, -90, 95, 105, 110, 116, 51, 50, -50, 127, -1, -1, -1, -90, 95, 105, 110, 116, 54, 52, -49, 127, -1, -1, -1, -1, -1, -1, -1, -86, 95, 102, 108, 111, 97, 116, 51, 50, 95, 49, -54, 0, 0, 0, 1, -86, 95, 102, 108, 111, 97, 116, 51, 50, 95, 50, -54, 127, 127, -1, -1, -86, 95, 102, 108, 111, 97, 116, 54, 52, 95, 49, -53, 0, 0, 0, 0, 0, 0, 0, 1, -86, 95, 102, 108, 111, 97, 116, 54, 52, 95, 50, -53, 127, -17, -1, -1, -1, -1, -1, -1, -88, 95, 98, 111, 111, 108, 101, 97, 110, -61, -88, 95, 115, 116, 114, 105, 110, 103, 49, -84, 104, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33, -88, 95, 115, 116, 114, 105, 110, 103, 50, -64)

        with(objectMapper.readValue(serialized, PrimitivesTest::class.java)) {
            assertEquals(0xFF, _uint8)
            assertEquals(0xFFFF, _uint16)
            assertEquals(0xFFFFFF, _uint32)
            assertEquals(0xFFFFFFFF, _uint64)
            assertEquals(Byte.MAX_VALUE, _int8)
            assertEquals(Short.MAX_VALUE, _int16)
            assertEquals(Int.MAX_VALUE, _int32)
            assertEquals(Long.MAX_VALUE, _int64)
            assertEquals(Float.MIN_VALUE, _float32_1)
            assertEquals(Float.MAX_VALUE, _float32_2)
            assertEquals(Double.MIN_VALUE, _float64_1)
            assertEquals(Double.MAX_VALUE, _float64_2)
            assertEquals(true, _boolean)
            assertEquals("hello world!", _string1)
            assertEquals(null, _string2)
        }
    }
}