package io.colyseus.test.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import io.colyseus.test.mapper.types.Cell
import io.colyseus.test.mapper.types.MyState
import io.colyseus.test.mapper.types.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.msgpack.jackson.dataformat.MessagePackFactory

class SerializeDeserializeTest {
    @Test
    fun `json serialize test`() {
        val objectMapper = ObjectMapper()

        val serialized = objectMapper.writeValueAsString(createMyState())

        println(serialized)

        val deserialized = objectMapper.readValue(serialized, LinkedHashMap::class.java)

        assertMyState(deserialized as LinkedHashMap<String, Any>)
    }

    @Test
    fun `msgpack serialize test`() {
        val objectMapper = ObjectMapper(MessagePackFactory())

        val serialized = objectMapper.writeValueAsBytes(createMyState())

        println(serialized.contentToString())

        val deserialized = objectMapper.readValue(serialized, LinkedHashMap::class.java)

        assertMyState(deserialized as LinkedHashMap<String, Any>)
    }

    fun createMyState(): MyState {
        return MyState().apply {
            player = Player().apply {
                _uint8 = 0xFF
                _uint16 = 0xFFFF
                _uint32 = 0xFFFFFF
                _uint64 = 0xFFFFFFFF
                _int8 = 100.toByte()
                _int16 = 100.toShort()
                _int32 = 100
                _int64 = 100L
                _float32_1 = 100f
                _float32_2 = 200f
                _float64_1 = 100.0
                _float64_2 = 200.0
                _number = 100f
                _boolean = true
                _string1 = "hello world!"
                _string2 = ""
                _string3 = null

                with(arrayOfCells) {
                    repeat(5) {
                        add(Cell().apply { x = 100f; y = 200f; })
                    }
                }

                with(mapOfCells) {
                    var i = 0
                    repeat(5) {
                        put("index_$i", Cell().apply { x = 100f; y = 200f; })
                        i++
                    }
                }

                with(arrayOfPrimitives) {
                    repeat(10) {
                        add(Math.random().toFloat() * 1000)
                    }
                }
                with(mapOfPrimitives) {
                    var i = 0
                    repeat(10) {
                        put("index_$i", Math.random().toFloat() * 1000)
                        i++
                    }
                }
            }
            with(arrayOfPlayers) {
                repeat(5) {
                    add(player)
                }
            }
            with(mapOfPlayers) {
                var i = 0
                repeat(5) {
                    put("index_$i", player)
                    i++
                }
            }
            with(arrayOfPrimitives) {
                repeat(10) {
                    add(Math.random().toFloat() * 1000)
                }
            }
            with(mapOfPrimitives) {
                var i = 0
                repeat(10) {
                    put("index_$i", Math.random().toFloat() * 1000)
                    i++
                }
            }
        }
    }

    fun assertMyState(myState: LinkedHashMap<String, Any>) {
        assertEquals(5, myState.keys.size)
        assertPlayer(myState["player"] as LinkedHashMap<String, Any>)
        with(myState["arrayOfPlayers"] as ArrayList<LinkedHashMap<String, Any>>) {
            assertEquals(5, size)
            forEach {
                assertPlayer(it)
            }
        }
        with(myState["mapOfPlayers"] as LinkedHashMap<String, Any>) {
            assertEquals(5, size)
            forEach {
                assertPlayer(it.value as LinkedHashMap<String, Any>)
            }
        }
        with(myState["arrayOfPrimitives"] as ArrayList<Any>) {
            assertEquals(10, size)
            forEach {
                assertEquals(true, it is Double)
            }
        }
        with(myState["mapOfPrimitives"] as LinkedHashMap<String, Any>) {
            assertEquals(10, size)
            forEach {
                assertEquals(true, it.value is Double)
            }
        }
    }

    fun assertPlayer(player: LinkedHashMap<String, Any>) {
        assertEquals(21, player.keys.size)
        assertEquals(0xFF, player["_uint8"])
        assertEquals(0xFFFF, player["_uint16"])
        assertEquals(0xFFFFFF, player["_uint32"])
        assertEquals(0xFFFFFFFF, player["_uint64"])
        assertEquals(100, player["_int8"])
        assertEquals(100, player["_int16"])
        assertEquals(100, player["_int32"])
        assertEquals(100, player["_int64"])
        assertEquals(100.0, player["_float32_1"])
        assertEquals(200.0, player["_float32_2"])
        assertEquals(100.0, player["_float64_1"])
        assertEquals(200.0, player["_float64_2"])
        assertEquals(100.0, player["_number"])
        assertEquals(true, player["_boolean"])
        assertEquals("hello world!", player["_string1"])
        assertEquals("", player["_string2"])
        assertEquals(null, player["_string3"])

        with(player["arrayOfCells"] as ArrayList<LinkedHashMap<String, Any>>) {
            assertEquals(5, size)
            forEach {
                assertCell(it)
            }
        }
        with(player["mapOfCells"] as LinkedHashMap<String, Any>) {
            assertEquals(5, size)
            forEach {
                assertCell(it.value as LinkedHashMap<String, Any>)
            }
        }
        with(player["arrayOfPrimitives"] as ArrayList<Any>) {
            assertEquals(10, size)
            forEach {
                assertEquals(true, it is Double)
            }
        }
        with(player["mapOfPrimitives"] as LinkedHashMap<String, Any>) {
            assertEquals(10, size)
            forEach {
                assertEquals(true, it.value is Double)
            }
        }
    }

    fun assertCell(cell: LinkedHashMap<String, Any>) {
        assertEquals(2, cell.keys.size)
        assertEquals(100.0, cell["x"])
        assertEquals(200.0, cell["y"])
    }
}