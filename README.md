# Colyseus-Kotlin

Implementation of Colyseus client using Kotlin without Reflection

## Download

```groovy
dependencies {
    implementation 'io.github.doorbash:colyseus-kotlin-no-reflection:0.14.0-alpha'
}
```

## How to

### Join or create a room:

```Kotlin
val client = Client("ws://localhost:2567")
with(client.joinOrCreate("game")) {
    println("connected to $name")

    // setup listeners
}
```

### Setup listeners

```Kotlin
/* Room listeners */
onLeave = { code -> println("onLeave $code") }

onError = { code, message ->
    println("onError $code $message")
}

onJoin = { println("onJoin") }

onStateChange = { state, isFirstState ->
    println("OnStateChange")
    println(isFirstState)
}

/* Schema listeners */
(state["primitives"] as Schema).onChange = { changes ->
    for (change in changes) {
        with(change!!) {
            println("$field: $previousValue -> $value")
        }
    }
}

(state["players"] as ArraySchema<*>).onAdd = { player, key ->
    println("player added: " + key + "  " + (player as Schema)["x"])
}

(state["players"] as ArraySchema<*>).onRemove = { player, key ->
    println("player removed: " + key + "  " + (player as Schema)["x"])
}

/* Message Listeners */
//Set listener by message type
onMessage("hi") { cells: MapSchema<Schema> ->
    println("cells size is ${cells.size}")
    println(cells)
}

// Set listener by type
onMessage { primitives: Schema ->
    println("some primitives...")
    println(primitives["_string"])
}

// Set listener by type id
onMessage(2) { cell: Schema ->
    println("handler for type 2")
    println(cell["x"])
}
```

### Send message to server:

```Kotlin
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
```

## Usage examples

- [hexy](https://github.com/doorbash/hexy) - MMO game made with Libgdx
- [agar-io](https://github.com/doorbash/agar-io) - A simple agar.io clone made with Libgdx

## License

MIT
