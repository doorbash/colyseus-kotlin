package io.colyseus.state_listener;

import java.util.*;

public class Compare {

    public static List<PatchObject> compare(LinkedHashMap<String, Object> tree1, LinkedHashMap<String, Object> tree2) {
        List<PatchObject> patches = new ArrayList<>();
        generate(tree1, tree2, patches, new ArrayList<String>());
        return patches;
    }

    public static void generate(List<Object> mirror, List<Object> obj, List<PatchObject> patches, List<String> path) {
        LinkedHashMap<String, Object> mirrorDict = new LinkedHashMap<>();
        for (int i = 0; i < mirror.size(); i++) {
            mirrorDict.put(String.valueOf(i), mirror.get(i));
        }

        LinkedHashMap<String, Object> objDict = new LinkedHashMap<>();
        for (int i = 0; i < obj.size(); i++) {
            objDict.put(String.valueOf(i), obj.get(i));
        }

        generate(mirrorDict, objDict, patches, path);
    }

    public static void generate(LinkedHashMap<String, Object> mirror, LinkedHashMap<String, Object> obj, List<PatchObject> patches, List<String> path) {
        Set<String> newKeys = obj.keySet();
        Set<String> oldKeys = mirror.keySet();
        boolean deleted = false;
        for (String key : oldKeys) {
            if (obj.containsKey(key) && obj.get(key) != null && !(!obj.containsKey(key) && mirror.containsKey(key) && !(obj instanceof List))) {
                Object oldVal = mirror.get(key);
                Object newVal = obj.get(key);

                if (oldVal != null && newVal != null &&
                        !isPrimitive(oldVal.getClass()) && !(oldVal instanceof String) &&
                        !isPrimitive(newVal.getClass()) && !(newVal instanceof String) &&
                        oldVal.getClass().equals(newVal.getClass())) {
                    List<String> deeperPath = new ArrayList<>(path);
                    deeperPath.add(key);

                    if (oldVal instanceof LinkedHashMap) {
                        generate((LinkedHashMap<String, Object>) oldVal, (LinkedHashMap<String, Object>) newVal, patches, deeperPath);

                    } else if (oldVal instanceof List) {
                        generate(((List<Object>) oldVal), ((List<Object>) newVal), patches, deeperPath);
                    }

                } else {
                    if ((oldVal == null && newVal != null) || !oldVal.equals(newVal)) {
                        List<String> replacePath = new ArrayList<>(path);
                        replacePath.add(key);
                        patches.add(new PatchObject("replace", replacePath, newVal));
                    }
                }
            } else {
                List<String> removePath = new ArrayList<>(path);
                removePath.add(key);
                patches.add(new PatchObject("remove", removePath, null));
                deleted = true; // property has been deleted
            }
        }

        if (!deleted && newKeys.size() == oldKeys.size()) {
            return;
        }
        for (String key : newKeys) {
            if (!mirror.containsKey(key) && obj.containsKey(key)) {
                List<String> addPath = new ArrayList<>(path);
                addPath.add(key);
                Object newVal = obj.get(key);
                if (newVal != null) {
                    // compare deeper additions
                    if (!isPrimitive(newVal.getClass()) && (newVal instanceof String)) {
                        if (newVal instanceof LinkedHashMap) {
                            generate(new LinkedHashMap<String, Object>(), (LinkedHashMap<String, Object>) newVal, patches, addPath);
                        } else if (newVal instanceof List) {
                            generate(new ArrayList<>(), (List<Object>) newVal, patches, addPath);
                        }
                    }
                }
                patches.add(new PatchObject("add", addPath, newVal));
            }
        }
    }


    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    private static boolean isPrimitive(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        return ret;
    }


}

