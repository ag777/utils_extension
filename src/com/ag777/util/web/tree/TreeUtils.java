package com.ag777.util.web.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;

/**
 * 树结构工具类
 * 
 * @author ag777
 * @version create on 2018年08月09日,last modify at 2018年08月30日
 */
public class TreeUtils {
	
	/**
	 * 拆分树节点
	 * <p>
	 * 树中每个分支都会被归到一个列表里,这个分支是最深的那一级
	 * </p>
	 * @param tree
	 * @param nodeFinder
	 * @return
	 */
	public static <S, K>List<List<K>> splitTree(S tree, NodeFinder<S, K> nodeFinder) {
		List<List<K>> resultList = ListUtils.newArrayList();
		splitTree(tree, ListUtils.newArrayList(), resultList, 0, nodeFinder);
		return resultList;
	}
	
	/**
	 * 拆分树节点(递归)
	 * @param node
	 * @param parentList
	 * @param resultList
	 * @param deep
	 * @param nodeFinder
	 */
	private static <S, K> void splitTree(S node, List<K> parentList, List<List<K>> resultList,int deep, NodeFinder<S, K> nodeFinder) {
		List<K> list = new ArrayList<>(parentList);
		list.add(nodeFinder.getKey(node, deep));
		List<S> children = nodeFinder.getChildren(node, deep);
		if(ListUtils.isEmpty(children)) {	//没有子节点，完成一棵树
			resultList.add(list);
		} else {
			deep++;
			for (S child : children) {
				splitTree(child, list, resultList, deep, nodeFinder);
			}
		}
		
	}
	
	/**
	 * 树级结构是否包含节点
	 * @param tree
	 * @param group
	 * @param nodeFinder
	 * @return
	 */
	public static <S, K> boolean containGroup(S tree, K[] group, NodeFinder<S, K> nodeFinder) {
		K key = nodeFinder.getKey(tree, 0);
		if(key != null && key.equals(group[0])) {
			return containGroup(nodeFinder.getChildren(tree, 0), group, 1, nodeFinder);
		} else {
			return false;
		}
	}
	
	/**
	 * 递归寻找节点
	 * @param nodeList
	 * @param group
	 * @param deep
	 * @param nodeFinder
	 * @return
	 */
	private static <S,K> boolean containGroup(List<S> nodeList, K[] group, int deep, NodeFinder<S, K> nodeFinder) {
		if(ListUtils.isEmpty(nodeList)) {	//需要的节点比当前节点树深
			return false;
		}
		for (S node : nodeList) {
			K key = nodeFinder.getKey(node, deep);
			if(key != null && key.equals(group[deep])) {
				if(deep < group.length) {
					List<S> children = nodeFinder.getChildren(node, deep);
					deep++;
					return containGroup(children, group, deep, nodeFinder);
				} else {	//当前节点树比需要的深
					return false;
				}
				
			}
			//当前节点不匹配，比较横向的下一个节点
		}
		return false;	//没找到对应节点
	}
	
	/**
	 * 遍历树结构
	 * @param tree
	 * @param viewer
	 */
	public static <S>void treeForeach(S tree, ViewerT<S> viewer) {
		treeForeach(tree, viewer, 0);
	}
	
	private static <S>void treeForeach(S node, ViewerT<S> viewer, int deep) {
		List<S> children = viewer.getChildren(node, deep);
		if(ListUtils.isEmpty(children)) {	//有子节点
			viewer.view(node, false, deep);
			for (S child : children) {
				treeForeach(child, viewer, deep+1);
			}
		} else {	//叶节点
			viewer.view(node, true, deep);
		}
		
	}
	
	/**
	 * 遍历列表结构
	 * @param itemList
	 * @param rootId
	 * @param viewer
	 */
	public static<S, K>void listForeach(List<S> itemList, K rootId, ViewerL<S, K> viewer) {
		/*先将列表转化为{pid:List<列表项>}map*/
		Map<K, List<S>> pidItemMap = getPidItemMap(itemList, viewer);
		List<S> children= MapUtils.get(pidItemMap, rootId);
		for (S child : children) {
			listForeach(child, pidItemMap, 0, viewer);
		}
	}
	
	private static<S,K>void listForeach(S node, Map<K, List<S>> pidItemMap, int deep, ViewerL<S, K> viewer) {
		K id = viewer.getId(node);
		boolean isLeaf = !pidItemMap.containsKey(id);
		viewer.view(node, isLeaf, deep);
		if(isLeaf) {
			List<S> children = MapUtils.get(pidItemMap, id);
			for (S child : children) {
				listForeach(child, pidItemMap, deep+1, viewer);
			}
		}
	}
	
	/**
	 * 
	 * @param treeList
	 * @param convertor
	 * @return
	 */
	public static <T, S>List<T> tree2List(List<S> treeList, ConvertorT2L<T,S> convertor) {
		return new TreeUtils().tree2List2(treeList, convertor);
	}
	
	/**
	 * 将列表转换为树
	 * @param itemList 列表
	 * @param rootId 根节点的父编号,注意判断一致用的equals所以如果是long型的0得传0l
	 * @param convertor 转换工具类
	 * @return
	 */
	public static <T, S, K>List<T> list2Tree(List<S> itemList, K rootId, ConvertorL2T<T,S,K> convertor) {
		/*先将列表转化为{pid:List<列表项>}map, 这里不能直接转化为树节点，因为会丢失id和pid信息*/
		Map<K, List<S>> pidItemMap = getPidItemMap(itemList, convertor);
		
		List<T> treeList = ListUtils.newArrayList();
		List<S> itemList1 = MapUtils.get(pidItemMap, rootId);
		if(itemList1 == null) {
			return treeList;
		}
		for (S item : itemList1) {
			List<S> children = MapUtils.get(pidItemMap, convertor.getId(item));
			boolean isLeaf = ListUtils.isEmpty(children);	//叶节点，没有子节点
			T tree = convertor.toNode(item, isLeaf);
			if(!isLeaf) {
				fillTreeNode(tree, children, pidItemMap, convertor);
			}
			treeList.add(tree);
		}
		return treeList;
	}
	
	/**
	 * [递归]找出该节点下的子节点，转换并添加至该节点下
	 * @param treeNode 当前树节点
	 * @param itemChildren 当前项所有子项
	 * @param pidItemMap 父节点及其对应的列表(源)
	 * @param convertor 转换工具
	 */
	private static <T, S, K>void fillTreeNode(T treeNode, List<S> itemChildren, Map<K, List<S>> pidItemMap, ConvertorL2T<T, S, K> convertor) {
		List<T> children = ListUtils.newArrayList();	//子节点
		for (S item : itemChildren) {
			List<S> childList = MapUtils.get(pidItemMap, convertor.getId(item));
			boolean isLeaf = ListUtils.isEmpty(childList);	//叶节点，没有子节点
			T node = convertor.toNode(item, isLeaf);
			children.add(node);
			if(!isLeaf) {
				fillTreeNode(node, childList, pidItemMap, convertor);
			}
			
		}
		convertor.setChildren(children, treeNode);	//将所有子节点添加至该节点下
	}
	
	
	
	private long id;
	
	private TreeUtils() {
		id = 0;
	}
	
	/**
	 * 树转ztree用的itemList
	 * @param treeMap
	 * @return
	 */
	public <T, S>List<T> tree2List2(List<S> treeList, ConvertorT2L<T,S> convertor) {
		List<T> list = ListUtils.newArrayList();
		id = 0;
		long pid=0;
		addList(treeList, list, pid, 0, convertor);
		return list;
	}
	
	/**
	 * 
	 * @param treeList 需要转化的树形结构列表
	 * @param itemList 转化为目标列表
	 * @param pid 该层的父id
	 * @param deep 该层的深度
	 * @param convertor 转化辅助工具
	 * @return
	 */
	public <T, S>void addList(List<S> treeList, List<T> itemList, long pid, int deep, ConvertorT2L<T,S> convertor) {
		for (S item : treeList) {
			List<S> children = convertor.getChildren(item, deep);
			boolean isLeaf = ListUtils.isEmpty(children);	//没有子节点被认为是叶节点
			id++;
			add(itemList, convertor.getItem(id, pid, item, isLeaf));
			if(!isLeaf) {
				addList(children, itemList, id, deep+1, convertor);
			}
		}
	}
	
	private <T>List<T> add(List<T> list, T item) {
		list.add(item);
		return list;
	}
	
	/**
	 * 列表型树建立pid:节点列表的映射关系
	 * @param itemList
	 * @param getter
	 * @return
	 */
	public static <S, K>Map<K, List<S>> getPidItemMap(List<S> itemList, PidGetter<S, K> getter) {
		Map<K, List<S>> pidItemMap = MapUtils.newHashMap();
		for (S item : itemList) {
			K pid = getter.getPid(item);
			if(!pidItemMap.containsKey(pid)) {
				pidItemMap.put(pid, ListUtils.newArrayList());
			}
			pidItemMap.get(pid).add(item);	
		}
		return pidItemMap;
	}
	
	/**
	 * 获取pid接口
	 * @author ag777
	 *
	 * @param <S>
	 */
	public static interface PidGetter<S, K> {
		/**
		 * 从(源)列表项中取出关联父节点的键,注意不能为null
		 * @param item
		 * @return
		 */
		public K getPid(S item);
	}
	
	public static interface ChildrenGetter<S> {
		public List<S> getChildren(S item, int deep);
	}
	
	/**
	 * 转换中介接口(Tree->List)
	 * @author ag777
	 *
	 * @param <T> 目标(也就是最终要返回的结果项)
	 * @param <S> 源
	 */
	public static interface ConvertorT2L<T, S> extends ChildrenGetter<S>  {
		/**
		 * 转换为目标列表中的项
		 * @param id
		 * @param pid
		 * @param content
		 * @param isleaf 是否为叶节点(没有子节点了)
		 * @return
		 */
		public T getItem(long id, long pid, S item, boolean isLeaf);
		
//		public List<S> getChildren(S item, int deep);
	}
	
	
	/**
	 * 转换中介接口(List->Tree)
	 * @author ag777
	 *
	 * @param <T> 目标类型
	 * @param <S> 源类型
	 * @param <K> id和pid的类型
	 */
	public static interface ConvertorL2T<T, S, K> extends PidGetter<S, K>{
		/**
		 * 从(源)列表项中取出主键,注意不能为null
		 * @param item
		 * @return
		 */
		public K getId(S item);
		
//		public Object getPid(S item);
		
		/**
		 * 转化为目标树中的节点
		 * @param item
		 * @param isLeaf 是否为叶节点(没有子节点)
		 * @return
		 */
		public T toNode(S item, boolean isLeaf);
		
		/**
		 * 为当前节点增加子节点
		 * @param children
		 * @param node
		 * @return
		 */
		public T setChildren(List<T> children, T node);
	}

	/**
	 * 树结构遍历接口
	 * @author ag777
	 *
	 * @param <S>
	 */
	public static interface ViewerT<S> extends ChildrenGetter<S> {
//		public List<S> getChildren(S node, int deep);
		public void view(S node, boolean isLeaf, int deep);
	}
	
	/**
	 * 列表结构遍历接口
	 * @author ag777
	 *
	 * @param <S>
	 * @param <K> id和pid类型
	 */
	public static interface ViewerL<S, K> extends PidGetter<S, K> {
		public K getId(S item);
//		public Object pid(S item);
		public void view(S node, boolean isLeaf, int deep);
	}
	
	/**
	 * 节点查找辅助接口
	 * @author ag777
	 *
	 * @param <S> 源树的节点类型
	 * @param <K> 需要对比的key类型
	 */
	public static interface NodeFinder<S, K> extends ChildrenGetter<S> {
		/**
		 * 需要定位的key
		 * @param node
		 * @param deep
		 * @return
		 */
		public K getKey(S node, int deep);
	}
	
}
