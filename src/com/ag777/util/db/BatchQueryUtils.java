package com.ag777.util.db;

import com.ag777.util.lang.collection.ListUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 批量查询并绑定详情工具类（生产终极版）
 * <p>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2025/11/28
 */
public final class BatchQueryUtils {

    private BatchQueryUtils() {}

    /**
     * 批量查询并绑定详情（收集模式）- 最常用模式
     * <p>
     * 将源列表中的每个对象批量查询其详情，并将详情收集为列表后统一绑定回源对象。
     * 推荐在 90% 的场景下使用此方法。
     *
     * @param <T1> 源对象类型
     * @param <T2> 详情对象类型
     * @param <K> 键类型（通常是ID类型）
     * @param sourceList 源对象列表，不能为空
     * @param sourceIdExtractor 从源对象中提取ID的函数
     * @param batchSize 批量查询的大小，必须大于0
     * @param batchQuery 批量查询函数，输入ID集合，返回详情列表
     * @param detailIdExtractor 从详情对象中提取ID的函数
     * @param detailCollector 详情收集器，用于将详情列表绑定到源对象
     */
    public static <T1, T2, K> void batchAttachAndCollect(
            List<T1> sourceList,
            Function<T1, K> sourceIdExtractor,
            int batchSize,
            Function<Set<K>, List<T2>> batchQuery,
            Function<T2, K> detailIdExtractor,
            BiConsumer<T1, List<T2>> detailCollector) {

        if (ListUtils.isEmpty(sourceList) || batchSize <= 0) {
            return;
        }

        // 用于收集每个源对象对应的详情列表，使用 IdentityHashMap 避免对象比较问题
        Map<T1, List<T2>> resultMap = new IdentityHashMap<>(sourceList.size());

        // 批处理映射：ID -> 该ID对应的源对象列表（支持同一ID的多个对象）
        Map<K, List<T1>> batchIdMap = new HashMap<>(Math.min(batchSize, 512));

        for (T1 source : sourceList) {
            K id = sourceIdExtractor.apply(source);
            if (id == null) {
                // ID为空的源对象直接绑定空列表，避免后续出现NPE
                detailCollector.accept(source, Collections.emptyList());
                continue;
            }

            batchIdMap.computeIfAbsent(id, k -> new ArrayList<>()).add(source);

            if (batchIdMap.size() >= batchSize) {
                processBatch(batchIdMap, batchQuery, detailIdExtractor, resultMap);
                batchIdMap.clear();
            }
        }

        // 处理剩余的最后一小批数据
        if (!batchIdMap.isEmpty()) {
            processBatch(batchIdMap, batchQuery, detailIdExtractor, resultMap);
        }

        // 统一将结果绑定回源对象，没有查询到的绑定空列表
        for (T1 source : sourceList) {
            detailCollector.accept(source, resultMap.getOrDefault(source, Collections.emptyList()));
        }
    }

    /**
     * 批量查询并绑定详情（回调模式）- 适合复杂处理逻辑
     * <p>
     * 将源列表中的每个对象批量查询其详情，每匹配到一个详情就立即回调处理。
     * 适用于需要在匹配时进行复杂处理逻辑的场景。
     *
     * @param <T1> 源对象类型
     * @param <T2> 详情对象类型
     * @param <K> 键类型（通常是ID类型）
     * @param sourceList 源对象列表，不能为空
     * @param sourceIdExtractor 从源对象中提取ID的函数
     * @param batchSize 批量查询的大小，必须大于0
     * @param batchQuery 批量查询函数，输入ID集合，返回详情列表
     * @param detailIdExtractor 从详情对象中提取ID的函数
     * @param onMatch 匹配时的回调函数，参数为（源对象、详情对象、匹配的ID）
     */
    public static <T1, T2, K> void batchAttach(
            List<T1> sourceList,
            Function<T1, K> sourceIdExtractor,
            int batchSize,
            Function<Set<K>, List<T2>> batchQuery,
            Function<T2, K> detailIdExtractor,
            TriConsumer<T1, T2, K> onMatch) {

        if (ListUtils.isEmpty(sourceList) || batchSize <= 0) {
            return;
        }

        Map<K, List<T1>> batchIdMap = new HashMap<>(Math.min(batchSize, 512));

        for (T1 source : sourceList) {
            K id = sourceIdExtractor.apply(source);
            if (id == null) {
                continue; // 跳过空 id
            }

            batchIdMap.computeIfAbsent(id, k -> new ArrayList<>()).add(source);

            if (batchIdMap.size() >= batchSize) {
                processBatch(batchIdMap, batchQuery, detailIdExtractor, onMatch);
                batchIdMap.clear();
            }
        }

        if (!batchIdMap.isEmpty()) {
            processBatch(batchIdMap, batchQuery, detailIdExtractor, onMatch);
        }
    }

    /** 处理收集模式批次数据的私有方法 */
    private static <T1, T2, K> void processBatch(
            Map<K, List<T1>> batchIdMap,
            Function<Set<K>, List<T2>> batchQuery,
            Function<T2, K> detailIdExtractor,
            Map<T1, List<T2>> resultMap) {

        List<T2> details;
        try {
            details = batchQuery.apply(batchIdMap.keySet());
        } catch (Exception e) {
            // 单个批次查询失败不影响其他批次，可根据需要替换为具体的日志框架
            System.err.println("批量查询失败，涉及的ID: " + batchIdMap.keySet() + ", 错误信息: " + e);
            return;
        }

        if (ListUtils.isEmpty(details)) {
            return;
        }

        // 将查询到的详情按ID分组，支持同一ID对应多条详情记录
        Map<K, List<T2>> detailGroup = new HashMap<>();
        for (T2 detail : details) {
            K id = detailIdExtractor.apply(detail);
            if (id != null) {
                detailGroup.computeIfAbsent(id, k -> new ArrayList<>()).add(detail);
            }
        }

        // 将分组后的详情绑定到对应的源对象
        for (Map.Entry<K, List<T1>> entry : batchIdMap.entrySet()) {
            K id = entry.getKey();
            List<T2> detailList = detailGroup.getOrDefault(id, Collections.emptyList());
            for (T1 source : entry.getValue()) {
                resultMap.computeIfAbsent(source, k -> new ArrayList<>()).addAll(detailList);
            }
        }
    }

    /** 处理回调模式批次数据的私有方法 */
    private static <T1, T2, K> void processBatch(
            Map<K, List<T1>> batchIdMap,
            Function<Set<K>, List<T2>> batchQuery,
            Function<T2, K> detailIdExtractor,
            TriConsumer<T1, T2, K> onMatch) {

        List<T2> details;
        try {
            details = batchQuery.apply(batchIdMap.keySet());
        } catch (Exception e) {
            // 单个批次查询失败不影响其他批次，可根据需要替换为具体的日志框架
            System.err.println("批量查询失败，涉及的ID: " + batchIdMap.keySet() + ", 错误信息: " + e);
            return;
        }

        if (ListUtils.isEmpty(details)) {
            return;
        }

        for (T2 detail : details) {
            K id = detailIdExtractor.apply(detail);
            if (id == null) continue;

            List<T1> sources = batchIdMap.get(id);
            if (sources != null) {
                for (T1 source : sources) {
                    onMatch.accept(source, detail, id);
                }
            }
        }
    }

    /**
     * 三参数消费型函数式接口
     * <p>
     * 类似于 {@link java.util.function.BiConsumer}，但接受三个参数。
     * 用于在批量查询匹配时进行回调处理。
     *
     * @param <T1> 第一个参数类型
     * @param <T2> 第二个参数类型
     * @param <T3> 第三个参数类型
     */
    @FunctionalInterface
    public interface TriConsumer<T1, T2, T3> {
        /**
         * 执行接受操作
         *
         * @param t1 第一个参数
         * @param t2 第二个参数
         * @param t3 第三个参数
         */
        void accept(T1 t1, T2 t2, T3 t3);
    }
}