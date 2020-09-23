# Colyseus-Kotlin

Implementation of Colyseus client using Kotlin

## Download

```groovy
dependencies {
    implementation 'io.github.doorbash:colyseus-kotlin:0.14.0-alpha.1'
}
```

## How to

### Define schema class

```Kotlin
class MyState : Schema() {
    @SchemaField("0/ref", PrimitivesTest::class)
    var primitives = PrimitivesTest()

    @SchemaField("1/array/ref", Player::class)
    var players = ArraySchema(Player::class.java)

    @SchemaField("2/map/ref", Cell::class)
    var cells = MapSchema(Cell::class.java)
}

class Cell : Schema() {
    @SchemaField("0/float32")
    var x = Float.default

    @SchemaField("1/float32")
    var y = Float.default
}

class PrimitivesTest : Schema() {
    @SchemaField("0/uint8")
    var _uint8 = Short.default

    @SchemaField("1/uint16")
    var _uint16 = Int.default

    @SchemaField("2/uint32")
    var _uint32 = Long.default

    @SchemaField("3/uint64")
    var _uint64 = Long.default

    @SchemaField("4/int8")
    var _int8 = Byte.default

    @SchemaField("5/int16")
    var _int16 = Short.default

    @SchemaField("6/int32")
    var _int32 = Int.default

    @SchemaField("7/int64")
    var _int64 = Long.default

    @SchemaField("8/float32")
    var _float32_n = Float.default

    @SchemaField("9/float32")
    var _float32_p = Float.default

    @SchemaField("10/float64")
    var _float64_n = Double.default

    @SchemaField("11/float64")
    var _float64_p = Double.default

    @SchemaField("12/boolean")
    var _boolean = Boolean.default

    @SchemaField("13/string")
    var _string = String.default
}

class Player : Schema() {
    @SchemaField("0/int32")
    var x = Int.default
}
```

### Join or create a room:

```Kotlin
val client = Client("ws://localhost:2567")
with(client.joinOrCreate(MyState::class.java, "game")) {
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
state.primitives.onChange = { changes ->
    for (change in changes!!) {
        with(change!!) {
            println("$Field: $PreviousValue -> $Value")
        }
    }
}

state.players.onAdd = { player: Player?, key: Int? ->
    println("player added: " + key + "  " + player?.x)
}

state.players.onRemove = { player: Player?, key: Int? ->
    println("player removed: " + key + "  " + player?.x)
}

/* Message Listeners */
//Set listener by message type
onMessage("hi") { cells: MapSchema<Cell> ->
    println("cells size is ${cells.size}")
    println(cells)
}

// Set listener by type
onMessage { primitives: PrimitivesTest ->
    println("some primitives...")
    println(primitives._string)
}

// Set listener by type id
onMessage(2) { cell: Cell ->
    println("handler for type 2")
    println(cell.x)
}
```

### Send message to server:

```Kotlin
// Send message with message type
send("fire", Math.random() * 100)

// Send a schema with type id
val c = Cell()
c.x = 100f
c.y = 200f
send(2, c)

// Send only the message type or type id
send("hello")
send(3)
```

## Usage examples
- [agar-io](https://github.com/doorbash/agar-io) - Agar.io clone made with colyseus-kotlin and libgdx

## License

MIT
