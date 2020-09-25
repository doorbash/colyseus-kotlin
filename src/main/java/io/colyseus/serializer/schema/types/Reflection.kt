package io.colyseus.serializer.schema.types

import io.colyseus.serializer.schema.Schema

class Reflection {
    companion object {

        public fun findRt(r: Short, fieldName: String): Short {
            var rt = (-1).toShort()
            val types = reflectionObject["types"] as ArraySchema<Schema>
            for (type in types) {
                if ((type?.get("id") as Short) == r) {
                    val fields = type["fields"] as ArraySchema<Schema>
                    for (field in fields) {
                        if ((field?.get("name") as String) == fieldName) {
                            rt = field["referencedType"] as Short
                            break
                        }
                    }
                    break
                }
            }
            return rt
        }

        var schemaReflectionField = Schema().apply {
            this.fieldsByIndex[0] = "name"
            this.fieldsByIndex[1] = "type"
            this.fieldsByIndex[2] = "referencedType"

            this.fieldTypes["name"] = String::class.java
            this.fieldTypes["type"] = String::class.java
            this.fieldTypes["referencedType"] = Short::class.java

            this.fieldTypeNames["name"] = "string"
            this.fieldTypeNames["type"] = "string"
            this.fieldTypeNames["referencedType"] = "uint8"
        }

        var schemaReflectionType = Schema().apply {
            this.fieldsByIndex[0] = "id"
            this.fieldsByIndex[1] = "fields"

            this.fieldTypes["id"] = Short::class.java
            this.fieldTypes["fields"] = ArraySchema::class.java

            this.fieldTypeNames["id"] = "uint8"
            this.fieldTypeNames["fields"] = "array"

            this.fieldChildPrimitiveTypes["fields"] = "ref"

            this.fieldChildTypes["fields"] = Schema::class.java
        }

        var schemaReflection = Schema().apply {
            this.fieldsByIndex[0] = "types"
            this.fieldsByIndex[1] = "rootType"

            this.fieldTypes["types"] = ArraySchema::class.java
            this.fieldTypes["rootType"] = Short::class.java

            this.fieldTypeNames["types"] = "array"
            this.fieldTypeNames["rootType"] = "uint8"

            this.fieldChildPrimitiveTypes["types"] = "ref"

            this.fieldChildTypes["types"] = Schema::class.java
        }

        var reflectionObject = schemaReflection.clone().apply {
            this["rootType"] = 0.toShort()
            this["types"] = ArraySchema(Schema::class.java)

            val types = this["types"] as ArraySchema<Schema>

            // SchemaReflection
            types.add(schemaReflectionType.clone().apply {
                this["id"] = 0.toShort()
                this["fields"] = ArraySchema(Schema::class.java)

                val fields = this["fields"] as ArraySchema<Schema>

                // types
                fields.add(schemaReflectionField.clone().apply {
                    this["name"] = "types"
                    this["type"] = "array"
                    this["referencedType"] = 1.toShort()
                })

                // rootType
                fields.add(schemaReflectionField.clone().apply {
                    this["name"] = "rootType"
                    this["type"] = "uint8"
                })
            })

            // SchemaReflectionType
            types.add(schemaReflectionType.clone().apply {
                this["id"] = 1.toShort()
                this["fields"] = ArraySchema(Schema::class.java)

                val fields = this["fields"] as ArraySchema<Schema>

                // id
                fields.add(schemaReflectionField.clone().apply {
                    this["name"] = "id"
                    this["type"] = "uint8"
                })

                // fields
                fields.add(schemaReflectionField.clone().apply {
                    this["name"] = "fields"
                    this["type"] = "array"
                    this["referencedType"] = 2.toShort()
                })
            })

            // SchemaReflectionField
            types.add(schemaReflectionType.clone().apply {
                this["id"] = 2.toShort()
                this["fields"] = ArraySchema(Schema::class.java)

                val fields = this["fields"] as ArraySchema<Schema>

                // name
                fields.add(schemaReflectionField.clone().apply {
                    this["name"] = "name"
                    this["type"] = "string"
//                    this["referencedType"] = 2
                })

                // type
                fields.add(schemaReflectionField.clone().apply {
                    this["name"] = "type"
                    this["type"] = "string"
                })

                // referencedType
                fields.add(schemaReflectionField.clone().apply {
                    this["name"] = "referencedType"
                    this["type"] = "uint8"
                })
            })
        }
    }
}
