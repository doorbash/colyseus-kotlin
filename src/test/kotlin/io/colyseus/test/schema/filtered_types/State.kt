package io.colyseus.test.schema.filtered_types

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema

class State : Schema() {
    @SchemaField("0/ref", Player::class)
    var playerOne = Player()

    @SchemaField("1/ref", Player::class)
    var playerTwo = Player()

    @SchemaField("2/array/ref", Player::class)
    var players = ArraySchema(Player::class.java)
}