package com.ag777.util.web.tree;

import java.util.List;
import java.util.Map;

import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;

/**
 * 树结构工具类
 * 
 * @author ag777
 * @version create on 2018年08月09日,last modify at 2018年08月10日
 */
public class TreeUtils {
	
	/**
	 * 
	 * @param treeList
	 * @param convertor
	 * @return
	 */
	public static <T, S>List<T> tree2List(List<S> treeList, Convertor<T,S> convertor) {
		return new TreeUtils().tree2List2(treeList, convertor);
	}
	
	/**
	 * 将列表转换为树
	 * @param itemList 列表
	 * @param rootId 根节点的父编号
	 * @param convertor 转换工具类
	 * @return
	 */
	public static <T, S>List<T> list2Tree(List<S> itemList, Object rootId, Convertor1<T,S> convertor) {
		/*先将列表转化为{pid:List<列表项>}map, 这里不能直接转化为树节点，因为会丢失id和pid信息*/
		Map<Object, List<S>> pidItemMap = MapUtils.newHashMap();
		for (S item : itemList) {
			Object pid = convertor.getPid(item);
			if(!pidItemMap.containsKey(pid)) {
				pidItemMap.put(pid, ListUtils.newArrayList());
			}
			pidItemMap.get(pid).add(item);	
		}
		
		List<T> treeList = ListUtils.newArrayList();
		List<S> itemList1 = MapUtils.get(pidItemMap, rootId);
		if(itemList1 == null) {
			return treeList;
		}
		for (S item : itemList1) {
			T tree = convertor.toNode(item);
			fillTreeNode(tree, convertor.getId(item), pidItemMap, convertor);
			treeList.add(tree);
		}
		return treeList;
	}
	
	/**
	 * [递归]找出该节点下的子节点，转换并添加至该节点下
	 * @param treeNode 当前树节点
	 * @param id 当前树节点所对应id
	 * @param pidItemMap 父节点及其对应的列表(源)
	 * @param convertor 转换工具
	 */
	private static <T, S>void fillTreeNode(T treeNode, Object id, Map<Object, List<S>> pidItemMap, Convertor1<T,S> convertor) {
		List<S> itemChildren = MapUtils.get(pidItemMap, id);
		if(ListUtils.isEmpty(itemChildren)) {	//没有子节点了
			return;
		}
		List<T> children = ListUtils.newArrayList();	//子节点
		for (S item : itemChildren) {
			T node = convertor.toNode(item);
			children.add(node);
			fillTreeNode(node, convertor.getId(item), pidItemMap, convertor);
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
	public <T, S>List<T> tree2List2(List<S> treeList, Convertor<T,S> convertor) {
		List<T> list = ListUtils.newArrayList();
		id = 0;
		long pid=0;
		addList(treeList, list, pid, convertor);
		return list;
	}
	
	/**
	 * 
	 * @param treeList 需要转化的树形结构列表
	 * @param itemList 转化为目标列表
	 * @param pid 该层的父id
	 * @param convertor 转化辅助工具
	 * @return
	 */
	public <T, S>void addList(List<S> treeList, List<T> itemList, long pid, Convertor<T,S> convertor) {
		for (S item : treeList) {
			List<S> children = convertor.getChildren(item);
			boolean isLeaf = ListUtils.isEmpty(children);	//没有子节点被认为是叶节点
			id++;
			add(itemList, convertor.getItem(id, pid, item, isLeaf));
			if(!isLeaf) {
				addList(children, itemList, id, convertor);
			}
		}
	}
	
	private <T>List<T> add(List<T> list, T item) {
		list.add(item);
		return list;
	}
	
	/**
	 * 转换中介类
	 * @author wanggz
	 *
	 * @param <T> 目标(也就是最终要返回的结果项)
	 * @param <S> 源
	 */
	public static interface Convertor<T, S>  {
		/**
		 * 转换为目标列表中的项
		 * @param id
		 * @param pid
		 * @param content
		 * @param isleaf 是否为叶节点(没有子节点了)
		 * @return
		 */
		public T getItem(long id, long pid, S item, boolean isLeaf);
		
		public List<S> getChildren(S item);
	}
	
	
	public static interface Convertor1<T, S>  {
		/**
		 * 从(源)列表项中取出主键,注意不能为null
		 * @param item
		 * @return
		 */
		public Object getId(S item);
		
		/**
		 * 从(源)列表项中取出关联父节点的键,注意不能为null
		 * @param item
		 * @return
		 */
		public Object getPid(S item);
		
		/**
		 * 转化为目标树中的节点
		 * @param item
		 * @return
		 */
		public T toNode(S item);
		
		/**
		 * 为当前节点增加子节点
		 * @param children
		 * @param node
		 * @return
		 */
		public T setChildren(List<T> children, T node);
	}
}
