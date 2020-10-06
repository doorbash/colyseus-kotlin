package io.colyseus.example.kotlin

import io.colyseus.Client
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

object Main3 {

    val client = Client("ws://localhost:2567")

    @JvmStatic
    fun main(args: Array<String>) {
        for (i in 0..10) connect(i)
        GlobalScope.launch {
            for (i in 10..20) connectAsync(i)
        }

        for (i in 10..20) {
            GlobalScope.launch {
                connectAsync(i)
            }
        }
        for (i in 20..30) connect(i)

        thread { while (true) { } }
    }

    fun connect(i: Int) {
        println("[#$i] connecting...")
        client.joinOrCreate(MyState::class.java, "game", callback = { room ->
            println("[#$i] connected to ${room.name}")
        }, onError = Exception::printStackTrace)
    }

    suspend fun connectAsync(i: Int) {
        println("[#$i] connecting...")
        with(client.joinOrCreate(MyState::class.java, "game")) {
            println("[#$i] connected to $name")
        }
    }
}