package com.ag777.util.web.tree;

import java.util.List;
import java.util.Map;

import com.ag777.util.lang.collection.MapUtils;

/**
 * 树结(map)构工具类
 * 
 * @author ag777
 * @version create on 2018年08月30日,last modify at 2018年08月30日
 */
public class MapTreeUtils {

	/**
	 * 树结构转列表结构
	 * @param treeList
	 * @param childrenKey
	 * @return
	 */
	public static List<Map<String, Object>> tree2List(List<Map<String, Object>> treeList, String childrenKey) {
		return TreeUtils.tree2List(treeList, new TreeUtils.ConvertorT2L<Map<String, Object>, Map<String, Object>>() {

			@Override
			public List<Map<String, Object>> getChildren(Map<String, Object> item, int deep) {
				return MapUtils.get(item, childrenKey);
			}

			@Override
			public Map<String, Object> getItem(long id, long pid, Map<String, Object> item, boolean isLeaf) {
				Map<String, Object> node = MapUtils.newHashMap();
				node.putAll(item);
				node.remove(childrenKey);
				return node;
			}
		});
	}
	
	public static <K>List<Map<String, Object>> list2Tree(List<Map<String, Object>> itemList, K rootId, String idKey, String pidKey, String childrenKey) {
		return TreeUtils.list2Tree(itemList, rootId, new TreeUtils.ConvertorL2T<Map<String, Object>, Map<String, Object>, K>() {

			@Override
			public K getPid(Map<String, Object> item) {
				return MapUtils.get(item, pidKey);
			}

			@Override
			public K getId(Map<String, Object> item) {
				return MapUtils.get(item, idKey);
			}

			@Override
			public Map<String, Object> toNode(Map<String, Object> item, boolean isLeaf) {
				Map<String, Object> node = MapUtils.newHashMap();
				node.putAll(item);
				node.remove(idKey);
				node.remove(pidKey);
				return node;
			}

			@Override
			public Map<String, Object> setChildren(List<Map<String, Object>> children, Map<String, Object> node) {
				node.put(childrenKey, children);
				return node;
			}
		});
	}
}
