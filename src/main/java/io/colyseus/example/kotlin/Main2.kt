package io.colyseus.example.kotlin

import io.colyseus.Client
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema

fun main() {
    val client = Client("ws://localhost:2567")
    client.joinOrCreate(MyState::class.java, "game", callback = { room ->
        with(room) {
            println("connected to $name")

//            state.onChange = { changes -> println(changes) }
            state.onRemove = { println("state.onRemove") }

            state.players.onAdd = { player: Player?, key: Int? ->
                println("player added: " + key + "  " + player?.x)
            }

            state.players.onRemove = { player: Player?, key: Int? ->
                println("player removed: " + key + "  " + player?.x)

                send(2, Cell().apply { x = 100f; y = 200f })
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

            onMessage { primitives: PrimitivesTest ->
                println("some primitives...")
                println(primitives._string)
            }

//            onMessage("hello") { data : Float ->
//                println(data)
//            }
//
            onMessage("hi") { cells: MapSchema<Cell> ->
                println("map size is ")
                println(cells.size)
                println(cells)
            }

            onMessage("hey") { players: ArraySchema<Player> ->
                println("player array size is ")
                println(players.size)
                println(players)
            }

            onMessage("ahoy") { cell: Cell ->
                println("""cell.x = ${cell.x} ,cell.y = ${cell.y}""")
            }

            onMessage(2) { cell: Cell ->
                println("handler for type 2")
                println(" >>>>>>>>>>>>>> " + cell.x)
            }
        }
    }, onError = { e ->
        e.printStackTrace()
    })

    readLine()
}