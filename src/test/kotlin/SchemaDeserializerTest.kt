import io.colyseus.byteArrayOfInts
import io.colyseus.serializer.SchemaSerializer
import io.colyseus.serializer.schema.ReferenceTracker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class SchemaDeserializerTest {
    @Test
    fun `primitive types test`() {
        val state = schema.primitive_types.PrimitiveTypes()
        val bytes = byteArrayOfInts(128, 128, 129, 255, 130, 0, 128, 131, 255, 255, 132, 0, 0, 0, 128, 133, 255, 255, 255, 255, 134, 0, 0, 0, 0, 0, 0, 0, 128, 135, 255, 255, 255, 255, 255, 255, 31, 0, 136, 204, 204, 204, 253, 137, 255, 255, 255, 255, 255, 255, 239, 127, 138, 208, 128, 139, 204, 255, 140, 209, 0, 128, 141, 205, 255, 255, 142, 210, 0, 0, 0, 128, 143, 203, 0, 0, 224, 255, 255, 255, 239, 65, 144, 203, 0, 0, 0, 0, 0, 0, 224, 195, 145, 203, 255, 255, 255, 255, 255, 255, 63, 67, 146, 203, 61, 255, 145, 224, 255, 255, 239, 199, 147, 203, 153, 153, 153, 153, 153, 153, 185, 127, 148, 171, 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 149, 1)
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
        val state = schema.child_schema_types.ChildSchemaTypes()
        val bytes = byteArrayOfInts(128, 1, 129, 2, 255, 1, 128, 205, 244, 1, 129, 205, 32, 3, 255, 2, 128, 204, 200, 129, 205, 44, 1)
        state.decode(bytes)

        assertEquals(500f, state.child.x)
        assertEquals(800f, state.child.y)

        assertEquals(200f, state.secondChild.x)
        assertEquals(300f, state.secondChild.y)
    }

    @Test
    fun `array schema types test`() {
        val state = schema.array_schema_types.ArraySchemaTypes()
        val bytes = byteArrayOfInts(128, 1, 129, 2, 130, 3, 131, 4, 255, 1, 128, 0, 5, 128, 1, 6, 255, 2, 128, 0, 0, 128, 1, 10, 128, 2, 20, 128, 3, 205, 192, 13, 255, 3, 128, 0, 163, 111, 110, 101, 128, 1, 163, 116, 119, 111, 128, 2, 165, 116, 104, 114, 101, 101, 255, 4, 128, 0, 232, 3, 0, 0, 128, 1, 192, 13, 0, 0, 128, 2, 72, 244, 255, 255, 255, 5, 128, 100, 129, 208, 156, 255, 6, 128, 100, 129, 208, 156)

        state.arrayOfSchemas.onAdd = { value, key -> println("onAdd, arrayOfSchemas => $key") }
        state.arrayOfNumbers.onAdd = { value, key -> println("onAdd, arrayOfNumbers => $key") }
        state.arrayOfStrings.onAdd = { value, key -> println("onAdd, arrayOfStrings => $key") }
        state.arrayOfInt32.onAdd = { value, key -> println("onAdd, arrayOfInt32 => $key") }

        val refs = ReferenceTracker()
        state.decode(bytes, refs = refs)

        assertEquals(2, state.arrayOfSchemas.size)
        assertEquals(100f, state.arrayOfSchemas[0]?.x)
        assertEquals(-100f, state.arrayOfSchemas[0]?.y)
        assertEquals(100f, state.arrayOfSchemas[1]?.x)
        assertEquals(-100f, state.arrayOfSchemas[1]?.y)

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

        val popBytes = byteArrayOfInts(255, 1, 64, 1, 255, 2, 64, 3, 64, 2, 64, 1, 255, 4, 64, 2, 64, 1, 255, 3, 64, 2, 64, 1)
        state.decode(popBytes, refs = refs)

        assertEquals(1, state.arrayOfSchemas.size)
        assertEquals(1, state.arrayOfNumbers.size)
        assertEquals(1, state.arrayOfStrings.size)
        assertEquals(1, state.arrayOfInt32.size)
        println("FINISHED")
    }

    @Test
    fun `map schema types test`() {
        val state = schema.map_schema_types.MapSchemaTypes()
        val bytes = byteArrayOfInts(128, 1, 129, 2, 130, 3, 131, 4, 255, 1, 128, 0, 163, 111, 110, 101, 5, 128, 1, 163, 116, 119, 111, 6, 128, 2, 165, 116, 104, 114, 101, 101, 7, 255, 2, 128, 0, 163, 111, 110, 101, 1, 128, 1, 163, 116, 119, 111, 2, 128, 2, 165, 116, 104, 114, 101, 101, 205, 192, 13, 255, 3, 128, 0, 163, 111, 110, 101, 163, 79, 110, 101, 128, 1, 163, 116, 119, 111, 163, 84, 119, 111, 128, 2, 165, 116, 104, 114, 101, 101, 165, 84, 104, 114, 101, 101, 255, 4, 128, 0, 163, 111, 110, 101, 192, 13, 0, 0, 128, 1, 163, 116, 119, 111, 24, 252, 255, 255, 128, 2, 165, 116, 104, 114, 101, 101, 208, 7, 0, 0, 255, 5, 128, 100, 129, 204, 200, 255, 6, 128, 205, 44, 1, 129, 205, 144, 1, 255, 7, 128, 205, 244, 1, 129, 205, 88, 2)

        state.mapOfSchemas.onAdd = { value, key -> println("OnAdd, mapOfSchemas => $key") }
        state.mapOfNumbers.onAdd = { value, key -> println("OnAdd, mapOfNumbers => $key") }
        state.mapOfStrings.onAdd = { value, key -> println("OnAdd, mapOfStrings => $key") }
        state.mapOfInt32.onAdd = { value, key -> println("OnAdd, mapOfInt32 => $key") }

        state.mapOfSchemas.onRemove = { value, key -> println("OnRemove, mapOfSchemas => $key") }
        state.mapOfNumbers.onRemove = { value, key -> println("OnRemove, mapOfNumbers => $key") }
        state.mapOfStrings.onRemove = { value, key -> println("OnRemove, mapOfStrings => $key") }
        state.mapOfInt32.onRemove = { value, key -> println("OnRemove, mapOfInt32 => $key") }

        val refs = ReferenceTracker()
        state.decode(bytes, refs = refs)

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

        val deleteBytes = byteArrayOfInts(255, 2, 64, 1, 64, 2, 255, 1, 64, 1, 64, 2, 255, 3, 64, 1, 64, 2, 255, 4, 64, 1, 64, 2)
        state.decode(deleteBytes, refs = refs)

        assertEquals(1, state.mapOfSchemas.size)
        assertEquals(1, state.mapOfNumbers.size)
        assertEquals(1, state.mapOfStrings.size)
        assertEquals(1, state.mapOfInt32.size)
    }

    @Test
    fun `map schema int8 test`() {
        val state = schema.map_schema_int8.MapSchemaInt8()
        val bytes = byteArrayOfInts(128, 171, 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 129, 1, 255, 1, 128, 0, 163, 98, 98, 98, 1, 128, 1, 163, 97, 97, 97, 1, 128, 2, 163, 50, 50, 49, 1, 128, 3, 163, 48, 50, 49, 1, 128, 4, 162, 49, 53, 1, 128, 5, 162, 49, 48, 1)

        val refs = ReferenceTracker()
        state.decode(bytes, refs = refs)

        assertEquals("Hello world", state.status)
        assertEquals(1, state.mapOfInt8["bbb"])
        assertEquals(1, state.mapOfInt8["aaa"])
        assertEquals(1, state.mapOfInt8["221"])
        assertEquals(1, state.mapOfInt8["021"])
        assertEquals(1, state.mapOfInt8["15"])
        assertEquals(1, state.mapOfInt8["10"])

        val addBytes = byteArrayOfInts(255, 1, 0, 5, 2)
        state.decode(addBytes, refs = refs)

        assertEquals(1, state.mapOfInt8["bbb"])
        assertEquals(1, state.mapOfInt8["aaa"])
        assertEquals(1, state.mapOfInt8["221"])
        assertEquals(1, state.mapOfInt8["021"])
        assertEquals(1, state.mapOfInt8["15"])
        assertEquals(2, state.mapOfInt8["10"])
    }

    @Test
    fun `inherited types test`() {
        val serializer = SchemaSerializer(schema.inherited_types.InheritedTypes::class.java)
        val handshake = byteArrayOfInts(128, 1, 129, 3, 255, 1, 128, 0, 2, 128, 1, 3, 128, 2, 4, 128, 3, 5, 255, 2, 129, 6, 128, 0, 255, 3, 129, 7, 128, 1, 255, 4, 129, 8, 128, 2, 255, 5, 129, 9, 128, 3, 255, 6, 128, 0, 10, 128, 1, 11, 255, 7, 128, 0, 12, 128, 1, 13, 128, 2, 14, 255, 8, 128, 0, 15, 128, 1, 16, 128, 2, 17, 128, 3, 18, 255, 9, 128, 0, 19, 128, 1, 20, 128, 2, 21, 128, 3, 22, 255, 10, 128, 161, 120, 129, 166, 110, 117, 109, 98, 101, 114, 255, 11, 128, 161, 121, 129, 166, 110, 117, 109, 98, 101, 114, 255, 12, 128, 161, 120, 129, 166, 110, 117, 109, 98, 101, 114, 255, 13, 128, 161, 121, 129, 166, 110, 117, 109, 98, 101, 114, 255, 14, 128, 164, 110, 97, 109, 101, 129, 166, 115, 116, 114, 105, 110, 103, 255, 15, 128, 161, 120, 129, 166, 110, 117, 109, 98, 101, 114, 255, 16, 128, 161, 121, 129, 166, 110, 117, 109, 98, 101, 114, 255, 17, 128, 164, 110, 97, 109, 101, 129, 166, 115, 116, 114, 105, 110, 103, 255, 18, 128, 165, 112, 111, 119, 101, 114, 129, 166, 110, 117, 109, 98, 101, 114, 255, 19, 128, 166, 101, 110, 116, 105, 116, 121, 130, 0, 129, 163, 114, 101, 102, 255, 20, 128, 166, 112, 108, 97, 121, 101, 114, 130, 1, 129, 163, 114, 101, 102, 255, 21, 128, 163, 98, 111, 116, 130, 2, 129, 163, 114, 101, 102, 255, 22, 128, 163, 97, 110, 121, 130, 0, 129, 163, 114, 101, 102)
        serializer.handshake(handshake, 0)

        val bytes = byteArrayOfInts(128, 1, 129, 2, 130, 3, 131, 4, 213, 2, 255, 1, 128, 205, 244, 1, 129, 205, 32, 3, 255, 2, 128, 204, 200, 129, 205, 44, 1, 130, 166, 80, 108, 97, 121, 101, 114, 255, 3, 128, 100, 129, 204, 150, 130, 163, 66, 111, 116, 131, 204, 200, 255, 4, 131, 100)
        serializer.setState(bytes)

        val state = serializer.state

        assertEquals(500f, state.entity.x)
        assertEquals(800f, state.entity.y)

        assertEquals(200f, state.player.x)
        assertEquals(300f, state.player.y)
        assertEquals("Player", state.player.name)

        assertEquals(100f, state.bot.x)
        assertEquals(150f, state.bot.y)
        assertEquals("Bot", state.bot.name)
        assertEquals(200f, state.bot.power)
    }


    @Test
    fun `backwards forwards test`() {
        val statev1bytes = byteArrayOfInts(129, 1, 128, 171, 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 255, 1, 128, 0, 163, 111, 110, 101, 2, 255, 2, 128, 203, 232, 229, 22, 37, 231, 231, 209, 63, 129, 203, 240, 138, 15, 5, 219, 40, 223, 63)
        val statev2bytes = byteArrayOfInts(128, 171, 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 130, 10)

        val statev2 = schema.backwards_forwards.StateV2()
        statev2.decode(statev1bytes)
        assertEquals("Hello world", statev2.str)

        val statev1 = schema.backwards_forwards.StateV1()
        statev1.decode(statev2bytes)
        assertEquals("Hello world", statev1.str)
    }


    @Test
    fun `filtered types test`() {
        val client1 = schema.filtered_types.State()
        client1.decode(byteArrayOfInts(255, 0, 130, 1, 128, 2, 128, 2, 255, 1, 128, 0, 4, 255, 2, 128, 163, 111, 110, 101, 255, 2, 128, 163, 111, 110, 101, 255, 4, 128, 163, 111, 110, 101))
        assertEquals("one", client1.playerOne.name)
        assertEquals("one", client1.players[0]?.name)
        assertEquals(null, client1.playerTwo.name)

        val client2 = schema.filtered_types.State()
        client2.decode(byteArrayOfInts(255, 0, 130, 1, 129, 3, 129, 3, 255, 1, 128, 1, 5, 255, 3, 128, 163, 116, 119, 111, 255, 3, 128, 163, 116, 119, 111, 255, 5, 128, 163, 116, 119, 111))
        assertEquals("two", client2.playerTwo.name)
        assertEquals("two", client2.players[1]?.name)
        assertEquals(null, client2.playerOne.name)
    }


    @Test
    fun `instance sharing types`() {
        val refs = ReferenceTracker()
        val client = schema.instance_sharing_types.State()

        client.decode(byteArrayOfInts(130, 1, 131, 2, 128, 3, 129, 3, 255, 1, 255, 2, 255, 3, 128, 4, 255, 3, 128, 4, 255, 4, 128, 10, 129, 10, 255, 4, 128, 10, 129, 10), refs = refs)
        assertEquals(client.player1, client.player2)
        assertEquals(client.player1.position, client.player2.position)
        assertEquals(2, refs.refCounts[client.player1.__refId])
        assertEquals(5, refs.refs.size)

        client.decode(byteArrayOfInts(130, 1, 131, 2, 64, 65), refs = refs)
        assertEquals(null, client.player1)
        assertEquals(null, client.player2)
        assertEquals(3, refs.refs.size)

        client.decode(byteArrayOfInts(255, 1, 128, 0, 5, 128, 1, 5, 128, 2, 5, 128, 3, 6, 255, 5, 128, 7, 255, 6, 128, 8, 255, 7, 128, 10, 129, 10, 255, 8, 128, 10, 129, 10), refs = refs)
        assertEquals(client.arrayOfPlayers[0], client.arrayOfPlayers[1])
        assertEquals(client.arrayOfPlayers[1], client.arrayOfPlayers[2])
        assertNotEquals(client.arrayOfPlayers[3], client.arrayOfPlayers[2])
        assertEquals(7, refs.refs.size)

        client.decode(byteArrayOfInts(255, 1, 64, 3, 64, 2, 64, 1), refs = refs)
        assertEquals(1, client.arrayOfPlayers.size)
        assertEquals(5, refs.refs.size)
        val previousArraySchemaRefId = client.arrayOfPlayers.__refId

        // Replacing ArraySchema
        client.decode(byteArrayOfInts(130, 9, 255, 9, 128, 0, 10, 255, 10, 128, 11, 255, 11, 128, 10, 129, 20), refs = refs)
        assertEquals(false, refs.refs.containsKey(previousArraySchemaRefId))
        assertEquals(1, client.arrayOfPlayers.size)
        assertEquals(5, refs.refs.size)

        // Clearing ArraySchema
        client.decode(byteArrayOfInts(255, 9, 10), refs = refs)
        assertEquals(0, client.arrayOfPlayers.size)
        assertEquals(3, refs.refs.size)

    }
}