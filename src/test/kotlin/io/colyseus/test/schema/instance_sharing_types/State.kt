package io.colyseus.test.schema.instance_sharing_types

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema

class State : Schema() {
    @SchemaField("0/ref", Player::class)
    var player1 = Player()

    @SchemaField("1/ref", Player::class)
    var player2 = Player()

    @SchemaField("2/array/ref", Player::class)
    var arrayOfPlayers = ArraySchema(Player::class.java)

    @SchemaField("3/map/ref", Player::class)
    var mapOfPlayers = MapSchema(Player::class.java)
}