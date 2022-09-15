package com.ag777.util.db;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 适用业务: 任意数量的源数据,需要根据数据的id批量到数据库里进行查询相关信息,并将结果和源数据对应上的情况
 * @author ag777 <837915770@vip.qq.com>
 * @Description 批量查询并绑定数据工具类
 * @Date 2022/9/15 9:02
 */
public class BatchQueryUtils {

    public static void main(String[] args) {
        bind(ListUtils.of(1, 2, 3, 4, 5), i->i, 2, (ids->{
            Console.prettyLog(ids);
            return ListUtils.of(MapUtils.of(
                    "id", 2,
                    "v", "a"
            ), MapUtils.of(
                    "id", 3,
                    "v", "a"
            ), MapUtils.of(
                    "id", 4,
                    "v", "a"
            ), MapUtils.of(
                    "id", 4,
                    "v", "a"
            ));
        }), map-> {
            // 转义不能去，去了报错: 无法推断类型变量 K,V
            return MapUtils.getInt((Map<String, Object>)map, "id");
        }, (t1, t2, id)->{
            System.out.println("匹配: ["+id+"]"+t1+", "+ GsonUtils.get().toJson(t2));
        });
    }

    /**
     * 查询数据并对结果进行绑定
     * @param list 源数据
     * @param getId1 从源数据中获取id的方法
     * @param batchSize 批量执行的个数
     * @param queryList 根据id列表进行查询目标的方法
     * @param getId2 从目标数据中获取前id的方法
     * @param onMatch 目标数据id和源数据id相同时，触发该方法; 参数分别是<源数据,目标数据,id>
     * @param <T1> 源数据的类型
     * @param <T2> 目标数据的类型
     * @param <K> id的类型
     */
    public static <T1, T2, K>void bind(List<T1> list, Function<T1, K> getId1, int batchSize, Function<Set<K>, List<T2>> queryList, Function<T2, K> getId2, TriConsumer<T1, T2, K> onMatch) {
        if (ListUtils.isEmpty(list)) {
            return;
        }
        int size = Math.min(list.size(), batchSize);
        // 临时存储对应关系，在查询完数据库以后被清空
        Map<K, T1> idMap = new HashMap<>(size);
        for (T1 t1 : list) {
            K id = getId1.apply(t1);
            idMap.put(id, t1);

            if (idMap.keySet().size() == batchSize) {
                findAndMatch(idMap, queryList, getId2, onMatch);
                idMap.clear();
            }
        }

        if (!idMap.isEmpty()) {
            findAndMatch(idMap, queryList, getId2, onMatch);
            idMap.clear();
        }

    }

    /**
     * 查询并绑定数
     * @param idMap id和源数据对应map
     * @param queryList 查询目标数据的方法
     * @param getId2 从目标数据中获取前id的方法
     * @param onMatch 目标数据id和源数据id相同时，触发该方法; 参数分别是<源数据,目标数据,id>
     * @param <T1> 源数据的类型
     * @param <T2> 目标数据的类型
     * @param <K> id的类型
     */
    private static <T1, T2, K>void findAndMatch(Map<K, T1> idMap, Function<Set<K>, List<T2>> queryList, Function<T2, K> getId2, TriConsumer<T1, T2, K> onMatch) {
        List<T2> detailList = queryList.apply(idMap.keySet());
        find(idMap, detailList, getId2, onMatch);
    }

    /**
     * 遍历绑定源数据和目标数据
     * @param idMap id和源数据对应map
     * @param list 目标数据
     * @param getId2 从目标数据中获取前id的方法
     * @param onMatch 目标数据id和源数据id相同时，触发该方法; 参数分别是<源数据,目标数据,id>
     * @param <T1> 源数据的类型
     * @param <T2> 目标数据的类型
     * @param <K> id的类型
     */
    private static <T1, T2, K>void find(Map<K, T1> idMap, List<T2> list, Function<T2, K> getId2, TriConsumer<T1, T2, K> onMatch) {
        // 遍历目标数据
        for (T2 t2 : list) {
            // 获取目标数据的id
            K id2 = getId2.apply(t2);
            if (id2 == null) {  // 跳过id为空的项
                continue;
            }
            // 查找源数据的id
            T1 t1 = idMap.get(id2);
            if (t1 != null) {
                // 找到对应源数据，触发绑定操作
                onMatch.apply(t1, t2, id2);
            }
        }
    }

    @FunctionalInterface
    public interface TriConsumer<S, T, U> {
        void apply(S var1, T var2, U var3);
    }
}
