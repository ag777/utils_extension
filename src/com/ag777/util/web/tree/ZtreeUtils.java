package com.ag777.util.web.tree;

import java.util.List;
import java.util.Map;

import com.ag777.util.lang.Console;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.web.tree.model.ZTree;
import com.ag777.util.web.tree.model.ZTreeItem;


/**
 * 树结构(ztree)工具类
 * 
 * @author ag777
 * @version create on 2018年08月09日,last modify at 2018年08月29日
 */
public class ZtreeUtils {

	private ZtreeUtils() {}
	
	/**
	 * 树转项列表(ztree->ztree)
	 * @param treeList
	 * @return
	 */
	public static List<ZTreeItem> tree2List(List<ZTree> treeList) {
		
		return TreeUtils.tree2List(treeList, new TreeUtils.ConvertorT2L<ZTreeItem, ZTree>() {

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
	
	public static <S>List<ZTree> list2Tree(List<S> itemList, long rootId, Converter<S> converter) {
		return TreeUtils.list2Tree(itemList, rootId, 
				new TreeUtils.ConvertorL2T<ZTree, S>() {

					@Override
					public Object getId(S item) {
						return converter.id(item);
					}

					@Override
					public Object getPid(S item) {
						return converter.pid(item);
					}

					@Override
					public ZTree toNode(S item, boolean isLeaf) {
						return new ZTree()
								.setName(converter.name(item))
								.setOpen(converter.open(item, isLeaf))
								.setIcon(converter.icon(item, isLeaf))
								.setIsParent(converter.isParent(item, isLeaf))
								.setExtraData(converter.extraData(item, isLeaf));
					}

					@Override
					public ZTree setChildren(List<ZTree> children, ZTree node) {
						return node.setChildren(children);
					}
				
				}
		);
	}
	
	/**
	 * 项转树列表(ztree->ztree)
	 * @param itemList
	 * @param rootId
	 * @return
	 */
	public static List<ZTree> list2Tree(List<ZTreeItem> itemList, long rootId) {
		return list2Tree(itemList, rootId, new Converter<ZTreeItem>() {

			@Override
			public String name(ZTreeItem item) {
				return item.getName();
			}

			@Override
			public long id(ZTreeItem item) {
				return item.getId();
			}

			@Override
			public long pid(ZTreeItem item) {
				return item.getpId();
			}

			@Override
			public Boolean open(ZTreeItem item, boolean isLeaf) {
				return item.getOpen();
			}

			@Override
			public Boolean isParent(ZTreeItem item, boolean isLeaf) {
				return item.getIsParent();
			}

			@Override
			public String icon(ZTreeItem item, boolean isLeaf) {
				return item.getIcon();
			}

			@Override
			public Map<String, Object> extraData(ZTreeItem item, boolean isLeaf) {
				return item.getExtraData();
			}
		});
	}
	
	public static interface Converter<S> {
		public String name(S item);
		public long id(S item);
		public long pid(S item);
		public Boolean open(S item, boolean isLeaf);
		public Boolean isParent(S item, boolean isLeaf);
		public String icon(S item, boolean isLeaf);
		public Map<String, Object> extraData(S item, boolean isLeaf);
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
