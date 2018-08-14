package com.ag777.util.web.tree.model;

import java.util.Map;

/**
 * jquery插件zTree每一项对应的java类,用于web目录树导出
 * 
 * @author ag777
 * @version create on 2017年07月26日,last modify at 2018年08月10日
 */
public class ZTreeItem {

	private long id;			//节点编号
	private long pId;		//父节点编号
	private String name;	//节点名称
	private String icon;	//图标路径,如../../../css/zTreeStyle/img/diy/3.png
	private Boolean isParent;	//是否是父节点
	private Boolean open;	//是否绽开
	
	private Map<String, Object> extraData;	//预留额外数据的绑定
	
	
	public long getId() {
		return id;
	}
	public ZTreeItem setId(long id) {
		this.id = id;
		return this;
	}
	public long getpId() {
		return pId;
	}
	public ZTreeItem setpId(long pId) {
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
	
	public Map<String, Object> getExtraData() {
		return extraData;
	}
	public ZTreeItem setExtraData(Map<String, Object> extraData) {
		this.extraData = extraData;
		return this;
	}
	
}
