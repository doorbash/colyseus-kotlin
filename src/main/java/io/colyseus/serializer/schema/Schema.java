package io.colyseus.serializer.schema;

import io.colyseus.serializer.schema.annotations.SchemaClass;
import io.colyseus.serializer.schema.annotations.SchemaField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

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

public class Schema {

    private HashMap<Integer, String> fieldsByIndex = new HashMap<>();
    private HashMap<String, String> fieldTypeNames = new HashMap<>();
    private HashMap<String, Class<?>> fieldTypes = new HashMap<>();
    private HashMap<String, String> fieldChildTypeNames = new HashMap<>();

    public onChange onChange;
    public onRemove onRemove;

    public Schema() {
        if (getClass().isAnnotationPresent(SchemaClass.class)) {

            for (Field field : getClass().getDeclaredFields()) {

                if (!field.isAnnotationPresent(SchemaField.class)) continue;

                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                String annotation = field.getAnnotation(SchemaField.class).value();
                String[] parts = annotation.split("/");
                int fieldIndex = Integer.parseInt(parts[0]);
                String schemaFieldTypeName = parts[1];
//                String javaFieldTypeName = fieldType.getCanonicalName();

//                System.out.println(fieldIndex + " " + fieldName + " " + schemaFieldTypeName + " " + javaFieldTypeName);

                fieldsByIndex.put(fieldIndex, fieldName);
                fieldTypeNames.put(fieldName, schemaFieldTypeName);
                fieldTypes.put(fieldName, fieldType);

                if (schemaFieldTypeName.equals("array") || schemaFieldTypeName.equals("map")) {
                    fieldChildTypeNames.put(fieldName, parts[2]);
                }
            }
        } else throw new Error(getClass() + " does not have @SchemaClass annotation");
    }

    public interface onChange {
        public void onChange(List<Change> changes);
    }

    public interface onRemove {
        public void onRemove();
    }

    public static class ArraySchema<T> implements ISchemaCollection<Integer, T> {

        public interface onAddListener<T> {
            public void onAdd(T value, int key);
        }

        public interface onChangeListener<T> {
            public void onChange(T value, int key);
        }

        public interface onRemoveListener<T> {
            public void onRemove(T value, int key);
        }

        public interface onAddBatchListener<T> {
            public void onAddBatch(List<KeyValue<Integer, T>> value);
        }

        public interface onChangeBatchListener<T> {
            public void onChangeBatch(List<KeyValue<Integer, T>> value);
        }

        public interface onRemoveBatchListener<T> {
            public void onRemoveBatch(List<KeyValue<Integer, T>> value);
        }

        private Class<T> childType;

        public ArrayList<T> items;

        public onAddListener<T> onAdd;
        public onChangeListener<T> onChange;
        public onRemoveListener<T> onRemove;

        public onAddBatchListener<T> onAddBatch;
        public onChangeBatchListener<T> onChangeBatch;
        public onRemoveBatchListener<T> onRemoveBatch;

        public ArraySchema() {
            items = new ArrayList<>();
        }

        public ArraySchema(Class<T> childType) {
            this.childType = childType;
            items = new ArrayList<>();
        }

        public ArraySchema(Class<T> childType, ArrayList<T> items) {
            this.childType = childType;
            if (items == null) this.items = new ArrayList<>();
            else this.items = items;
        }

        @Override
        public ISchemaCollection _clone() {
            ArraySchema<T> clone = new ArraySchema<>(childType, items);
            clone.onAdd = this.onAdd;
            clone.onChange = this.onChange;
            clone.onRemove = this.onRemove;
            clone.onAddBatch = this.onAddBatch;
            clone.onChangeBatch = this.onChangeBatch;
            clone.onRemoveBatch = this.onRemoveBatch;
            return clone;
        }

        @Override
        public Class<?> getChildType() {
            return childType;
        }

        @Override
        public boolean hasSchemaChild() {
            if (childType == null) return false;
            return Schema.class.isAssignableFrom(childType);
        }

        @Override
        public int count() {
            return items.size();
        }

        @Override
        public Object getItems() {
            return items;
        }

        @Override
        public void setItems(Object items) {
            this.items = (ArrayList<T>) items;
        }

        @Override
        public T get(Integer key) {
            if (key >= 0 && key < items.size())
                return items.get(key);
            return null;
        }

        @Override
        public void set(Integer key, T item) {
            if (key < items.size()) {
                items.set(key, item);
            } else if (key == items.size()) {
                items.add(item);
            }
        }

        @Override
        public void triggerAll() {
            if (onAdd == null) return;
            for (int i = 0; i < items.size(); i++) {
                onAdd.onAdd(items.get(i), i);
            }
        }
    }

    public static class MapSchema<T> implements ISchemaCollection<String, T> {

        public interface onAddListener<T> {
            public void onAdd(T value, String key);
        }

        public interface onChangeListener<T> {
            public void onChange(T value, String key);
        }

        public interface onRemoveListener<T> {
            public void onRemove(T value, String key);
        }

        public interface onAddBatchListener<T> {
            public void onAddBatch(List<KeyValue<String, T>> value);
        }

        public interface onChangeBatchListener<T> {
            public void onChangeBatch(List<KeyValue<String, T>> value);
        }

        public interface onRemoveBatchListener<T> {
            public void onRemoveBatch(List<KeyValue<String, T>> value);
        }

        private Class<T> childType;

        public LinkedHashMap<String, T> items;

        public onAddListener<T> onAdd;
        public onChangeListener<T> onChange;
        public onRemoveListener<T> onRemove;

        public onAddBatchListener<T> onAddBatch;
        public onChangeBatchListener<T> onChangeBatch;
        public onRemoveBatchListener<T> onRemoveBatch;

        public MapSchema() {
            items = new LinkedHashMap<>();
        }

        public MapSchema(Class<T> childType) {
            this.childType = childType;
            items = new LinkedHashMap<>();
        }

        public MapSchema(Class<T> childType, LinkedHashMap<String, T> items) {
            this.childType = childType;
            if (items == null) this.items = new LinkedHashMap<>();
            else this.items = items;
        }

        @Override
        public ISchemaCollection _clone() {
            MapSchema<T> clone = new MapSchema<T>(childType, items);
            clone.onAdd = this.onAdd;
            clone.onChange = this.onChange;
            clone.onRemove = this.onRemove;
            clone.onAddBatch = this.onAddBatch;
            clone.onChangeBatch = this.onChangeBatch;
            clone.onRemoveBatch = this.onRemoveBatch;
            return clone;
        }

        @Override
        public Class<?> getChildType() {
            return childType;
        }

        @Override
        public boolean hasSchemaChild() {
            if (childType == null) return false;
            return Schema.class.isAssignableFrom(childType);
        }

        @Override
        public T get(String key) {
            return items.get(key);
        }

        @Override
        public void set(String key, T item) {
            items.put(key, item);
        }

        @Override
        public Object getItems() {
            return items;
        }

        public void clear() {
            items.clear();
        }

        public boolean contains(String key, T value) {
            T val = items.get(key);
            return val != null && val.equals(value);
        }

        public void remove(String key) {
            items.remove(key);
        }

        public Set<String> keys() {
            return items.keySet();
        }

        public Collection<T> values() {
            return items.values();
        }

        @Override
        public int count() {
            return items.size();
        }

        public boolean containsKey(String key) {
            return items.containsKey(key);
        }

        @Override
        public void triggerAll() {
            if (onAdd == null) return;
            for (String key : items.keySet()) {
                onAdd.onAdd(items.get(key), key);
            }
        }

        @Override
        public void setItems(Object items) {
            this.items = (LinkedHashMap<String, T>) items;
        }
    }

    public void decode(byte[] bytes) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        decode(bytes, new Iterator(0));
    }

    public void decode(byte[] bytes, Iterator it) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Decoder decode = Decoder.getInstance();

        List<Change> changes = new ArrayList<>();

        if (bytes[it.offset] == SPEC.TYPE_ID) {
            it.offset += 2;
        }

        int totalBytes = bytes.length;
        while (it.offset < totalBytes) {
            int index = bytes[it.offset++];
            if (index == SPEC.END_OF_STRUCTURE) {
                break;
            }
            String field = fieldsByIndex.get(index);
            Class<?> fieldType = fieldTypes.get(field);
            String fieldTypeName = fieldTypeNames.get(field);
//            Class<?> childType = fieldChildTypes.get(field);
            String childPrimitiveType = fieldChildTypeNames.get(field);
            Object change = null;
            Object value;
            boolean hasChange;
            switch (fieldTypeName) {
                case "ref":
                    // child schema type
                    if (decode.nilCheck(bytes, it)) {
                        it.offset++;
                        value = null;
                    } else {
                        value = thiz(field);
                        if (value == null) {
                            value = createTypeInstance(bytes, it, fieldType);
                        }
                        ((Schema) value).decode(bytes, it);
                    }

                    hasChange = true;
                    break;
                case "array": {
                    change = new ArrayList<>();

                    ArraySchema valueRef = (ArraySchema) thiz(field);
                    ArraySchema currentValue = (ArraySchema) valueRef._clone();

                    int newLength = (int) decode.decodeNumber(bytes, it);
                    int numChanges = Math.min((int) decode.decodeNumber(bytes, it), newLength);
                    hasChange = numChanges > 0;
                    boolean hasIndexChange = false;

                    // ensure current array has the same length as encoded one
                    if (currentValue.count() > newLength) {
                        for (int i = newLength; i < currentValue.count(); i++) {
                            Object item = currentValue.get(i);
                            if (item instanceof Schema && ((Schema) item).onRemove != null) {
                                ((Schema) item).onRemove.onRemove();
                            }
                            if (currentValue.onRemove != null)
                                currentValue.onRemove.onRemove(item, i);
                        }
                        // reduce items length
                        ArrayList items = (ArrayList) currentValue.getItems();
                        ArrayList newItems = new ArrayList();
                        for (int i = 0; i < newLength; i++) {
                            newItems.add(items.get(i));
                        }
                        currentValue.setItems(newItems);

                    }

                    List<KeyValue<Integer, Object>> addList = null;
                    List<KeyValue<Integer, Object>> changeList = null;
                    List<KeyValue<Integer, Object>> removeList = null;

                    if (currentValue.onAddBatch != null) addList = new ArrayList<>();
                    if (currentValue.onChangeBatch != null) changeList = new ArrayList<>();
                    if (currentValue.onRemoveBatch != null) removeList = new ArrayList<>();

                    for (int i = 0; i < numChanges; i++) {
                        int newIndex = (int) decode.decodeNumber(bytes, it);

                        int indexChangedFrom = -1;
                        if (decode.indexChangeCheck(bytes, it)) {
                            decode.decodeUint8(bytes, it);
                            indexChangedFrom = (int) decode.decodeNumber(bytes, it);
                            hasIndexChange = true;
                        }

                        boolean isNew = (!hasIndexChange && currentValue.get(newIndex) == null) || (hasIndexChange && indexChangedFrom != -1);

                        if (currentValue.hasSchemaChild()) {
                            Schema item;

                            if (isNew) {
                                item = (Schema) createTypeInstance(bytes, it, currentValue.getChildType());
                            } else if (indexChangedFrom != -1) {
                                item = (Schema) valueRef.get(indexChangedFrom);
                            } else {
                                item = (Schema) valueRef.get(newIndex);
                            }

                            if (item == null) {
                                item = (Schema) createTypeInstance(bytes, it, currentValue.getChildType());
                                isNew = true;
                            }

                            if (decode.nilCheck(bytes, it)) {
                                it.offset++;
                                if (item.onRemove != null) item.onRemove.onRemove();
                                if (valueRef.onRemove != null)
                                    valueRef.onRemove.onRemove(item, newIndex);
                                if (removeList != null)
                                    removeList.add(new KeyValue<>(newIndex, item));
                                continue;
                            }

                            item.decode(bytes, it);
                            currentValue.set(newIndex, item);
                        } else {
                            currentValue.set(newIndex, decode.decodePrimitiveType(childPrimitiveType, bytes, it));
                        }

                        if (isNew) {
                            Object item = currentValue.get(newIndex);
                            if (currentValue.onAdd != null)
                                currentValue.onAdd.onAdd(item, newIndex);
                            if (addList != null) addList.add(new KeyValue<>(newIndex, item));
                        } else {
                            Object item = currentValue.get(newIndex);
                            if (currentValue.onChange != null)
                                currentValue.onChange.onChange(item, newIndex);
                            if (changeList != null) changeList.add(new KeyValue<>(newIndex, item));
                        }

                        ((ArrayList) change).add(currentValue.get(newIndex));
                    }

                    if (currentValue.onAddBatch != null && addList != null && !addList.isEmpty())
                        currentValue.onAddBatch.onAddBatch(addList);
                    if (currentValue.onChangeBatch != null && changeList != null && !changeList.isEmpty())
                        currentValue.onChangeBatch.onChangeBatch(changeList);
                    if (currentValue.onRemoveBatch != null && removeList != null && !removeList.isEmpty())
                        currentValue.onRemoveBatch.onRemoveBatch(removeList);

                    value = currentValue;

                    break;
                }
                case "map": {
                    MapSchema valueRef = (MapSchema) thiz(field);
                    MapSchema currentValue = (MapSchema) valueRef._clone();

                    int length = (int) decode.decodeNumber(bytes, it);
                    hasChange = (length > 0);

                    boolean hasIndexChange = false;

                    LinkedHashMap items = (LinkedHashMap) currentValue.getItems();
                    Object[] keys = items.keySet().toArray();
                    String[] mapKeys = new String[items.size()];
                    for (int i = 0; i < keys.length; i++) {
                        mapKeys[i] = (String) keys[i];
                    }

                    List<KeyValue<String, Object>> addList = null;
                    List<KeyValue<String, Object>> changeList = null;
                    List<KeyValue<String, Object>> removeList = null;

                    if (currentValue.onAddBatch != null) addList = new ArrayList<>();
                    if (currentValue.onChangeBatch != null) changeList = new ArrayList<>();
                    if (currentValue.onRemoveBatch != null) removeList = new ArrayList<>();

                    for (int i = 0; i < length; i++) {
                        // `encodeAll` may indicate a higher number of indexes it actually encodes
                        // TODO: do not encode a higher number than actual encoded entries
                        if (it.offset > bytes.length || bytes[it.offset] == SPEC.END_OF_STRUCTURE) {
                            break;
                        }

                        String previousKey = null;
                        if (decode.indexChangeCheck(bytes, it)) {
                            it.offset++;
                            previousKey = mapKeys[(int) decode.decodeNumber(bytes, it)];
                            hasIndexChange = true;
                        }

                        boolean hasMapIndex = decode.numberCheck(bytes, it);
                        boolean isSchemaType = currentValue.hasSchemaChild();

                        String newKey = (hasMapIndex)
                                ? mapKeys[(int) decode.decodeNumber(bytes, it)]
                                : decode.decodeString(bytes, it);

                        Object item;
                        boolean isNew = (!hasIndexChange && valueRef.get(newKey) == null) || (hasIndexChange && previousKey == null && hasMapIndex);

                        if (isNew && isSchemaType) {
                            item = createTypeInstance(bytes, it, currentValue.getChildType());
                        } else if (previousKey != null) {
                            item = valueRef.get(previousKey);
                        } else {
                            item = valueRef.get(newKey);
                        }

                        if (decode.nilCheck(bytes, it)) {
                            it.offset++;

                            if (item instanceof Schema && ((Schema) item).onRemove != null) {
                                ((Schema) item).onRemove.onRemove();
                            }

                            if (valueRef.onRemove != null) valueRef.onRemove.onRemove(item, newKey);
                            if (removeList != null) removeList.add(new KeyValue<>(newKey, item));
                            items.remove(newKey);
                            continue;

                        } else if (!isSchemaType) {
                            currentValue.set(newKey, decode.decodePrimitiveType(childPrimitiveType, bytes, it));
                        } else {
                            ((Schema) item).decode(bytes, it);
                            currentValue.set(newKey, item);
                        }

                        if (isNew) {
                            Object _item = currentValue.get(newKey);
                            if (currentValue.onAdd != null)
                                currentValue.onAdd.onAdd(_item, newKey);
                            if (addList != null)
                                addList.add(new KeyValue<>(newKey, _item));
                        } else {
                            Object _item = currentValue.get(newKey);
                            if (currentValue.onChange != null)
                                currentValue.onChange.onChange(_item, newKey);
                            if (changeList != null)
                                changeList.add(new KeyValue<>(newKey, _item));
                        }
                    }

                    if (currentValue.onAddBatch != null && addList != null && !addList.isEmpty())
                        currentValue.onAddBatch.onAddBatch(addList);
                    if (currentValue.onChangeBatch != null && changeList != null && !changeList.isEmpty())
                        currentValue.onChangeBatch.onChangeBatch(changeList);
                    if (currentValue.onRemoveBatch != null && removeList != null && !removeList.isEmpty())
                        currentValue.onRemoveBatch.onRemoveBatch(removeList);

                    value = currentValue;
                    break;
                }
                default:
                    // Primitive type
                    value = decode.decodePrimitiveType(fieldTypeName, bytes, it);
                    hasChange = true;
                    break;
            }

            if (hasChange) {
                Change dataChange = new Change();
                dataChange.field = field;
                dataChange.value = (change != null) ? change : value;
                dataChange.previousValue = thiz(field);
                changes.add(dataChange);
            }

            Field f = getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(this, value);
        }
        if (!changes.isEmpty() && onChange != null) {
            onChange.onChange(changes);
        }
    }

    public void triggerAll() {
        if (onChange == null) return;
        try {
            List<Change> changes = new ArrayList<>();
            for (String field : fieldsByIndex.values()) {
                Object value = thiz(field);
                if (value != null) {
                    Change change = new Change();
                    change.field = field;
                    change.value = value;
                    change.previousValue = null;
                    changes.add(change);
                }
            }
            onChange.onChange(changes);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected Object thiz(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(this);
    }

    protected Object createTypeInstance(byte[] bytes, Iterator it, Class<?> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (bytes[it.offset] == SPEC.TYPE_ID) {
            it.offset++;
            int typeId = Decoder.getInstance().decodeUint8(bytes, it);
            Type anotherType = Context.getInstance().get(typeId);
            return anotherType.getClass().getConstructor().newInstance();
        } else {
            Constructor constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
    }

    @SchemaClass
    public static class SchemaReflectionField extends Schema {
        @SchemaField("0/string")
        public String name;

        @SchemaField("1/string")
        public String type;

        @SchemaField("2/uint8")
        public int referencedType;
    }

    @SchemaClass
    public static class SchemaReflectionType extends Schema {
        @SchemaField("0/uint8")
        public int id;

        @SchemaField("1/array/SchemaReflectionField")
        public ArraySchema<SchemaReflectionField> fields = new ArraySchema<>(SchemaReflectionField.class);
    }

    @SchemaClass
    public static class SchemaReflection extends Schema {
        @SchemaField("0/array/SchemaReflectionType")
        public ArraySchema<SchemaReflectionType> types = new ArraySchema<>(SchemaReflectionType.class);

        @SchemaField("1/uint8")
        public int rootType;
    }

    public static class Context {
        protected static Context instance = new Context();
        protected LinkedHashMap<Integer, Type> typeIds = new LinkedHashMap<>();

        public static Context getInstance() {
            return instance;
        }

        public Type get(int typeid) {
            return typeIds.get(typeid);
        }
    }

    public static class SPEC {
        public static final byte END_OF_STRUCTURE = (byte) 193;
        public static final byte NIL = (byte) 192;
        public static final byte INDEX_CHANGE = (byte) 212;
        public static final byte TYPE_ID = (byte) 213;
    }

    public static class KeyValue<K, V> {
        K key;
        V value;

        public KeyValue(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

}