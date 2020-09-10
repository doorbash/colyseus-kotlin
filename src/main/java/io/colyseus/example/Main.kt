package io.colyseus.example

import io.colyseus.Client
import kotlinx.coroutines.runBlocking

object Main {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val client = Client("ws://localhost:2567")
        with(client.joinOrCreate("game", MyState::class.java)) {
            println("connected to $name")

//            state.onChange = { changes -> println(changes) }
            state.onRemove = { println("state.onRemove") }

            state.cells.onChange = {
               cell : Cell?, key: String? ->
                println(key + "  " + cell?.x)
            }

            onLeave = { code -> println("onLeave $code") }
            onError = { code, message ->
                println("onError")
                println(code)
                println(message)
            }
            onJoin = { println("onJoin") }

            onStateChange = { state, isFirstState ->
//                println("OnStateChange")
//                println(state)
//                println(isFirstState)
            }

//            onMessage("xxxxx") { message: Player ->
//                println("xxxxx!!! >> " + message.x)
//            }

            onMessage { primitives : PrimitivesTest ->
                println("some primitives...")
                println(primitives._string)
            }

//            onMessage("hello") { data : Schema.ArraySchema<Player> ->
//                println("size is ")
//                println(data.size)
//            }
//
//            onMessage("hi") { data : Schema.MapSchema<Cell> ->
//                println("map size is ")
//                println(data.size)
//                println(data)
//            }

//            onMessage("yo") {
//                x : Int ->
//                println("yo x = $x")
//            }
        }
    }
}