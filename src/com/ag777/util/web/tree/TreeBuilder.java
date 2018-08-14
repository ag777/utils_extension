package com.ag777.util.web.tree;

import java.util.List;
import java.util.Map;

import com.ag777.util.lang.collection.ListUtils;

/**
 * 树结构构建工具类
 * 
 * @author ag777
 * @version create on 2018年08月09日,last modify at 2018年08月10日
 */
public class TreeBuilder<T> {

	private List<T> treeList;
	
	public List<T> getTreeList() {
		return treeList;
	}

	public TreeBuilder() {
		treeList = ListUtils.newArrayList();
	}
	
	public <S>void add(S[] group, Map<String, Object> extraData, Convertor<T, S> convertor) {
		add(group, 0, extraData, treeList, convertor);
	}
	
	/**
	 * 递归添加节点
	 * @param group
	 * @param index 数组下标
	 * @param extraData
	 * @param nodeList
	 * @param convertor
	 */
	private <S>void add(S[] group, int index, Map<String, Object> extraData, List<T> nodeList, Convertor<T, S> convertor) {
		S item = group[index];
		T node = null;
		for (T node1 : nodeList) {
			if(convertor.equals(item, node1)) {
				node = node1;
			}
		}
		
		index++;
		boolean isLeaf = index >= group.length;
		
		if(node == null) {
			node = convertor.convert(item, index, isLeaf, extraData);
			nodeList.add(node);
		} else {
			convertor.modify(node, item, index, isLeaf, extraData);
		}
		
		
		if(!isLeaf) {	//有子节点
			List<T> children = convertor.getChildren(node);
			if(children == null) {
				children = ListUtils.newArrayList();
				convertor.setChildren(node, children);
			}
			add(group, index, extraData, children, convertor);
		}
	}
	
	public static interface Convertor<T, S> {
		/**
		 * 转换方法
		 * @param item 节点内容(源数组的项)
		 * @param deep 深度最开始为1层
		 * @param isLeaf 是否为叶节点(没有子节点)
		 * @param extraData 额外的数据
		 * @return
		 */
		public T convert(S item, int deep, boolean isLeaf, Map<String, Object> extraData);
		
		
		/**
		 * 构造树时再次经过该节点调用的方法
		 * @param node
		 * @param item
		 * @param deep
		 * @param isLeaf
		 * @param extraData
		 */
		public void modify(T node, S item, int deep, boolean isLeaf, Map<String, Object> extraData);
		
		public boolean equals(S item, T node);
		
		/**
		 * 获取当前父节点的所有子节点
		 * @param trreeNode
		 * @return
		 */
		public List<T> getChildren(T treeNode);
		
		/**
		 * 添加一个空的子节点子列表到当前节点下
		 * @param trreeNode
		 * @param children
		 * @return
		 */
		public T setChildren(T treeNode, List<T> children);
	}
	
}
