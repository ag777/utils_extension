package com.ag777.util.web.tree;

import java.util.List;
import java.util.Map;

import com.ag777.util.lang.ObjectUtils;
import com.ag777.util.web.tree.model.ZTree;


/**
 * 树结构(ztree)构建工具类
 * 
 * @author ag777
 * @version create on 2018年08月10日,last modify at 2018年08月10日
 */
public class ZtreeBuilder extends TreeBuilder<ZTree> {
	private Convertor<ZTree, String> convertor;
	public ZtreeBuilder() {
		convertor = new Convertor<ZTree, String>() {

			@Override
			public ZTree convert(String url, int deep, boolean isLeaf, Map<String, Object> extraData) {
				ZTree zTree = new ZTree()
						.setName(url)
						.setExtraData(extraData);
				if(!isLeaf) {
					zTree.setIsParent(true)
						.setOpen(true);
				}
				return zTree;
			}

			@Override
			public void modify(ZTree node, String item, int deep, boolean isLeaf, Map<String, Object> extraData) {
				//不是叶节点且目前不是父节点,则设置当前节点为父节点并展开
				if(!isLeaf && !ObjectUtils.isBooleanTrue(node.getIsParent())){	
					node.setIsParent(true)
						.setOpen(true);
				}
				
			}

			@Override
			public boolean equals(String url, ZTree node) {
				return node.getName().equals(url);
			}

			@Override
			public List<ZTree> getChildren(ZTree treeNode) {
				return treeNode.getChildren();
			}

			@Override
			public ZTree setChildren(ZTree treeNode, List<ZTree> children) {
				treeNode.setChildren(children);
				return treeNode;
			}
			
		};
	}
	
	
	@Override
	public List<ZTree> getTreeList() {
		return super.getTreeList();
	}
	
	public void add(String[] group, Map<String, Object> extraData) {
		super.add(group, extraData, convertor);
	}
	
}
