package io.colyseus.example.kotlin

import io.colyseus.Client
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema
import kotlinx.coroutines.runBlocking

object Main {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val client = Client("ws://localhost:2567")
        with(client.joinOrCreate("game")) {
            println("connected to $name")

//            state.onChange = { changes -> println(changes) }
            state.onRemove = { println("state.onRemove") }

            (state["players"] as ArraySchema<*>).onAdd = { player: Any?, key: Int ->
                player as Schema
                println("player added: " + key + "  " + (player["x"]))
            }

            (state["players"] as ArraySchema<*>).onRemove = { player: Any?, key: Int? ->
                player as Schema
                println("player removed: " + key + "  " + player["x"])

                send("mmmmm", Schema().apply {
                    this["x"] = 100f
                    this["y"] = 200f
                })
            }

            onLeave = { code -> println("onLeave $code") }
            onError = { code, message ->
                println("onError")
                println(code)
                println(message)
            }
            onJoin = { println("onJoin") }

//            onStateChange = { state, isFirstState ->
//                println("OnStateChange")
//                println(state.players.size)
//                println(isFirstState)
//            }

//            onMessage("xxxxx") { message: Player ->
//                println("xxxxx!!! >> " + message.x)
//            }

            onMessage { primitives: Schema ->
                println(primitives["_string"])
            }

//            onMessage("hello") { data : Float ->
//                println(data)
//            }
//
            onMessage("hi") { cells: MapSchema<Schema> ->
                println("map size is ")
                println(cells.size)
                println(cells)
            }

            onMessage("hey") { players: ArraySchema<Schema> ->
                println("player array size is ")
                println(players.size)
                println(players)
            }

            onMessage("ahoy") { cell: Schema ->
                println("""cell.x = ${cell["x"]} ,cell.y = ${cell["y"]}""")
            }

            onMessage("hello") { cell: Schema ->
                println("handler for type 2")
                println(" >>>>>>>>>>>>>> " + cell["x"])
            }


            // Send message with message type
            send("fire", Math.random() * 100)

            // Send a schema with type id
            val c = Schema()
            c["x"] = 100f
            c["y"] = 200f
            send(2, c)

            // Send only the message type or type id
            send("hello")
            send(3)
        }
    }
}