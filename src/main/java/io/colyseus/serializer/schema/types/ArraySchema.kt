package io.colyseus.serializer.schema.types

import com.fasterxml.jackson.annotation.JsonIgnore
import io.colyseus.default
import io.colyseus.serializer.schema.IRef
import io.colyseus.serializer.schema.ISchemaCollection
import io.colyseus.serializer.schema.ReferenceTracker
import io.colyseus.serializer.schema.Schema
import java.util.*
import kotlin.collections.ArrayList

class ArraySchema<T : Any?>(
        @JsonIgnore public var ct: Class<T>?,
) : ArrayList<T?>(), ISchemaCollection, IRef {

    constructor() : this(null)

    @JsonIgnore
    var onAdd: ((value: T?, key: Int?) -> Unit)? = null

    @JsonIgnore
    var onChange: ((value: T?, key: Int?) -> Unit)? = null

    @JsonIgnore
    var onRemove: ((value: T?, key: Int?) -> Unit)? = null

    public override fun hasSchemaChild(): Boolean = (Schema::class.java).isAssignableFrom(ct)

    override var childPrimitiveType: String? = null

    public override var __refId: Int = 0
    public override var __parent: IRef? = null

    public override fun setIndex(index: Int, dynamicIndex: Any) {
//        println("setIndex index=" + index + " dynamicIndex=" + dynamicIndex)
    }

    public override fun setByIndex(index: Int, dynamicIndex: Any, value: Any?) {
        var ind = dynamicIndex as Int

        if (ind < 0) return

        if (ind < size) {
            this[ind] = value as T?
            return
        }

        val s = size
        for (i in s until ind) add(null)
        add(value as T?)
    }

    public override fun getIndex(index: Int): Int? {
        return index
    }

    public override fun getByIndex(index: Int): Any? {
        return if (index < 0 || index >= size) null else this[index]
    }

    public override fun deleteByIndex(index: Int) {
        if (index < 0 || index >= size) return
        this.removeAt(index)
    }

    public override fun _clear(refs: ReferenceTracker?) {
        if (refs != null && hasSchemaChild()) {
            for (item in this) {
                refs.remove((item as Schema).__refId)
            }
        }

        super.clear()
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
            onAdd?.invoke(this[i], i)
        }
    }

    public override fun moveEventHandlers(previousInstance: ISchemaCollection) {
        onAdd = (previousInstance as ArraySchema<T>).onAdd
        onChange = previousInstance.onChange
        onRemove = previousInstance.onRemove
    }

    override fun invokeOnAdd(item: Any, index: Any) {
        onAdd?.invoke(item as T?, index as Int)
    }

    override fun invokeOnChange(item: Any, index: Any) {
        onChange?.invoke(item as T?, index as Int)
    }

    override fun invokeOnRemove(item: Any, index: Any) {
        onRemove?.invoke(item as T?, index as Int)
    }
}