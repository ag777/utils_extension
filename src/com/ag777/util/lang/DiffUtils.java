package com.ag777.util.lang;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 差分计算工具类
 * 用于计算两个列表之间的差异，支持自定义唯一键和更新判断逻辑
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/1/9 11:30
 */
public class DiffUtils {

    /**
     * 计算两个列表之间的差异
     * 
     * @param <T> 对象类型，列表中元素的类型
     * @param <K> 唯一键类型，用于标识对象的唯一性
     * @param remoteList 远程集合（源集合），包含最新的数据
     * @param localList 本地集合（目标集合），包含现有的数据
     * @param toUniqueKey 从对象中提取唯一键的函数，参数T为对象类型，返回K为唯一键类型
     * @param getUpdateItem 判断是否需要更新的函数，第一个参数T为远程对象，第二个参数T为本地对象，返回T为需要更新的对象或null表示不需要更新
     * @return 差异结果，包含新增、删除、更新列表
     * @throws IllegalArgumentException 当参数为null时抛出异常
     */
    public static <T, K> Diff<T> diff(Collection<T> remoteList, Collection<T> localList, 
                                     Function<T, K> toUniqueKey, 
                                     BiFunction<T, T, T> getUpdateItem) {
        if (remoteList == null || localList == null) {
            throw new IllegalArgumentException("远程集合和本地集合不能为null");
        }
        if (toUniqueKey == null) {
            throw new IllegalArgumentException("唯一键提取函数不能为null");
        }
        
        // 将本地列表转换为Map，便于快速查找
        Map<K, T> localMap = localList.stream()
                .collect(Collectors.toMap(
                        toUniqueKey,
                        item -> item,
                        (existing, replacement) -> existing // 处理重复键，保留第一个
                ));
        
        List<T> addList = new ArrayList<>();
        List<T> updateList = new ArrayList<>();
        Set<K> processedKeys = new HashSet<>();
        
        // 遍历远程列表，判断是新增还是更新
        for (T remote : remoteList) {
            K key = toUniqueKey.apply(remote);
            if (key == null) {
                continue; // 跳过无法提取唯一键的项
            }
            processedKeys.add(key);
            
            T local = localMap.get(key);
            if (local == null) {
                // 本地不存在，需要新增
                addList.add(remote);
            } else {
                // 本地存在，检查是否需要更新
                T updateItem = getUpdateItem != null ? getUpdateItem.apply(remote, local) : null;
                if (updateItem != null) {
                    updateList.add(updateItem);
                }
            }
        }
        
        // 找出需要删除的（本地有但远程没有的）
        List<T> deleteList = localMap.values().stream()
                .filter(local -> !processedKeys.contains(toUniqueKey.apply(local)))
                .collect(Collectors.toList());
        
        return new Diff<>(addList, deleteList, updateList);
    }
    
    /**
     * 计算两个列表之间的差异（简化版本，仅基于唯一键判断）
     * 
     * @param <T> 对象类型，列表中元素的类型
     * @param <K> 唯一键类型，用于标识对象的唯一性
     * @param remoteList 远程集合，包含最新的数据
     * @param localList 本地集合，包含现有的数据
     * @param toUniqueKey 唯一键提取函数，参数T为对象类型，返回K为唯一键类型
     * @return 差异结果，包含新增、删除列表（无更新）
     * @throws IllegalArgumentException 当参数为null时抛出异常
     */
    public static <T, K> Diff<T> diffByKey(Collection<T> remoteList, Collection<T> localList, 
                                          Function<T, K> toUniqueKey) {
        return diff(remoteList, localList, toUniqueKey, null);
    }
    
    /**
     * 计算两个列表之间的差异（基于equals方法）
     * 
     * @param <T> 对象类型，需要正确实现equals和hashCode方法
     * @param remoteList 远程集合，包含最新的数据
     * @param localList 本地集合，包含现有的数据
     * @return 差异结果，包含新增、删除列表（无更新）
     * @throws IllegalArgumentException 当参数为null时抛出异常
     */
    public static <T> Diff<T> diffByEquals(Collection<T> remoteList, Collection<T> localList) {
        return diff(remoteList, localList, Function.identity(), null);
    }
    
    /**
     * 差异结果类
     * 
     * @param <T> 对象类型，与差分计算中的元素类型保持一致
     */
    public static class Diff<T> {
        private final List<T> addList;
        private final List<T> deleteList;
        private final List<T> updateList;
        
        /**
         * 构造差异结果
         * 
         * @param addList 新增列表，包含需要在本地添加的对象
         * @param deleteList 删除列表，包含需要在本地删除的对象
         * @param updateList 更新列表，包含需要在本地更新的对象
         */
        public Diff(List<T> addList, List<T> deleteList, List<T> updateList) {
            this.addList = addList != null ? new ArrayList<>(addList) : new ArrayList<>();
            this.deleteList = deleteList != null ? new ArrayList<>(deleteList) : new ArrayList<>();
            this.updateList = updateList != null ? new ArrayList<>(updateList) : new ArrayList<>();
        }
        
        /**
         * 获取新增列表
         * @return 新增列表
         */
        public List<T> getAddList() {
            return new ArrayList<>(addList);
        }
        
        /**
         * 获取删除列表
         * @return 删除列表
         */
        public List<T> getDeleteList() {
            return new ArrayList<>(deleteList);
        }
        
        /**
         * 获取更新列表
         * @return 更新列表
         */
        public List<T> getUpdateList() {
            return new ArrayList<>(updateList);
        }
        
        /**
         * 检查是否有变化
         * @return 如果有新增、删除或更新则返回true
         */
        public boolean hasChanges() {
            return !addList.isEmpty() || !deleteList.isEmpty() || !updateList.isEmpty();
        }
        
        /**
         * 获取变化总数
         * @return 变化总数
         */
        public int getTotalChanges() {
            return addList.size() + deleteList.size() + updateList.size();
        }
        
        @Override
        public String toString() {
            return String.format("Diff{add=%d, delete=%d, update=%d}", 
                    addList.size(), deleteList.size(), updateList.size());
        }
    }
}
