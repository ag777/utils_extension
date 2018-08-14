package com.ag777.util.web.tree.model;

import java.util.List;
import java.util.Map;

/**
 * jquery插件zTree每一项对应的java类,用于web目录树导出
 * 
 * @author ag777
 * @version create on 2017年07月26日,last modify at 2018年08月10日
 */
public class ZTree {

	private String name;	//节点名称
	private String icon;	//图标路径,如../../../css/zTreeStyle/img/diy/3.png
	private Boolean isParent;	//是否是父节点
	private Boolean open;	//是否绽开
	
	private Map<String, Object> extraData;	//预留额外数据的绑定
	
	private List<ZTree> children;
	
	public String getName() {
		return name;
	}
	public ZTree setName(String name) {
		this.name = name;
		return this;
	}
	public String getIcon() {
		return icon;
	}
	public ZTree setIcon(String icon) {
		this.icon = icon;
		return this;
	}
	public Boolean getIsParent() {
		return isParent;
	}
	public ZTree setIsParent(Boolean isParent) {
		this.isParent = isParent;
		return this;
	}
	public Boolean getOpen() {
		return open;
	}
	public ZTree setOpen(Boolean open) {
		this.open = open;
		return this;
	}
	
	
	
	public Map<String, Object> getExtraData() {
		return extraData;
	}
	public ZTree setExtraData(Map<String, Object> extraData) {
		this.extraData = extraData;
		return this;
	}
	public List<ZTree> getChildren() {
		return children;
	}
	public ZTree setChildren(List<ZTree> children) {
		this.children = children;
		return this;
	}
	
}
