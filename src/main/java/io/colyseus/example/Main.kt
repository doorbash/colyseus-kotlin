package io.colyseus.example

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import io.colyseus.Client
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.Schema.onRemove
import kotlinx.coroutines.runBlocking
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val client = Client("ws://localhost:2567")
        with(client.joinOrCreate("game", MyState::class.java)) {
            println("connected to $name")

//            state.onChange = onChange { changes: List<Change?>? -> println(changes) }
            state.onRemove = onRemove { println("state.onRemove") }

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