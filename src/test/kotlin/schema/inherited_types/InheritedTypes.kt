package schema.inherited_types

import io.colyseus.annotations.SchemaField
import io.colyseus.serializer.schema.Schema

class InheritedTypes : Schema() {
    @SchemaField("0/ref", Entity::class)
    var entity = Entity()

    @SchemaField("1/ref", Player::class)
    var player = Player()

    @SchemaField("2/ref", Bot::class)
    var bot = Bot()

    @SchemaField("3/ref", Entity::class)
    var any = Entity()
}