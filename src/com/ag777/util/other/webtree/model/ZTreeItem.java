package com.ag777.util.other.webtree.model;

/**
 * jquery插件zTree每一项对应的java类
 * 
 * @author ag777
 * @version create on 2017年07月26日,last modify at 2018年07月27日
 */
public class ZTreeItem {

	private int id;			//节点编号
	private int pId;		//父节点编号
	private String name;	//节点名称
	private String icon;	//图标路径,如../../../css/zTreeStyle/img/diy/3.png
	private Boolean isParent;	//是否是父节点
	private Boolean open;	//是否绽开
	
	public int getId() {
		return id;
	}
	public ZTreeItem setId(int id) {
		this.id = id;
		return this;
	}
	public int getpId() {
		return pId;
	}
	public ZTreeItem setpId(int pId) {
		this.pId = pId;
		return this;
	}
	public String getName() {
		return name;
	}
	public ZTreeItem setName(String name) {
		this.name = name;
		return this;
	}
	public String getIcon() {
		return icon;
	}
	public ZTreeItem setIcon(String icon) {
		this.icon = icon;
		return this;
	}
	public Boolean getIsParent() {
		return isParent;
	}
	public ZTreeItem setIsParent(Boolean isParent) {
		this.isParent = isParent;
		return this;
	}
	public Boolean getOpen() {
		return open;
	}
	public ZTreeItem setOpen(Boolean open) {
		this.open = open;
		return this;
	}
	
}
