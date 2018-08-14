package com.ag777.util.web.tree;

import java.util.List;

import com.ag777.util.lang.Console;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.web.tree.model.ZTree;
import com.ag777.util.web.tree.model.ZTreeItem;


/**
 * 树结构(ztree)工具类
 * 
 * @author ag777
 * @version create on 2018年08月09日,last modify at 2018年08月10日
 */
public class ZtreeUtils {

	private ZtreeUtils() {}
	
	/**
	 * 树转项列表
	 * @param treeList
	 * @return
	 */
	public static List<ZTreeItem> tree2List(List<ZTree> treeList) {
		
		return TreeUtils.tree2List(treeList, new TreeUtils.Convertor<ZTreeItem, ZTree>() {

			@Override
			public ZTreeItem getItem(long id, long pid, ZTree node, boolean isLeaf) {
				return new ZTreeItem()
						.setId(id)
						.setpId(pid)
						.setName(node.getName())
						.setOpen(node.getOpen())
						.setIcon(node.getIcon())
						.setIsParent(node.getIsParent())
						.setExtraData(node.getExtraData());
			}

			@Override
			public List<ZTree> getChildren(ZTree item) {
				return item.getChildren();
			}
		});
		
	}
	
	/**
	 * 项转树列表
	 * @param itemList
	 * @param rootId
	 * @return
	 */
	public static List<ZTree> list2Tree(List<ZTreeItem> itemList, long rootId) {
		return TreeUtils.list2Tree(itemList, rootId, 
				new TreeUtils.Convertor1<ZTree, ZTreeItem>() {

					@Override
					public Object getId(ZTreeItem item) {
						return item.getId();
					}

					@Override
					public Object getPid(ZTreeItem item) {
						return item.getpId();
					}

					@Override
					public ZTree toNode(ZTreeItem item) {
						return new ZTree()
								.setName(item.getName())
								.setName(item.getName())
								.setOpen(item.getOpen())
								.setIcon(item.getIcon())
								.setIsParent(item.getIsParent())
								.setExtraData(item.getExtraData());
					}

					@Override
					public ZTree setChildren(List<ZTree> children, ZTree node) {
						return node.setChildren(children);
					}
				}
			);
	}
	
	public static void main(String[] args) {
		List<ZTree> treeList = ListUtils.of(
				new ZTree()
					.setName("根节点")
					.setChildren(ListUtils.of(
							new ZTree()
								.setName("子节点1")
								.setChildren(ListUtils.of(new ZTree().setName("子节点11"))),
							new ZTree()
								.setName("子节点2").setChildren(ListUtils.of(new ZTree().setName("子节点21")))
							)
					));
		List<ZTreeItem> itemList = tree2List(treeList);
		Console.prettyLog(list2Tree(itemList, 0));
	}
	
}
