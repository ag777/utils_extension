package com.ag777.util.file.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ag777.util.lang.collection.MapUtils;

public class CssBuilder {
	private LinkedHashMap<String, Map<String, String>> cssMap;
	private Map<String, String> curMap;
	
	public CssBuilder() {
		cssMap = MapUtils.newLinkedHashMap(); 
	}
	
	public CssBuilder cssQuery(String cssQuery) {
		curMap = MapUtils.newLinkedHashMap();
		cssMap.put(cssQuery, curMap);
		return this;
	}
	
	public CssBuilder color(String color) {
		return style("color", color);
	}
	public CssBuilder backGroundColor(String color) {
		return style("background-color", color);
	}
	
	public CssBuilder fontSize(int size) {
		return style("font-size", size+"px");
	}
	
	
	public CssBuilder paddingTop(int px) {
		return style("padding-top", px+"px");
	}
	public CssBuilder paddingBottom(int px) {
		return style("padding-bottom", px+"px");
	}
	public CssBuilder paddingLeft(int px) {
		return style("padding-left", px+"px");
	}
	public CssBuilder paddingRight(int px) {
		return style("padding-right", px+"px");
	}
	
	public CssBuilder marginTop(int px) {
		return style("margin-top", px+"px");
	}
	public CssBuilder marginBottom(int px) {
		return style("margin-bottom", px+"px");
	}
	public CssBuilder marginLeft(int px) {
		return  style("margin-left", px+"px");
	}
	public CssBuilder marginRight(int px) {
		return style("margin-right", px+"px");
	}
	
	
	public CssBuilder style(String key, String value) {
		curMap.put(key, value);
		return this;
	}
}
