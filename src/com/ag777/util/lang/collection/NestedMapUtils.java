package com.ag777.util.lang.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 嵌套映射工具类，提供了在嵌套的Map结构中进行操作的方法。
 * 它允许用户通过点分隔的字符串键（例如 "a.b.c"）来访问，修改和删除嵌套的Map中的元素。
 * 这个类的方法使得在复杂的嵌套Map结构中读取和写入数据变得简单和直观。
 * <p>
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/1/4 10:58
 */
public class NestedMapUtils {
    private NestedMapUtils() {}

    /**
     * 根据以点号分隔的嵌套键在Map中放置一个值。如果在键的层次结构中某个中间键不存在，
     * 或者对应的值不是Map类型，则创建一个新的HashMap。
     * 调用示例：putValueByNestedKey(map, "a.b.c", 1) 会在map中创建或更新路径为 {"a":{"b":{"c":1}}}
     *
     * @param map   要操作的Map
     * @param key   以点号分隔的嵌套键
     * @param value 要放置的值
     * @return 之前与给定键相关联的值，如果之前没有与该键相关联的值，则返回null
     */
    public static Object put(Map<String, Object> map, String key, Object value) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = map;

        // 遍历所有的键，除了最后一个
        for (int i = 0; i < keys.length - 1; i++) {
            String currentKey = keys[i];
            Object obj = currentMap.get(currentKey);

            // 如果当前键对应的不是Map类型，则创建一个新的HashMap
            if (!(obj instanceof Map)) {
                obj = new HashMap<String, Object>();
                currentMap.put(currentKey, obj);
            }

            // 更新当前Map为下一层的Map
            currentMap = (Map<String, Object>) obj;
        }

        // 在最深层的Map中放置值
        return currentMap.put(keys[keys.length - 1], value);
    }

    /**
     * 根据以点号分隔的嵌套键从Map中获取一个值。如果在键的层次结构中某个中间键不存在，
     * 或者对应的值不是Map类型，则返回指定的默认值。
     * 调用示例：Object value = getValueByNestedKeyOrDefault(map, "a.b.c", "default") 可能会返回map中路径为 {"a":{"b":{"c":1}}} 的值1，或者返回"default"。
     *
     * @param map          要操作的Map
     * @param key          以点号分隔的嵌套键
     * @param defaultValue 如果找不到值时返回的默认值
     * @return 返回找到的值，如果没有找到则返回默认值
     */
     public static <V>V getOrDefault(Map<String, Object> map, String key, V defaultValue) {
        V value = get(map, key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /**
     * 根据以点号分隔的嵌套键从Map中获取一个值。如果在键的层次结构中某个中间键不存在，
     * 或者对应的值不是Map类型，则返回null。
     * 调用示例：Object value = getValueByNestedKey(map, "a.b.c") 可能会返回map中路径为 {"a":{"b":{"c":1}}} 的值1。
     *
     * @param map 要操作的Map
     * @param key 以点号分隔的嵌套键
     * @return 返回找到的值，如果没有找到则返回null
     */
    public static <V>V get(Map<String, Object> map, String key) {
        String[] keys = key.split("\\.");
        Object current = map;

        // 遍历所有的键
        for (String k : keys) {
            if (!(current instanceof Map)) {
                return null; // 如果当前对象不是Map，返回null
            }
            current = ((Map<?, ?>) current).get(k);
            if (current == null) {
                return null; // 如果键不存在，返回null
            }
        }

        return (V) current; // 返回找到的值
    }

    /**
     * 使用循环删除嵌套Map中的键，如果删除后父Map为空，则连父Map一起删除。
     * @param map 要操作的Map
     * @param nestedKey 以点号分隔的嵌套键
     * @return 如果删除成功返回true，如果键不存在返回false
     */
    public static boolean remove(Map<String, Object> map, String nestedKey) {
        String[] keys = nestedKey.split("\\.");
        Stack<Map<String, Object>> mapStack = new Stack<>();
        Map<String, Object> currentMap = map;

        // 遍历嵌套键，直到找到最后一个键或者路径中断
        for (int i = 0; i < keys.length - 1; i++) {
            Object value = currentMap.get(keys[i]);
            if (!(value instanceof Map)) {
                return false; // 路径中断，未找到完整路径
            }
            mapStack.push(currentMap);
            currentMap = (Map<String, Object>) value;
        }

        // 删除最后一个键
        Object removedValue = currentMap.remove(keys[keys.length - 1]);
        if (removedValue == null) {
            return false; // 最后一个键不存在
        }

        // 如果当前Map为空，回溯并尝试删除父Map中的键
        while (!mapStack.isEmpty() && currentMap.isEmpty()) {
            currentMap = mapStack.pop();
            currentMap.remove(keys[mapStack.size()]);
        }

        return true;
    }
}
