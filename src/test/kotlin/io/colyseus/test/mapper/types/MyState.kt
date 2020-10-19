package io.colyseus.test.mapper.types;

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema
import io.colyseus.util.default

public class MyState : Schema() {
    @SchemaField("0/ref", Player::class)
    var player = Player()

    @SchemaField("1/array/ref", Player::class)
    var arrayOfPlayers = ArraySchema(Player::class.java)

    @SchemaField("2/map/ref", Player::class)
    var mapOfPlayers = MapSchema(Player::class.java)

    @SchemaField("3/array/number")
    var arrayOfPrimitives = ArraySchema(Float::class.java)

    @SchemaField("4/map/number")
    var mapOfPrimitives = MapSchema(Float::class.java)
}

class Player : Schema() {
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
    var _float32_1 = Float.default

    @SchemaField("9/float32")
    var _float32_2 = Float.default

    @SchemaField("10/float64")
    var _float64_1 = Double.default

    @SchemaField("11/number")
    var _number = Float.default

    @SchemaField("12/float64")
    var _float64_2 = Double.default

    @SchemaField("13/boolean")
    var _boolean = Boolean.default

    @SchemaField("14/string")
    var _string1 = String.default

    @SchemaField("15/string")
    var _string2 = String.default

    @SchemaField("16/string")
    var _string3 = String.default

    @SchemaField("17/array/ref", Cell::class)
    var arrayOfCells = ArraySchema(Cell::class.java)

    @SchemaField("18/map/ref", Cell::class)
    var mapOfCells = MapSchema(Cell::class.java)

    @SchemaField("19/array/number")
    var arrayOfPrimitives = ArraySchema(Float::class.java)

    @SchemaField("20/map/number")
    var mapOfPrimitives = MapSchema(Float::class.java)
}

class Cell : Schema() {
    @SchemaField("0/number")
    var x = Float.default

    @SchemaField("1/number")
    var y = Float.default
}