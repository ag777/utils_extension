package com.ag777.util.lang.collection;

import java.util.Collection;
import java.util.function.Function;

/**
 * 分页工具类，提供了计算分页的静态方法。
 * @author ag777 <837915770@vip.qq.com>
 * @version 2023/12/29 9:56
 */
public class PaginationUtil {

    /**
     * 对多个集合进行跨集合分页计算的方法。
     * 这个方法通过计算每个集合中的起始索引和限制数（即每个集合中应该显示的条目数量），
     * 来支持在一次分页操作中跨越多个集合。
     * 这样可以处理多个数据源或不同类型的集合，实现统一的分页逻辑。
     *
     * @param <T>             分页对象的类型
     * @param pageableEntities 分页对象集合，每个对象代表一个可分页的实体及其大小
     * @param getPageSize     函数，用于获取分页对象的大小
     * @param setStartLimit   函数，用于设置分页对象的起始索引和限制数
     * @param pageIndex       当前页索引，从0开始
     * @param pageSize        每页的限制数
     */
    public static <T> void calculateCrossCollectionPagination(
            Collection<T> pageableEntities,
            Function<T, Integer> getPageSize,
            StartLimitSetter<T> setStartLimit,
            int pageIndex,
            int pageSize) {

        int currentPageStart = pageIndex * pageSize; // 当前页的起始索引
        int itemsLeftOnPage = pageSize; // 当前页剩余的条目数

        for (T entity : pageableEntities) {
            int entitySize = getPageSize.apply(entity); // 获取分页对象的大小
            if (currentPageStart >= entitySize) {
                // 如果当前页的起始索引超过了实体大小，则跳过这个实体
                currentPageStart -= entitySize;
                setStartLimit.set(entity, 0, 0); // 初始化起始索引和限制数为0
            } else {
                // 为这个实体计算起始索引和限制数
                int availableItems = entitySize - currentPageStart; // 从起始索引开始实体中可用的条目数
                int itemsToTake = Math.min(availableItems, itemsLeftOnPage); // 取的条目是可用条目和页剩余限制数中的较小者
                setStartLimit.set(entity, currentPageStart, itemsToTake);

                // 更新计数器
                currentPageStart = 0; // 为下一个实体重置起始索引
                itemsLeftOnPage -= itemsToTake; // 减少页剩余的条目数

                if (itemsLeftOnPage == 0) {
                    // 如果当前页的条目已满，则不再继续计算后续实体的分页
                    break;
                }
            }
        }
    }

    /**
     * 计算基于多个集合的总页数。
     * 该方法会遍历所有集合并累计每个集合的元素数量，然后根据指定的每页条目数来计算总页数。
     *
     * @param <T>               分页对象的类型
     * @param pageableEntities  分页对象集合，每个对象代表一个可分页的实体及其大小
     * @param getPageSize       函数，用于获取分页对象的大小
     * @param pageSize          每页的限制数
     * @return                  根据所有集合累计的元素数量和每页条目数计算出的总页数
     */
    public static <T> int calculateTotalPages(
            Collection<T> pageableEntities,
            Function<T, Integer> getPageSize,
            int pageSize) {
        int totalItems = 0;
        for (T entity : pageableEntities) {
            totalItems += getPageSize.apply(entity);
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    /**
     * 根据总项数和每页的大小计算总页数。
     *
     * @param totalItems 总项数，即所有集合中元素的总和。
     * @param pageSize   每页的限制数，即每页最多可以显示的条目数量。
     * @return           总页数，根据总项数和每页大小计算得出。
     */
    public static int calculateNumberOfPages(int totalItems, int pageSize) {
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    /**
     * 函数式接口，用于设置对象的起始索引和限制数。
     *
     * @param <T> 对象的类型
     */
    @FunctionalInterface
    public interface StartLimitSetter<T> {
        /**
         * 设置对象的起始索引和限制数。
         *
         * @param entity 对象实体
         * @param start  起始索引
         * @param limit  限制数
         */
        void set(T entity, int start, int limit);
    }
}
