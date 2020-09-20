package io.colyseus.serializer.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import io.colyseus.allFields

import io.colyseus.annotations.SchemaField
import io.colyseus.get
import io.colyseus.getType
import io.colyseus.isPrimary
import io.colyseus.serializer.schema.SPEC.SWITCH_TO_STRUCTURE
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema


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

public class Iterator(public var offset: Int = 0)

public enum class SPEC(val value: Int) {
    SWITCH_TO_STRUCTURE(255),
    TYPE_ID(213)
}

public enum class OPERATION(val value: Int) {
    ADD(128),
    REPLACE(0),
    DELETE(64),
    DELETE_AND_ADD(192),
    CLEAR(10),
}

class DataChange(
        var Op: Int = 0,
        var Field: String? = null,
        var DynamicIndex: Any? = null,
        var Value: Any? = null,
        var PreviousValue: Any? = null,
)

//    public delegate void OnChangeEventHandler(List<DataChange> changes)
//    public delegate void KeyValueEventHandler<T, K>(T value, K key)
//    public delegate void OnRemoveEventHandler()

public interface ISchemaCollection {
    fun moveEventHandlers(previousInstance: ISchemaCollection)
    fun invokeOnAdd(item: Any, index: Any)
    fun invokeOnChange(item: Any, index: Any)
    fun invokeOnRemove(item: Any, index: Any)

    fun triggerAll()
    fun _clear(refs: ReferenceTracker?)

    fun getChildType(): Class<*>?
    fun getTypeDefaultValue(): Any?

    fun _containsKey(key: Any): Boolean

    fun hasSchemaChild(): Boolean
    var childPrimitiveType: String?


    fun setIndex(index: Int, dynamicIndex: Any)
    fun getIndex(index: Int): Any?
    fun setByIndex(index: Int, dynamicIndex: Any, value: Any?)

    fun _clone(): ISchemaCollection

    fun _keys(): MutableSet<*>
    fun _get(index: Any?): Any?
    fun _set(index: Any?, value: Any?)
}


public interface IRef {
    public var __refId: Int
    var __parent: IRef?

    fun getByIndex(index: Int): Any?

    fun deleteByIndex(index: Int)
}


open class Schema : IRef {

    val fieldsByIndex = HashMap<Int, String?>()
    val fieldTypes = HashMap<String, Class<*>?>()
    val fieldTypeNames = HashMap<String, String?>()
    val fieldChildPrimitiveTypes = HashMap<String, String?>()
    val fieldChildTypes = HashMap<String, Class<*>?>()

    @JsonIgnore
    var onChange: ((changes: List<DataChange?>?) -> Unit)? = null

    @JsonIgnore
    var onRemove: (() -> Unit)? = null

    @JsonIgnore
    public override var __refId: Int = 0

    @JsonIgnore
    public override var __parent: IRef? = null

    @JsonIgnore
    private var refs: ReferenceTracker? = null

    init {
        for (field in javaClass.allFields) {
            if (!field.isAnnotationPresent(SchemaField::class.java)) continue
            field.isAccessible = true
            val fieldName = field.name
            val v1 = field.getAnnotation(SchemaField::class.java).v1
            val v2 = field.getAnnotation(SchemaField::class.java).v2

            val parts = v1.split("/").toTypedArray()
            val fieldIndex = parts[0].toInt()
            val schemaFieldTypeName = parts[1]

            fieldsByIndex[fieldIndex] = fieldName
            fieldTypeNames[fieldName] = schemaFieldTypeName

            if (isPrimary(schemaFieldTypeName)) {
                fieldTypes[fieldName] = v2.java
            } else if (schemaFieldTypeName == "ref") {
                fieldTypes[fieldName] = v2.java
                fieldChildTypes[fieldName] = v2.java
            } else {
                // array, map
                fieldTypes[fieldName] = getType(schemaFieldTypeName)
                fieldChildPrimitiveTypes[fieldName] = parts[2]
                fieldChildTypes[fieldName] = v2.java
                if (v2 == Any::class && parts[2] != "ref") {
                    fieldChildTypes[fieldName] = getType(parts[2])
                }
            }
        }
    }

    /* allow to retrieve property values by its string name */
    public operator fun get(propertyName: String): Any? {
        val field = this::class.java[propertyName]
        field?.isAccessible = true
        return field?.get(this)
    }

    public operator fun set(propertyName: String, value: Any?) {
        val field = this::class.java[propertyName]
        field?.isAccessible = true
        field?.set(this, value)
    }

    public fun decode(bytes: ByteArray, it: Iterator? = Iterator(0), refs: ReferenceTracker? = null) {
        var it = it
        var refs = refs

        if (it == null) it = Iterator()
        if (refs == null) refs = ReferenceTracker()
        this.refs = refs
        val totalBytes = bytes.size

        var refId = 0
        var _ref: IRef? = this
        var changes = arrayListOf<DataChange>()
        val allChanges = hashMapOf<Any, Any>()
        refs.add(refId, this)

        while (it.offset < totalBytes) {
            val _byte = bytes[it.offset++].toInt() and 0xFF

            if (_byte == SWITCH_TO_STRUCTURE.value) {
                refId = Decoder.decodeNumber(bytes, it).toInt()
                _ref = refs[refId]

                //
                // Trying to access a reference that haven't been decoded yet.
                //
                if (_ref == null) {
                    throw Exception("refId not found: $refId") }

                // create empty list of changes for this refId.
                changes = arrayListOf()
                allChanges[refId] = changes
                continue
            }


            val isSchema = _ref is Schema

            val operation = if (isSchema) _byte shr 6 shl 6 and 0xFF // "compressed" index + operation
            else _byte // "uncompressed" index + operation (array/map items)

            if (operation == OPERATION.CLEAR.value) {
                (_ref as ISchemaCollection)._clear(refs)
                continue
            }

            var fieldIndex: Int
            var fieldName: String? = null
            var fieldType: String? = null

            var childType: Class<*>? = null
            var childPrimitiveType: String? = null

            if (isSchema) {
                fieldIndex = _byte % (if (operation == 0) 255 else operation) // FIXME: JS allows (0 || 255)
                fieldName = (_ref as Schema).fieldsByIndex[fieldIndex]

                fieldType = _ref.fieldTypeNames[fieldName]
                childType = _ref.fieldChildTypes[fieldName]
            } else {
                fieldName = "" // FIXME

                fieldIndex = Decoder.decodeNumber(bytes, it).toInt()
                if ((_ref as ISchemaCollection).hasSchemaChild()) {
                    fieldType = "ref"
                    childType = (_ref as ISchemaCollection).getChildType()
                } else {
                    fieldType = (_ref as ISchemaCollection).childPrimitiveType
                }
            }


            var value: Any? = null
            var previousValue: Any? = null
            var dynamicIndex: Any? = null

            if (!isSchema) {
                previousValue = _ref.getByIndex(fieldIndex)
                if ((operation and OPERATION.ADD.value) == OPERATION.ADD.value) {
                    // MapSchema dynamic index.
//                    dynamicIndex = if ((_ref as ISchemaCollection).GetItems() is HashMap)
                    dynamicIndex = if (_ref is MapSchema<*>)
                        Decoder.decodeString(bytes, it)
                    else fieldIndex

                    (_ref as ISchemaCollection).setIndex(fieldIndex, dynamicIndex)
                } else {
                    dynamicIndex = (_ref as ISchemaCollection).getIndex(fieldIndex)
                }
            } else if (fieldName != null) { // FIXME: duplicate check
                previousValue = (_ref as Schema)[fieldName]
            }


            //
            // Delete operations
            //
            if ((operation and OPERATION.DELETE.value) == OPERATION.DELETE.value) {
                if (operation != OPERATION.DELETE_AND_ADD.value) {
                    _ref.deleteByIndex(fieldIndex)
                }

                // Flag `refId` for garbage collection.
                if (previousValue != null && previousValue is IRef) {
                    refs.remove((previousValue as IRef).__refId!!)
                }

                value = null
            }


            if (fieldName == null) {
                //
                // keep skipping next bytes until reaches a known structure
                // by local decoder.
                //
                val nextIterator = Iterator(offset = it.offset)

                while (it.offset < totalBytes) {
                    if (Decoder.switchStructureCheck(bytes, it)) {
                        nextIterator.offset = it.offset + 1
                        if (refs.has(Decoder.decodeNumber(bytes, nextIterator).toInt())) {
                            break
                        }
                    }

                    it.offset++
                }

                continue

            } else if (operation == OPERATION.DELETE.value) {
                //
                // FIXME: refactor me.
                // Don't do anything.
                //
            } else if (fieldType == "ref") {
                refId = Decoder.decodeNumber(bytes, it).toInt()
                value = refs[refId]

                if (operation != OPERATION.REPLACE.value) {
                    val concreteChildType = getSchemaType(bytes, it, childType)

                    if (value == null) {
                        value = createTypeInstance(concreteChildType)

                        if (previousValue != null) {
                            (value as Schema).onChange = (previousValue as Schema).onChange
                            value.onRemove = previousValue.onRemove

                            if ((previousValue as IRef).__refId > 0 && refId != previousValue.__refId) {
                                refs.remove((previousValue as IRef).__refId)
                            }
                        }
                    }

                    refs.add(refId, value as IRef, (value != previousValue))
                }
            } else if (childType == null) {
                // primitive values
                value = Decoder.decodePrimitiveType(fieldType, bytes, it)
            } else {
                refId = Decoder.decodeNumber(bytes, it).toInt()
                value = refs[refId]

                val valueRef: ISchemaCollection = if (refs.has(refId))
                    previousValue as ISchemaCollection
                else {
                    when (fieldType) {
                        "array" -> ArraySchema(childType)
                        "map" -> MapSchema(childType)
                        else -> throw Error("$fieldType is not supported")
                    }
                }

//                value = valueRef._clone()
                value = valueRef

                // keep reference to nested childPrimitiveType.
                childPrimitiveType = (_ref as Schema).fieldChildPrimitiveTypes[fieldName]
                value.childPrimitiveType = childPrimitiveType!!

                if (previousValue != null) {
                    value.moveEventHandlers(previousValue as ISchemaCollection)

                    if ((previousValue as IRef).__refId > 0 && refId != (previousValue as IRef).__refId) {
                        refs.remove((previousValue as IRef).__refId)

                        val deletes = arrayListOf<DataChange>()
                        val keys = (previousValue as ISchemaCollection)._keys()

                        for (key in keys) {
                            deletes.add(DataChange(
                                    DynamicIndex = key,
                                    Op = OPERATION.DELETE.value,
                                    Value = null,
                                    PreviousValue = previousValue._get(key)
                            ))
                        }

                        allChanges[(previousValue as IRef).__refId] = deletes
                    }
                }

                refs.add(refId, value as IRef, (valueRef !== previousValue))
            }


            val hasChange = previousValue !== value

            if (value != null) {
                if (value is IRef) {
                    value.__refId = refId
                    value.__parent = _ref
                }

                if (_ref is Schema) {
                    _ref[fieldName] = value
                } else if (_ref is ISchemaCollection) {
                    (_ref as ISchemaCollection).setByIndex(fieldIndex, dynamicIndex!!, value)
                }
            }

            if (hasChange) {
                changes.add(DataChange(
                        Op = operation,
                        Field = fieldName,
                        DynamicIndex = dynamicIndex,
                        Value = value,
                        PreviousValue = previousValue
                ))
            }
        }

        triggerChanges(allChanges)

        refs.garbageCollection()

    }

    public fun triggerAll() {
        var allChanges = HashMap<Any, Any>()
        triggerAllFillChanges(this, allChanges)
        triggerChanges(allChanges)
    }


    protected fun triggerAllFillChanges(currentRef: IRef, allChanges: HashMap<Any, Any>) {
        // skip recursive structures...
        if (allChanges.contains(currentRef.__refId)) {
            return }

        var changes = arrayListOf<DataChange>()
        allChanges[currentRef.__refId as Any] = changes

        if (currentRef is Schema) {
            for (fieldName in currentRef.fieldsByIndex.values) {
                val value = currentRef[fieldName!!]
                changes.add(DataChange(
                        Field = fieldName,
                        Op = OPERATION.ADD.value,
                        Value = value
                ))

                if (value is IRef) {
                    triggerAllFillChanges(value, allChanges)
                }
            }
        } else {
            if ((currentRef as ISchemaCollection).hasSchemaChild()) {
                val keys = (currentRef as ISchemaCollection)._keys()
                for (key in keys) {
                    val child = currentRef._get(key)

                    changes.add(DataChange
                    (
                            Field = null,
                            DynamicIndex = key,
                            Op = OPERATION.ADD.value,
                            Value = child
                    ))

                    triggerAllFillChanges(child as IRef, allChanges)
                }
            }
        }
    }

    fun triggerChanges(allChanges: HashMap<Any, Any>) {
        for (key in allChanges.keys) {
            val changes = allChanges[key] as List<DataChange>?

            val _ref = refs!![key as Int]
            val isSchema = _ref is Schema

            for (change in changes!!) {
                //const listener = ref['$listeners'] && ref['$listeners'][change.field]

                if (!isSchema) {
                    val container = _ref as ISchemaCollection

                    if (change.Op == OPERATION.ADD.value && change.PreviousValue == container.getTypeDefaultValue()) {
                        container.invokeOnAdd(change.Value!!, change.DynamicIndex!!)

                    } else if (change.Op == OPERATION.DELETE.value) {
                        //
                        // FIXME: `previousValue` should always be avaiiable.
                        // ADD + DELETE operations are still encoding DELETE operation.
                        //
                        if (change.PreviousValue != container.getTypeDefaultValue()) {
                            container.invokeOnRemove(change.PreviousValue!!, change.DynamicIndex ?: change.Field!!)
                        }
                    } else if (change.Op == OPERATION.DELETE_AND_ADD.value) {
                        if (change.PreviousValue != container.getTypeDefaultValue()) {
                            container.invokeOnRemove(change.PreviousValue!!, change.DynamicIndex!!)
                        }
                        container.invokeOnAdd(change.Value!!, change.DynamicIndex!!)

                    } else if (change.Op == OPERATION.REPLACE.value || change.Value != change.PreviousValue) {
                        container.invokeOnChange(change.Value!!, change.DynamicIndex!!)
                    }
                }

                //
                // trigger onRemove on child structure.
                //
                if ((change.Op and OPERATION.DELETE.value) == OPERATION.DELETE.value && change.PreviousValue is Schema) {
                    (change.PreviousValue as Schema).onRemove?.invoke()
                }
            }

            if (isSchema) {
                (_ref as Schema).onChange?.invoke(changes)
            }
        }
    }

    fun getSchemaType(bytes: ByteArray, it: Iterator, defaultType: Class<*>?): Class<*>? {
        var type: Class<*>? = defaultType
        if (bytes[it.offset].toInt() and 0xFF == SPEC.TYPE_ID.value) {
            it.offset++
            val typeId: Int = Decoder.decodeNumber(bytes, it).toInt()
            type = Context.instance[typeId]
        }
        return type
    }

    fun createTypeInstance(type: Class<*>?): Any {
        val constructor = type!!.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }


    public override fun getByIndex(index: Int): Any? {
        val fieldName: String = fieldsByIndex[index] ?: return null
        return this[fieldName]
    }

    public override fun deleteByIndex(index: Int) {
        val fieldName: String = fieldsByIndex[index] ?: return
        this[fieldName] = null
    }

}