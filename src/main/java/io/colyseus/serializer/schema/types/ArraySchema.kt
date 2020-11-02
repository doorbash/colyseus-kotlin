package io.colyseus.serializer.schema.types

import com.fasterxml.jackson.annotation.JsonIgnore
import io.colyseus.serializer.schema.IRef
import io.colyseus.serializer.schema.ISchemaCollection
import io.colyseus.serializer.schema.ReferenceTracker
import io.colyseus.serializer.schema.Schema
import io.colyseus.util.callbacks.Function2Void
import io.colyseus.util.default
import java.util.*

class ArraySchema<T : Any?>(
        @JsonIgnore public var ct: Class<T>?,
) : ArrayList<T?>(), ISchemaCollection {

    constructor() : this(null)

    @JsonIgnore
    private val items: TreeMap<Int, T?> = TreeMap()

    @JsonIgnore
    var onAdd: ((value: T, key: Int) -> Unit)? = null

    public fun setOnAdd(f: Function2Void<T, Int>) {
        onAdd = f::invoke
    }

    @JsonIgnore
    var onChange: ((value: T, key: Int) -> Unit)? = null

    public fun setOnChange(f: Function2Void<T, Int>) {
        onChange = f::invoke
    }

    @JsonIgnore
    var onRemove: ((value: T, key: Int) -> Unit)? = null

    public fun setOnRemove(f: Function2Void<T, Int>) {
        onRemove = f::invoke
    }

    public override fun hasSchemaChild(): Boolean = (Schema::class.java).isAssignableFrom(ct)

    override var childPrimitiveType: String? = null

    public override var __refId: Int = 0
    public override var __parent: IRef? = null

    public override fun setIndex(index: Int, dynamicIndex: Any) {
//        println("setIndex index=$index dynamicIndex=$dynamicIndex")
    }

    public override fun setByIndex(index: Int, dynamicIndex: Any, value: Any?) {
//        val ind = dynamicIndex as Int

//        if (ind < 0) return
//
//        if (ind < size) {
//            this[ind] = value as T?
//            return
//        }

//        val s = size
//        for (i in s until ind) add(null)
//        add(value as T?)

        items[dynamicIndex as Int] = value as T?
    }

//    override fun put(key: Int, value: T?): T? {
//        val value = super.put(key, value)
//        valuesList = values.toList()
//        return value
//    }

    public override operator fun get(key: Int): T? {
        // FIXME: should be O(1)
        if (key >= size) return null
        return items.values.toList().get(key)
    }

    public override fun getIndex(index: Int): Int? {
        return index
    }

    public override fun getByIndex(index: Int): Any? {
        return this[index]
    }

    public override fun deleteByIndex(index: Int) {
//        if (index < 0 || index >= size) return
//        this.removeAt(index)
        // TODO
        items.remove(index)
    }

    override fun remove(element: T?): Boolean {
        return items.values.remove(element)
    }

    override fun removeAt(index: Int): T? {
        // FIXME: should be O(1)
        if (index >= size) return null
        val key = items.keys.toList().get(index)
        return items.remove(key)
    }

    public override fun _clear(refs: ReferenceTracker?) {
        if (refs != null && hasSchemaChild()) {
            for (item in this.items.values) {
                if (item == null) continue
                refs.remove((item as Schema).__refId)
            }
        }
        items.clear()
    }

    public override fun _clone(): ISchemaCollection {
        val clone = ArraySchema(ct)
        clone.onAdd = onAdd
        clone.onChange = onChange
        clone.onRemove = onRemove
        return clone
    }

    override fun _keys(): MutableSet<*> {
        val set = mutableSetOf<Int>()
        set.addAll(0 until size)
        return set
    }

    override fun _get(index: Any?): Any? {
        return getByIndex(index as Int)
    }

    override fun _set(index: Any?, value: Any?) {
        setByIndex(index as Int, index, value)
    }

    public override fun getChildType(): Class<*> {
        return ct!!
    }

    public override fun getTypeDefaultValue(): Any? {
        return default(ct!!)
    }

    public override fun _containsKey(key: Any): Boolean {
        key as Int
        return key >= 0 && key < size
    }

    public override fun triggerAll() {
        if (onAdd == null) {
            return
        }
        for (i in 0 until size) {
            if (this[i] == null) continue
            onAdd?.invoke(this[i]!!, i)
        }
    }

    public override fun moveEventHandlers(previousInstance: IRef) {
        onAdd = (previousInstance as ArraySchema<T?>).onAdd
        onChange = previousInstance.onChange
        onRemove = previousInstance.onRemove
    }

    override fun invokeOnAdd(item: Any, index: Any) {
        onAdd?.invoke(item as T, index as Int)
    }

    override fun invokeOnChange(item: Any, index: Any) {
        onChange?.invoke(item as T, index as Int)
    }

    override fun invokeOnRemove(item: Any, index: Any) {
        onRemove?.invoke(item as T, index as Int)
    }

    override fun toString(): String {
        return items.values.toString()
    }

    override fun iterator(): MutableIterator<T?> {
        return items.values.iterator()
    }

    override fun add(element: T?): Boolean {
        items[try {
            items.keys.last()
        } catch (e: NoSuchElementException) {
            -1
        } + 1] = element
        return true
    }

    override fun set(index: Int, element: T?): T? {
        if (index > items.size) return null
        if (index == items.size) {
            add(element)
            return element
        }
        val key = items.keys.toList()[index]
        items.put(key, element)
        return element
    }

    override val size: Int
        get() = items.size
}