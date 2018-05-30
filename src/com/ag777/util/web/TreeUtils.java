package com.ag777.util.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ag777.util.lang.collection.MapUtils;

/**
 * 树结构工具类
 * 
 * @author ag777
 * @version create on 2017年06月20日,last modify at 2017年06月20日
 */
public class TreeUtils {
	
	private static final String CHILDREN = "children";
	
	/**
	 * 列表转树
	 * @param idKey
	 * @param pidKey
	 * @param childrenKey
	 * @param openKey
	 * @param folderKey
	 * @param rootNode
	 * @param allList
	 * List<Map<String,Object>> tree = new ArrayList<Map<String,Object>>();
		
		Map<String,Object> rootNode = new HashMap<String,Object>();
		rootNode.put("resource_id", 0l);
		rootNode.put("resource_pid",-1l);
		rootNode.put("resource_name", "ROOT");
		rootNode.put("expand",true);
		rootNode.put("leaf",true);
		tree.add(rootNode);

		tree.addAll(resources);
		
		TreeUtil.listToTree("resource_id", "resource_pid", "children", rootNode, tree);
		tree.clear();
		tree.add(rootNode);
	 */
	public static void listToTree(String idKey,String pidKey,String childrenKey,String openKey,String folderKey, Map<String,Object> rootNode,List<Map<String,Object>> allList){
		allList.remove(rootNode);// 删除掉本身。
		List<Map<String,Object>> tempList = new ArrayList<Map<String,Object>>();// 除去已被用过栏目的集合
		tempList.addAll(allList);
		List<Map<String,Object>> children = new ArrayList<Map<String,Object>>();// 本级子栏目
		
		for (Map<String,Object> node : allList) {
			if (rootNode.get(idKey)!=null && node.get(pidKey) !=null && rootNode.get(idKey).toString().equals(node.get(pidKey).toString())) {
				children.add(node);
				tempList.remove(node);
			}
		}
		rootNode.put(folderKey,false);
		if (children.size() == 0) {
			return;
		} else {
			rootNode.put(childrenKey, children);
			rootNode.put(folderKey,true);
			rootNode.put(openKey,true);
			for (Map<String,Object> temp : children) {
				listToTree(idKey,pidKey,childrenKey,openKey,folderKey,temp, tempList);
			}
		}
	}
	
	/**
	 * 折叠树,将某个节点以下的树节点的targetValue折叠到该节点的targetKey属性下
	 * 如果单纯地想删除子节点，则targetValue和targetKey直接传null
	 * @param root 整棵树
	 * @param targetKey	整合后节点的key
	 * @param targetValue 需要被整合的子节点的key
	 * @param filter 筛选器
	 */
	public static void convertToShortTree(List<Map<String, Object>> root, String targetKey, String targetValue, Filter filter) {
		List<Map<String, Object>> list = MapUtils.get(root.get(0), CHILDREN);
		convertToShortTreeWithoutRoot(list, targetKey, targetValue, filter);
	}
	
	
	/*---内部工具方法---*/
	private static List<String> convertToShortTreeWithoutRoot(List<Map<String, Object>> list, String targetKey, String targetValue, Filter filter) {
		List<String> ids = new ArrayList<String>();
		if(list != null) {	//一级菜单
			for(int i=list.size()-1;i>=0;i--) {
				Map<String, Object> item = list.get(i);
				
				if(!filter.shouldFold(item)) {	//不需要被折叠
					List<Map<String, Object>> children = MapUtils.get(item, CHILDREN);
					List<String> childrenList = convertToShortTreeWithoutRoot(children, targetValue, targetValue, filter);
					if(targetKey != null) {
						item.put(targetKey, childrenList);
					}
					
				} else {	//除了菜单其余均认为是按钮
					if(targetValue != null) {
						String id = MapUtils.get(item, targetValue);
						if(id != null) {
							ids.add(id);
						}
					}
					
					list.remove(item);
				}
				
			}
			
		}
		return ids;
	}
	
	/*----辅助类----*/
	public interface Filter {
		boolean shouldFold(Map<String, Object> item);
		
	}
}
