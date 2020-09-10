package io.colyseus.example

import io.colyseus.serializer.schema.Schema
import io.colyseus.annotations.SchemaClass
import io.colyseus.annotations.SchemaField

@SchemaClass
class MyState : Schema() {
    @SchemaField("0/ref")
    var primitives = PrimitivesTest()

    @SchemaField("1/array/ref")
    var players = ArraySchema(Player::class.java)

    @SchemaField("2/map/ref")
    var cells = MapSchema(Cell::class.java)
}

@SchemaClass
class Cell : Schema() {
    @SchemaField("0/float32")
    var x = 0f

    @SchemaField("1/float32")
    var y = 0f
}

@SchemaClass
class PrimitivesTest : Schema() {
    @SchemaField("0/uint8")
    var _uint8: Short = 0

    @SchemaField("1/uint16")
    var _uint16 = 0

    @SchemaField("2/uint32")
    var _uint32: Long = 0

    @SchemaField("3/uint64")
    var _uint64: Long = 0

    @SchemaField("4/int8")
    var _int8: Byte = 0

    @SchemaField("5/int16")
    var _int16: Short = 0

    @SchemaField("6/int32")
    var _int32 = 0

    @SchemaField("7/int64")
    var _int64: Long = 0

    @SchemaField("8/float32")
    var _float32_n = 0f

    @SchemaField("9/float32")
    var _float32_p = 0f

    @SchemaField("10/float64")
    var _float64_n = 0.0

    @SchemaField("11/float64")
    var _float64_p = 0.0

    @SchemaField("12/boolean")
    var _boolean = false

    @SchemaField("13/string")
    var _string: String? = null
}

@SchemaClass
class Player : Schema() {
    @SchemaField("0/int32")
    var x = 0
}