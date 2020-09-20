package io.colyseus.example.kotlin

import io.colyseus.serializer.schema.Schema
import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema

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