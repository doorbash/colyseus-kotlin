import io.colyseus.byteArrayOfInts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import schema.array_schema_types.ArraySchemaTypes
import schema.child_schema_types.ChildSchemaTypes
import schema.map_schema_types.MapSchemaTypes
import schema.primitive_types.PrimitiveTypes

class SchemaDeserializerTest {
    @Test
    fun `primitive types test`() {
        val state = PrimitiveTypes()
        val bytes = byteArrayOfInts(0, 128, 1, 255, 2, 0, 128, 3, 255, 255, 4, 0, 0, 0, 128, 5, 255, 255, 255, 255, 6, 0, 0, 0, 0, 0, 0, 0, 128, 7, 255, 255, 255, 255, 255, 255, 31, 0, 8, 204, 204, 204, 253, 9, 255, 255, 255, 255, 255, 255, 239, 127, 10, 208, 128, 11, 204, 255, 12, 209, 0, 128, 13, 205, 255, 255, 14, 210, 0, 0, 0, 128, 15, 203, 0, 0, 224, 255, 255, 255, 239, 65, 16, 203, 0, 0, 0, 0, 0, 0, 224, 195, 17, 203, 255, 255, 255, 255, 255, 255, 63, 67, 18, 203, 61, 255, 145, 224, 255, 255, 239, 199, 19, 203, 153, 153, 153, 153, 153, 153, 185, 127, 20, 171, 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 21, 1)
        state.decode(bytes)

        assertEquals(-128, state.int8)
        assertEquals(255, state.uint8)
        assertEquals(-32768, state.int16)
        assertEquals(65535, state.uint16)
        assertEquals(-2147483648, state.int32)
        assertEquals(4294967295, state.uint32)
//        assertEquals(-9223372036854775808, state.int64)
        assertEquals(9007199254740991, state.uint64)
        assertEquals(-3.40282347E+37f, state.float32)
        assertEquals(1.7976931348623157e+308, state.float64)

        assertEquals(-128f, state.varint_int8)
        assertEquals(255f, state.varint_uint8)
        assertEquals(-32768f, state.varint_int16)
        assertEquals(65535f, state.varint_uint16)
        assertEquals(-2147483648f, state.varint_int32)
        assertEquals(4294967295f, state.varint_uint32)
//        assertEquals(-9223372036854775808, state.varint_int64)
        assertEquals(9007199254740991f, state.varint_uint64)
        assertEquals(-3.40282347E+38f, state.varint_float32)
//        assertEquals(Mathf.Infinity, state.varint_float64)

        assertEquals("Hello world", state.str)
        assertEquals(true, state.boolean)
    }

    @Test
    fun `child schema types test`() {
        val state = ChildSchemaTypes()
        val bytes = byteArrayOfInts(0, 0, 205, 244, 1, 1, 205, 32, 3, 193, 1, 0, 204, 200, 1, 205, 44, 1, 193)
        state.decode(bytes)

        assertEquals(500f, state.child.x)
        assertEquals(800f, state.child.y)

        assertEquals(200f, state.secondChild.x)
        assertEquals(300f, state.secondChild.y)
    }

    @Test
    fun `array schema types test`() {
        val state = ArraySchemaTypes()
        val bytes = byteArrayOfInts(0, 2, 2, 0, 0, 100, 1, 208, 156, 193, 1, 0, 100, 1, 208, 156, 193, 1, 4, 4, 0, 0, 1, 10, 2, 20, 3, 205, 192, 13, 2, 3, 3, 0, 163, 111, 110, 101, 1, 163, 116, 119, 111, 2, 165, 116, 104, 114, 101, 101, 3, 3, 3, 0, 232, 3, 0, 0, 1, 192, 13, 0, 0, 2, 72, 244, 255, 255)

        state.arrayOfSchemas.onAdd = { value, key -> println("onAdd, arrayOfSchemas => $key") }
        state.arrayOfNumbers.onAdd = { value, key -> println("onAdd, arrayOfNumbers => $key") }
        state.arrayOfStrings.onAdd = { value, key -> println("onAdd, arrayOfStrings => $key") }
        state.arrayOfInt32.onAdd = { value, key -> println("onAdd, arrayOfInt32 => $key") }
        state.decode(bytes)

        assertEquals(2, state.arrayOfSchemas.size)
        assertEquals(100f, state.arrayOfSchemas[0].x)
        assertEquals(-100f, state.arrayOfSchemas[0].y)
        assertEquals(100f, state.arrayOfSchemas[1].x)
        assertEquals(-100f, state.arrayOfSchemas[1].y)

        assertEquals(4, state.arrayOfNumbers.size)
        assertEquals(0f, state.arrayOfNumbers[0])
        assertEquals(10f, state.arrayOfNumbers[1])
        assertEquals(20f, state.arrayOfNumbers[2])
        assertEquals(3520f, state.arrayOfNumbers[3])

        assertEquals(3, state.arrayOfStrings.size)
        assertEquals("one", state.arrayOfStrings[0])
        assertEquals("two", state.arrayOfStrings[1])
        assertEquals("three", state.arrayOfStrings[2])

        assertEquals(3, state.arrayOfInt32.size)
        assertEquals(1000, state.arrayOfInt32[0])
        assertEquals(3520, state.arrayOfInt32[1])
        assertEquals(-3000, state.arrayOfInt32[2])

        state.arrayOfSchemas.onRemove = { value, key -> println("onRemove, arrayOfSchemas => $key") }
        state.arrayOfNumbers.onRemove = { value, key -> println("onRemove, arrayOfNumbers => $key") }
        state.arrayOfStrings.onRemove = { value, key -> println("onRemove, arrayOfStrings => $key") }
        state.arrayOfInt32.onRemove = { value, key -> println("onRemove, arrayOfInt32 => $key") }

        val popBytes = byteArrayOfInts(0, 1, 0, 1, 1, 0, 3, 1, 0, 2, 1, 0)
        state.decode(popBytes)

        assertEquals(1, state.arrayOfSchemas.size)
        assertEquals(1, state.arrayOfNumbers.size)
        assertEquals(1, state.arrayOfStrings.size)
        assertEquals(1, state.arrayOfInt32.size)
        println("FINISHED")
    }

    @Test
    fun `map schema types test`() {
        val state = MapSchemaTypes()
        val bytes = byteArrayOfInts(0, 3, 163, 111, 110, 101, 0, 100, 1, 204, 200, 193, 163, 116, 119, 111, 0, 205, 44, 1, 1, 205, 144, 1, 193, 165, 116, 104, 114, 101, 101, 0, 205, 244, 1, 1, 205, 88, 2, 193, 1, 3, 163, 111, 110, 101, 1, 163, 116, 119, 111, 2, 165, 116, 104, 114, 101, 101, 205, 192, 13, 2, 3, 163, 111, 110, 101, 163, 79, 110, 101, 163, 116, 119, 111, 163, 84, 119, 111, 165, 116, 104, 114, 101, 101, 165, 84, 104, 114, 101, 101, 3, 3, 163, 111, 110, 101, 192, 13, 0, 0, 163, 116, 119, 111, 24, 252, 255, 255, 165, 116, 104, 114, 101, 101, 208, 7, 0, 0)

        state.mapOfSchemas.onAdd = { value, key -> println("OnAdd, mapOfSchemas => $key") }
        state.mapOfNumbers.onAdd = { value, key -> println("OnAdd, mapOfNumbers => $key") }
        state.mapOfStrings.onAdd = { value, key -> println("OnAdd, mapOfStrings => $key") }
        state.mapOfInt32.onAdd = { value, key -> println("OnAdd, mapOfInt32 => $key") }

        state.mapOfSchemas.onRemove = { value, key -> println("OnRemove, mapOfSchemas => $key") }
        state.mapOfNumbers.onRemove = { value, key -> println("OnRemove, mapOfNumbers => $key") }
        state.mapOfStrings.onRemove = { value, key -> println("OnRemove, mapOfStrings => $key") }
        state.mapOfInt32.onRemove = { value, key -> println("OnRemove, mapOfInt32 => $key") }

        state.decode(bytes)

        assertEquals(3, state.mapOfSchemas.size)
        assertEquals(100f, state.mapOfSchemas["one"]?.x)
        assertEquals(200f, state.mapOfSchemas["one"]?.y)
        assertEquals(300f, state.mapOfSchemas["two"]?.x)
        assertEquals(400f, state.mapOfSchemas["two"]?.y)
        assertEquals(500f, state.mapOfSchemas["three"]?.x)
        assertEquals(600f, state.mapOfSchemas["three"]?.y)

        assertEquals(3, state.mapOfNumbers.size)
        assertEquals(1f, state.mapOfNumbers["one"])
        assertEquals(2f, state.mapOfNumbers["two"])
        assertEquals(3520f, state.mapOfNumbers["three"])

        assertEquals(3, state.mapOfStrings.size)
        assertEquals("One", state.mapOfStrings["one"])
        assertEquals("Two", state.mapOfStrings["two"])
        assertEquals("Three", state.mapOfStrings["three"])

        assertEquals(3, state.mapOfInt32.size)
        assertEquals(3520, state.mapOfInt32["one"])
        assertEquals(-1000, state.mapOfInt32["two"])
        assertEquals(2000, state.mapOfInt32["three"])

        val deleteBytes = byteArrayOfInts(1, 2, 192, 1, 192, 2, 0, 2, 192, 1, 192, 2, 2, 2, 192, 1, 192, 2, 3, 2, 192, 1, 192, 2)
        state.decode(deleteBytes)

        assertEquals(1, state.mapOfSchemas.size)
        assertEquals(1, state.mapOfNumbers.size)
        assertEquals(1, state.mapOfStrings.size)
        assertEquals(1, state.mapOfInt32.size)
    }
}