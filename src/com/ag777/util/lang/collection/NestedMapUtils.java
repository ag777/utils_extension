package com.ag777.util.lang.collection;

import com.ag777.util.lang.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

/**
 * 嵌套映射工具类，提供了在嵌套的Map结构中进行操作的方法。
 * 它允许用户通过点分隔的字符串键（例如 "a.b.c"）来访问，修改和删除嵌套的Map中的元素。
 * 这个类的方法使得在复杂的嵌套Map结构中读取和写入数据变得简单和直观。
 * <p>
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/02/01 11:03
 */
public class NestedMapUtils {
    private NestedMapUtils() {}

    /**
     * 根据以点号分隔的嵌套键在Map中放置一个值。如果在键的层次结构中某个中间键不存在，
     * 或者对应的值不是Map类型，则创建一个新的HashMap。
     * 调用示例：put(map, "a.b.c", 1) 会在map中创建或更新路径为 {"a":{"b":{"c":1}}}
     *
     * @param map   要操作的Map
     * @param key   以点号分隔的嵌套键
     * @param value 要放置的值
     * @return 之前与给定键相关联的值，如果之前没有与该键相关联的值，则返回null
     */
    @SuppressWarnings("unchecked")
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
     *
     * @param map          要操作的Map
     * @param key          以点号分隔的嵌套键
     * @param defaultValue 如果找不到值时返回的默认值
     * @return 返回找到的值，如果没有找到则返回默认值
     */
    public static <V> V getOrDefault(Map<String, Object> map, String key, V defaultValue) {
        V value = get(map, key);
        return value != null ? value : defaultValue;
    }

    /**
     * 根据以点号分隔的嵌套键从Map中获取一个值。如果在键的层次结构中某个中间键不存在，
     * 或者对应的值不是Map类型，则返回null。
     *
     * @param map 要操作的Map
     * @param key 以点号分隔的嵌套键
     * @param <V> 值类型
     * @return 返回找到的值，如果没有找到则返回null
     */
    @SuppressWarnings("unchecked")
    public static <V> V get(Map<String, Object> map, String key) {
        String[] keys = key.split("\\.");
        Object current = map;

        for (String k : keys) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<?, ?>) current).get(k);
            if (current == null) {
                return null;
            }
        }

        return (V) current;
    }

    /**
     * 获取嵌套键对应值并转为String。
     *
     * @param map map
     * @param key 嵌套键
     * @return String值，转换失败返回null
     */
    public static String getStr(Map<String, Object> map, String key) {
        return getAs(map, key, ObjectUtils::toStr);
    }

    /**
     * 获取嵌套键对应值并转为String。
     *
     * @param map map
     * @param key 嵌套键
     * @param defaultValue 默认值
     * @return String值，转换失败返回默认值
     */
    public static String getStr(Map<String, Object> map, String key, String defaultValue) {
        return getAs(map, key, obj -> ObjectUtils.toStr(obj, defaultValue), defaultValue);
    }

    /**
     * 获取嵌套键对应值并转为Double。
     *
     * @param map map
     * @param key 嵌套键
     * @return Double值，转换失败返回null
     */
    public static Double getDouble(Map<String, Object> map, String key) {
        return getAs(map, key, ObjectUtils::toDouble);
    }

    /**
     * 获取嵌套键对应值并转为double。
     *
     * @param map map
     * @param key 嵌套键
     * @param defaultValue 默认值
     * @return double值，转换失败返回默认值
     */
    public static double getDouble(Map<String, Object> map, String key, double defaultValue) {
        return getAs(map, key, obj -> ObjectUtils.toDouble(obj, defaultValue), defaultValue);
    }

    /**
     * 获取嵌套键对应值并转为Float。
     *
     * @param map map
     * @param key 嵌套键
     * @return Float值，转换失败返回null
     */
    public static Float getFloat(Map<String, Object> map, String key) {
        return getAs(map, key, ObjectUtils::toFloat);
    }

    /**
     * 获取嵌套键对应值并转为float。
     *
     * @param map map
     * @param key 嵌套键
     * @param defaultValue 默认值
     * @return float值，转换失败返回默认值
     */
    public static float getFloat(Map<String, Object> map, String key, float defaultValue) {
        return getAs(map, key, obj -> ObjectUtils.toFloat(obj, defaultValue), defaultValue);
    }

    /**
     * 获取嵌套键对应值并转为Integer。
     *
     * @param map map
     * @param key 嵌套键
     * @return Integer值，转换失败返回null
     */
    public static Integer getInt(Map<String, Object> map, String key) {
        return getAs(map, key, ObjectUtils::toInt);
    }

    /**
     * 获取嵌套键对应值并转为int。
     *
     * @param map map
     * @param key 嵌套键
     * @param defaultValue 默认值
     * @return int值，转换失败返回默认值
     */
    public static int getInt(Map<String, Object> map, String key, int defaultValue) {
        return getAs(map, key, obj -> ObjectUtils.toInt(obj, defaultValue), defaultValue);
    }

    /**
     * 获取嵌套键对应值并转为Long。
     *
     * @param map map
     * @param key 嵌套键
     * @return Long值，转换失败返回null
     */
    public static Long getLong(Map<String, Object> map, String key) {
        return getAs(map, key, ObjectUtils::toLong);
    }

    /**
     * 获取嵌套键对应值并转为long。
     *
     * @param map map
     * @param key 嵌套键
     * @param defaultValue 默认值
     * @return long值，转换失败返回默认值
     */
    public static long getLong(Map<String, Object> map, String key, long defaultValue) {
        return getAs(map, key, obj -> ObjectUtils.toLong(obj, defaultValue), defaultValue);
    }

    /**
     * 获取嵌套键对应值并转为Boolean。
     *
     * @param map map
     * @param key 嵌套键
     * @return Boolean值，转换失败返回null
     */
    public static Boolean getBoolean(Map<String, Object> map, String key) {
        return getAs(map, key, ObjectUtils::toBoolean);
    }

    /**
     * 获取嵌套键对应值并转为boolean。
     *
     * @param map map
     * @param key 嵌套键
     * @param defaultValue 默认值
     * @return boolean值，转换失败返回默认值
     */
    public static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        return getAs(map, key, obj -> ObjectUtils.toBoolean(obj, defaultValue), defaultValue);
    }

    /**
     * 获取嵌套键对应值并转为Date。
     *
     * @param map map
     * @param key 嵌套键
     * @return Date值，转换失败返回null
     */
    public static Date getDate(Map<String, Object> map, String key) {
        return getAs(map, key, ObjectUtils::toDate);
    }

    /**
     * 获取嵌套键对应值并转为Date。
     *
     * @param map map
     * @param key 嵌套键
     * @param defaultValue 默认值
     * @return Date值，转换失败返回默认值
     */
    public static Date getDate(Map<String, Object> map, String key, Date defaultValue) {
        return getAs(map, key, obj -> ObjectUtils.toDate(obj, defaultValue), defaultValue);
    }

    /**
     * 从嵌套的Map中移除指定的嵌套键，并返回被移除的对象。如果嵌套键不存在，返回null。
     * 如果移除后父Map为空，则连父Map一起移除。
     *
     * @param map 要操作的Map
     * @param nestedKey 以点号分隔的嵌套键
     * @param <V> 值类型
     * @return 被移除的对象，如果嵌套键不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public static <V> V remove(Map<String, Object> map, String nestedKey) {
        String[] keys = nestedKey.split("\\.");
        Stack<Map<String, Object>> mapStack = new Stack<>();
        Map<String, Object> currentMap = map;

        for (int i = 0; i < keys.length - 1; i++) {
            Object value = currentMap.get(keys[i]);
            if (!(value instanceof Map)) {
                return null;
            }
            mapStack.push(currentMap);
            currentMap = (Map<String, Object>) value;
        }

        Object removedValue = currentMap.remove(keys[keys.length - 1]);
        if (removedValue == null) {
            return null;
        }

        while (!mapStack.isEmpty() && currentMap.isEmpty()) {
            currentMap = mapStack.pop();
            currentMap.remove(keys[mapStack.size()]);
        }

        return (V) removedValue;
    }

    /**
     * 获取并转换指定嵌套键的值。
     *
     * @param map map
     * @param key 嵌套键
     * @param converter 转换器
     * @param <T> 目标类型
     * @return 转换后的值
     */
    private static <T> T getAs(Map<String, Object> map, String key, Function<Object, T> converter) {
        return converter.apply(get(map, key));
    }

    /**
     * 获取并转换指定嵌套键的值，支持默认值兜底。
     *
     * @param map map
     * @param key 嵌套键
     * @param converter 转换器
     * @param defaultValue 默认值
     * @param <T> 目标类型
     * @return 转换后的值，如果为null则返回默认值
     */
    private static <T> T getAs(Map<String, Object> map, String key, Function<Object, T> converter, T defaultValue) {
        T value = getAs(map, key, converter);
        return value != null ? value : defaultValue;
    }
}
