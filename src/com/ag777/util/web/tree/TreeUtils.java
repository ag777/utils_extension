package com.ag777.util.web.tree;

import java.util.List;
import java.util.Map;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;

/**
 * 树结构工具类
 * 
 * @author ag777
 * @version create on 2018年08月09日,last modify at 2018年08月29日
 */
public class TreeUtils {
	
	/**
	 * 遍历树结构
	 * @param node
	 * @param viewer
	 */
	public static <S>void treeForeach(S node, ViewerT<S> viewer) {
		treeForeach(node, viewer, 0);
	}
	
	private static <S>void treeForeach(S node, ViewerT<S> viewer, int deep) {
		List<S> children = viewer.children(node, deep);
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
	public static<S>void listForeach(List<S> itemList, Object rootId, ViewerL<S> viewer) {
		/*先将列表转化为{pid:List<列表项>}map*/
		Map<Object, List<S>> pidItemMap = getPidItemMap(itemList, viewer);
		List<S> children= MapUtils.get(pidItemMap, rootId);
		for (S child : children) {
			listForeach(child, pidItemMap, 0, viewer);
		}
	}
	
	private static<S>void listForeach(S node, Map<Object, List<S>> pidItemMap, int deep, ViewerL<S> viewer) {
		Object id = viewer.getId(node);
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
	 * @param rootId 根节点的父编号
	 * @param convertor 转换工具类
	 * @return
	 */
	public static <T, S>List<T> list2Tree(List<S> itemList, Object rootId, ConvertorL2T<T,S> convertor) {
		/*先将列表转化为{pid:List<列表项>}map, 这里不能直接转化为树节点，因为会丢失id和pid信息*/
		Map<Object, List<S>> pidItemMap = getPidItemMap(itemList, convertor);
		
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
	private static <T, S>void fillTreeNode(T treeNode, List<S> itemChildren, Map<Object, List<S>> pidItemMap, ConvertorL2T<T,S> convertor) {
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
	public <T, S>void addList(List<S> treeList, List<T> itemList, long pid, ConvertorT2L<T,S> convertor) {
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
	 * 列表型树建立pid:节点列表的映射关系
	 * @param itemList
	 * @param getter
	 * @return
	 */
	public static <S>Map<Object, List<S>> getPidItemMap(List<S> itemList, PidGetter<S> getter) {
		Map<Object, List<S>> pidItemMap = MapUtils.newHashMap();
		for (S item : itemList) {
			Object pid = getter.getPid(item);
			if(!pidItemMap.containsKey(pid)) {
				pidItemMap.put(pid, ListUtils.newArrayList());
			}
			pidItemMap.get(pid).add(item);	
		}
		return pidItemMap;
	}
	
	/**
	 * 获取pid接口
	 * @author wanggz
	 *
	 * @param <S>
	 */
	public static interface PidGetter<S> {
		/**
		 * 从(源)列表项中取出关联父节点的键,注意不能为null
		 * @param item
		 * @return
		 */
		public Object getPid(S item);
	}
	
	/**
	 * 转换中介接口(Tree->List)
	 * @author wanggz
	 *
	 * @param <T> 目标(也就是最终要返回的结果项)
	 * @param <S> 源
	 */
	public static interface ConvertorT2L<T, S>  {
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
	
	
	/**
	 * 转换中介接口(List->Tree)
	 * @author wanggz
	 *
	 * @param <T>
	 * @param <S>
	 */
	public static interface ConvertorL2T<T, S> extends PidGetter<S>{
		/**
		 * 从(源)列表项中取出主键,注意不能为null
		 * @param item
		 * @return
		 */
		public Object getId(S item);
		
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
	 * @author wanggz
	 *
	 * @param <S>
	 */
	public static interface ViewerT<S> {
		public List<S> children(S node, int deep);
		public void view(S node, boolean isLeaf, int deep);
	}
	
	/**
	 * 列表结构遍历接口
	 * @author wanggz
	 *
	 * @param <S>
	 */
	public static interface ViewerL<S> extends PidGetter<S> {
		public Object getId(S item);
//		public Object pid(S item);
		public Object view(S node, boolean isLeaf, int deep);
	}
}
