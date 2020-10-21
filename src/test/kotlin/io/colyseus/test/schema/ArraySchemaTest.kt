package io.colyseus.test.schema

import io.colyseus.serializer.schema.types.ArraySchema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ArraySchemaTest {
    @Test
    fun `ArraySchema test`() {
        fun doSomethingWithArray(array: ArrayList<Int>): ArrayList<Int> {
            return array.apply {
                add(3)
                add(4000)
                add(-100)
                removeAt(1)
                add(0)
                set(0, 200)
                remove(200)
                set(1, 300)
                removeAt(0)
                add(-100)
                add(400)
            }
        }

        val arrayList = doSomethingWithArray(ArrayList())
        val arraySchema = doSomethingWithArray(ArraySchema<Int>() as ArrayList<Int>)

        println(arrayList)

        assertEquals(arrayList.size, arraySchema.size)

        // iterator test
        arraySchema.forEachIndexed { index, element ->
            assertEquals(element, arrayList[index])
        }

        // get method test
        repeat(arrayList.size) {
            assertEquals(arraySchema[it], arrayList[it])
        }
    }
}