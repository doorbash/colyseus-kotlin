package io.colyseus.serializer.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import io.colyseus.annotations.SchemaClass
import io.colyseus.annotations.SchemaField
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Type
import java.util.*

/*
   Allowed primitive types:
       "string"
       "number"
       "boolean"
       "int8"
       "uint8"
       "int16"
       "uint16"
       "int32"
       "uint32"
       "int64"
       "uint64"
       "float32"
       "float64"
       Allowed reference types:
       "ref"
       "array"
       "map"
*/
open class Schema {
    val fieldsByIndex = HashMap<Int, String>()
    val fieldTypeNames = HashMap<String?, String>()
    val fieldTypes = HashMap<String?, Class<*>>()
    val fieldChildTypeNames = HashMap<String?, String>()

    @JsonIgnore
    var onChange: ((changes: List<Change?>?) -> Unit)? = null

    @JsonIgnore
    var onRemove: (() -> Unit)? = null

    class ArraySchema<T> : ArrayList<T>, ISchemaCollection<Int, T> {

        @JsonIgnore
        private var ct: Class<T>? = null

        @JsonIgnore
        var onAdd: ((value: T?, key: Int?) -> Unit)? = null

        @JsonIgnore
        var onChange: ((value: T, key: Int?) -> Unit)? = null

        @JsonIgnore
        var onRemove: ((value: T, key: Int?) -> Unit)? = null

        constructor(childType: Class<T>?) {
            this.ct = childType
        }

        override fun _clone(): ArraySchema<T> {
            val clone: ArraySchema<T> = ArraySchema(ct)
            clone.onAdd = onAdd
            clone.onChange = onChange
            clone.onRemove = onRemove
            clone.addAll(this);
            return clone
        }

        override fun _set(key: Int, item: T) {
            if (key < size) {
                set(key, item)
            } else if (key == size) {
                add(item)
            }
        }

        fun containsKeys(index: Int): Boolean {
            return size > index
        }

        fun getChildType(): Class<*>? {
            return ct
        }

        override fun hasSchemaChild(): Boolean {
            return if (ct == null) false else Schema::class.java.isAssignableFrom(ct)
        }

        override fun count(): Int {
            return size
        }

        fun invokeOnAdd(item: T, key: Int) {
            onAdd?.invoke(item, key)
        }

        fun invokeOnChange(item: T, key: Int) {
            onChange?.invoke(item, key)
        }

        fun invokeOnRemove(item: T, key: Int) {
            onRemove?.invoke(item, key)
        }

        override fun triggerAll() {
            if (onAdd == null) return
            for (i in 0 until size) {
                onAdd?.invoke(get(i), i)
            }
        }
    }

    class MapSchema<T> : LinkedHashMap<String?, T>, ISchemaCollection<String?, T> {

        @JsonIgnore
        private var ct: Class<T>? = null

        @JsonIgnore
        var onAdd: ((value: T, key: String) -> Unit)? = null

        @JsonIgnore
        var onChange: ((value: T, key: String) -> Unit)? = null

        @JsonIgnore
        var onRemove: ((value: T, key: String) -> Unit)? = null

        constructor(childType: Class<T>?) {
            this.ct = childType
        }

        override fun _clone(): MapSchema<T> {
            val clone = MapSchema(ct)
            clone.onAdd = onAdd
            clone.onChange = onChange
            clone.onRemove = onRemove
            for (key in keys) {
                clone[key] = this[key]!!
            }
            return clone
        }

        fun getChildType(): Class<*>? {
            return ct
        }

        override fun hasSchemaChild(): Boolean {
            return if (ct == null) false else Schema::class.java.isAssignableFrom(ct)
        }

        override fun count(): Int {
            return size
        }

        fun contains(key: String?, value: T): Boolean {
            val `val` = get(key)
            return `val` != null && `val` == value
        }

        fun invokeOnAdd(item: T, key: String) {
            onAdd?.invoke(item, key)
        }

        fun invokeOnChange(item: T, key: String) {
            onChange?.invoke(item, key)
        }

        fun invokeOnRemove(item: T, key: String) {
            onRemove?.invoke(item, key)
        }

        override fun triggerAll() {
            if (onAdd == null) return
            for (key in keys) {
                onAdd?.invoke(get(key)!!, key!!)
            }
        }

        override fun _set(key: String?, item: T) {
            put(key, item)
        }
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun decode(bytes: ByteArray, it: Iterator? = Iterator(0)) {
        var it = it
        if (it == null) it = Iterator()
        val changes: MutableList<Change> = ArrayList()
        val totalBytes = bytes.size
        while (it.offset < totalBytes) {
            if (bytes[it.offset] == SPEC.TYPE_ID) {
                it.offset += 2
            }
            val isNil = Decoder.nilCheck(bytes, it)
            if (isNil) it.offset++
            val index = bytes[it.offset++].toInt()
            if (index == SPEC.END_OF_STRUCTURE.toInt()) {
                break
            }
            // Schema version mismatch (backwards compatibility)
            if (!fieldsByIndex.containsKey(index)) continue
            val field = fieldsByIndex[index]
            val fieldType = fieldTypes[field]!!
            val fieldTypeName = fieldTypeNames[field]
            //            Class<?> childType = fieldChildTypes.get(field);
            val childPrimitiveType = fieldChildTypeNames[field]
            //            Object change = null;
            var value: Any?
            var hasChange: Boolean
            if (isNil) {
                value = null
                hasChange = true
            } else {
                when (fieldTypeName) {
                    "ref" -> {
                        // child schema type
//                        if (Decoder.nilCheck(bytes, it)) {
//                            it.offset++;
//                            value = null;
//                        } else {
                        value = thiz(field)
                        if (value == null) {
                            value = createTypeInstance(bytes, it, fieldType)
                        }
                        (value as Schema?)!!.decode(bytes, it)
                        //                        }
                        hasChange = true
                    }
                    "array" -> {

//                        change = new ArrayList<>();
                        val valueRef = thiz(field) as ArraySchema<Any>
                        val currentValue = valueRef._clone()
                        val newLength = Decoder.decodeNumber(bytes, it).toInt()
                        val numChanges = Math.min(Decoder.decodeNumber(bytes, it).toInt(), newLength)
                        val hasRemoval = currentValue.count() > newLength
                        hasChange = numChanges > 0 || hasRemoval
                        var hasIndexChange = false

                        // ensure current array has the same length as encoded one
                        if (hasRemoval) {
                            val removeList: MutableList<Any?> = ArrayList<Any?>()
                            val items: ArraySchema<Any> = currentValue
                            var i = newLength
                            val l = currentValue.count()
                            while (i < l) {
                                val item: Any = items[i]!!
                                if (item is Schema) {
                                    item.onRemove?.invoke()
                                }
                                removeList.add(item)
                                currentValue.invokeOnRemove(item, i)
                                i++
                            }
                            for (item in removeList) {
                                items.remove(item)
                            }
                            // reduce items length
//                            ArrayList newItems = new ArrayList();
//                            for (int i = 0; i < newLength; i++) {
//                                newItems.add(currentValue.get(i));
//                            }
//                            currentValue.items = newItems;
                        }
                        var i = 0
                        while (i < numChanges) {
                            val newIndex = Decoder.decodeNumber(bytes, it).toInt()
                            var indexChangedFrom = -1
                            if (Decoder.indexChangeCheck(bytes, it)) {
                                Decoder.decodeUint8(bytes, it)
                                indexChangedFrom = Decoder.decodeNumber(bytes, it).toInt()
                                hasIndexChange = true
                            }
                            var isNew = !hasIndexChange && !currentValue.containsKeys(newIndex) || hasIndexChange && indexChangedFrom != -1
                            if (currentValue.hasSchemaChild()) {
                                var item: Schema?
                                item = if (isNew) {
                                    createTypeInstance(bytes, it, currentValue.getChildType()) as Schema
                                } else if (indexChangedFrom != -1) {
                                    valueRef[indexChangedFrom] as Schema?
                                } else {
                                    valueRef[newIndex] as Schema?
                                }
                                if (item == null) {
                                    item = createTypeInstance(bytes, it, currentValue.getChildType()) as Schema
                                    isNew = true
                                }

//                                if (Decoder.nilCheck(bytes, it)) {
//                                    it.offset++;
//                                    if (item.onRemove != null) item.onRemove.onRemove();
//                                    valueRef.invokeOnRemove(item, newIndex);
//                                    continue;
//                                }
                                item!!.decode(bytes, it)
                                currentValue._set(newIndex, item)
                            } else {
                                currentValue._set(newIndex, Decoder.decodePrimitiveType(childPrimitiveType, bytes, it))
                            }
                            if (isNew) {
                                currentValue.invokeOnAdd(currentValue[newIndex]!!, newIndex)
                            } else {
                                currentValue.invokeOnChange(currentValue[newIndex]!!, newIndex)
                            }
                            i++
                        }
                        value = currentValue
                    }
                    "map" -> {
                        val valueRef: MapSchema<Any> = thiz(field) as MapSchema<Any>
                        val currentValue = valueRef._clone()
                        val length = Decoder.decodeNumber(bytes, it).toInt()
                        hasChange = length > 0
                        var hasIndexChange = false
                        val items: MapSchema<Any> = currentValue
                        val keys: Array<Any> = items.keys.toTypedArray() as Array<Any>
                        val mapKeys = arrayOfNulls<String>(items.size)
                        run {
                            var i = 0
                            while (i < keys.size) {
                                mapKeys[i] = keys[i] as String
                                i++
                            }
                        }
                        var i = 0
                        while (i < length) {

                            // `encodeAll` may indicate a higher number of indexes it actually encodes
                            // TODO: do not encode a higher number than actual encoded entries
                            if (it.offset > bytes.size || bytes[it.offset] == SPEC.END_OF_STRUCTURE) {
                                break
                            }
                            val isNilItem = Decoder.nilCheck(bytes, it)
                            if (isNilItem) it.offset++
                            var previousKey: String? = null
                            if (Decoder.indexChangeCheck(bytes, it)) {
                                it.offset++
                                previousKey = mapKeys[Decoder.decodeNumber(bytes, it).toInt()]
                                hasIndexChange = true
                            }
                            val hasMapIndex = Decoder.numberCheck(bytes, it)
                            val isSchemaType = currentValue.hasSchemaChild()
                            val newKey: String = if (hasMapIndex) mapKeys[Decoder.decodeNumber(bytes, it).toInt()]!! else Decoder.decodeString(bytes, it)
                            var item: Any?
                            val isNew = !hasIndexChange && !valueRef.containsKey(newKey) || hasIndexChange && previousKey == null && hasMapIndex
                            item = if (isNew && isSchemaType) {
                                createTypeInstance(bytes, it, currentValue.getChildType())
                            } else if (previousKey != null) {
                                valueRef[previousKey]
                            } else {
                                valueRef[newKey]
                            }
                            if (isNilItem) {
                                if (item is Schema && item.onRemove != null) {
                                    item.onRemove?.invoke()
                                }
                                valueRef.invokeOnRemove(item!!, newKey)
                                items.remove(newKey)
                                i++
                                continue
                            } else if (!isSchemaType) {
                                currentValue[newKey] = Decoder.decodePrimitiveType(childPrimitiveType, bytes, it)
                            } else {
                                (item as Schema?)!!.decode(bytes, it)
                                currentValue[newKey] = item as Any
                            }
                            if (isNew) {
                                currentValue.invokeOnAdd(currentValue[newKey]!!, newKey)
                            } else {
                                currentValue.invokeOnChange(currentValue[newKey]!!, newKey)
                            }
                            i++
                        }
                        value = currentValue
                    }
                    else -> {
                        // Primitive type
                        value = Decoder.decodePrimitiveType(fieldTypeName, bytes, it)
                        hasChange = true
                    }
                }
            }
            if (hasChange) {
                val dataChange = Change()
                dataChange.field = field
                dataChange.value = value
                dataChange.previousValue = thiz(field)
                changes.add(dataChange)
            }
            val f = javaClass.getDeclaredField(field)
            f.isAccessible = true
            f[this] = value
        }
        if (!changes.isEmpty()) {
            onChange?.invoke(changes)
        }
    }

    fun triggerAll() {
        if (onChange == null) return
        try {
            val changes: MutableList<Change> = ArrayList()
            for (field in fieldsByIndex.values) {
                val value = thiz(field)
                val change = Change()
                change.field = field
                change.value = value
                change.previousValue = null
                changes.add(change)
            }
            onChange?.invoke(changes)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    protected fun thiz(fieldName: String?): Any? {
        val field = javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field[this]
    }

    @Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    protected fun createTypeInstance(bytes: ByteArray, it: Iterator, type: Class<*>?): Any {
        return if (bytes[it.offset] == SPEC.TYPE_ID) {
            it.offset++
            val typeId = Decoder.decodeUint8(bytes, it).toInt()
            val anotherType = Context.instance[typeId]
            anotherType!!.javaClass.getConstructor().newInstance()
        } else {
            val constructor = type!!.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
        }
    }

    @SchemaClass
    class SchemaReflectionField : Schema() {
        @SchemaField("0/string")
        var name: String? = null

        @SchemaField("1/string")
        var type: String? = null

        @SchemaField("2/uint8")
        var referencedType = 0
    }

    @SchemaClass
    class SchemaReflectionType : Schema() {
        @SchemaField("0/uint8")
        var id = 0

        @SchemaField("1/array/SchemaReflectionField")
        var fields = ArraySchema(SchemaReflectionField::class.java)
        var type: Type? = null
    }

    @SchemaClass
    class SchemaReflection : Schema() {
        @SchemaField("0/array/SchemaReflectionType")
        var types = ArraySchema(SchemaReflectionType::class.java)

        @SchemaField("1/uint8")
        var rootType = 0
    }

    class Context {
        protected var typeIds = LinkedHashMap<Int, Type>()
        operator fun get(typeid: Int): Type? {
            return typeIds[typeid]
        }

        fun setTypeId(type: Type, typeid: Int) {
            typeIds[typeid] = type
        }

        companion object {
            var instance = Context()
                protected set
        }
    }

    object SPEC {
        const val END_OF_STRUCTURE = 193.toByte()
        const val NIL = 192.toByte()
        const val INDEX_CHANGE = 212.toByte()
        const val TYPE_ID = 213.toByte()
    }

    fun _clone(): Schema {
        return this
    }

    init {
        if (javaClass.isAnnotationPresent(SchemaClass::class.java)) {
            for (field in javaClass.declaredFields) {
                if (!field.isAnnotationPresent(SchemaField::class.java)) continue
                val fieldName = field.name
                val fieldType = field.type
                val annotation = field.getAnnotation(SchemaField::class.java).value
                val parts = annotation.split("/").toTypedArray()
                val fieldIndex = parts[0].toInt()
                val schemaFieldTypeName = parts[1]
                //                String javaFieldTypeName = fieldType.getCanonicalName();

//                System.out.println(fieldIndex + " " + fieldName + " " + schemaFieldTypeName + " " + javaFieldTypeName);
                fieldsByIndex[fieldIndex] = fieldName
                fieldTypeNames[fieldName] = schemaFieldTypeName
                fieldTypes[fieldName] = fieldType
                if (schemaFieldTypeName == "array" || schemaFieldTypeName == "map") {
                    fieldChildTypeNames[fieldName] = parts[2]
                }
            }
        } else if (javaClass != Schema::class.java) {
            throw Error("$javaClass does not have @SchemaClass annotation")
        }
    }
}