package com.ag777.util.file.xml;

import java.util.List;
import java.util.Map;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.model.Pair;

/**
 * html构造工具类
 * <p>
 * 	基于XmlBuilder
 * 需要jar包:
 * 	<ul>
 * 	<li>dom4j-1.6.1.jar</li>
 * 	</ul> 
 * </p>
 * 
 * @author ag777
 * @version create on 2017年12月14日,last modify at 2017年12月15日
 */
public class HtmlBuilder {
	
	public static boolean save(XmlBuilder[] bodyElements, String filePath) {
		XmlBuilder builder = new XmlBuilder("html")
				.child(new XmlBuilder("body")
						.children(bodyElements));
		return builder.saveAsHtml(filePath);
	}
	
	public static XmlBuilder div() {
		return new XmlBuilder("div");
	}
	public static XmlBuilder div(String id, String[] classes, String title, String text, Map<String, Object> attrMap) {
		return any("div", id, classes, title, text, attrMap);
	}
	
	
	public static XmlBuilder a(String href, String text) {
		return new XmlBuilder("a")
				.attr("href", href)
				.val(text);
	}
	public static XmlBuilder a(String href, String text, String id, String[] classes, String title, Map<String, Object> attrMap) {
		MapUtils.put(attrMap, "href", href);
		return any("a", id, classes, title, text, attrMap);
	}
	
	public static XmlBuilder img(String src) {
		return new XmlBuilder("img")
				.attr("src", src);
	}
	public static XmlBuilder img(String src, String id, String[] classes, String title, String text, Map<String, Object> attrMap) {
		MapUtils.put(attrMap, "src", src);
		return any("img", id, classes, title, text, attrMap);
	}
	
	public static XmlBuilder span(String text) {
		return new XmlBuilder(text)
				.val(text);
	}
	public static XmlBuilder span(String text, String id, String[] classes, String title, Map<String, Object> attrMap) {
		return any("span", id, classes, title, text, attrMap);
	}
	
	public static XmlBuilder ul() {
		return new XmlBuilder("ul");
	}
	public static XmlBuilder ul(XmlBuilder[] lis) {
		return ul().children(lis);
	}
	public static XmlBuilder ul(XmlBuilder[] lis, String id, String[] classes, String title, String text, Map<String, Object> attrMap) {
		return any("ul", id, classes, title, text, attrMap).children(lis);
	}
	
	public static XmlBuilder li(String id, String[] classes, String title, String text, Map<String, Object> attrMap) {
		return any("li", id, classes, title, text, attrMap);
	}
	
	public static XmlBuilder table() {
		return new XmlBuilder("table");
	}
	public static XmlBuilder table(XmlBuilder[] trs) {
		return table().children(trs);
	}
	public static XmlBuilder table(XmlBuilder[] trs, String id, String[] classes, String title, String text, Map<String, Object> attrMap) {
		return any("table", id, classes, title, text, attrMap).children(trs);
	}
	
	public static XmlBuilder tr() {
		return new XmlBuilder("tr");
	}
	public static XmlBuilder tr(XmlBuilder[] trs) {
		return tr().children(trs);
	}
	public static XmlBuilder tr(XmlBuilder[] trs, String id, String[] classes, String title, String text, Map<String, Object> attrMap) {
		return any("tr", id, classes, title, text, attrMap).children(trs);
	}
	
	public static XmlBuilder td(String text) {
		return new XmlBuilder("td").val(text);
	}
	public static XmlBuilder td(String id, String[] classes, String title, String text, Map<String, Object> attrMap) {
		return any("td", id, classes, title, text, attrMap);
	}
	
	/**
	 * 简单列表
	 * @param tableContent
	 * @return
	 */
	public static XmlBuilder table(List<List<String>> tableContent) {
		XmlBuilder table = table();
		for (List<String> list : tableContent) {
			XmlBuilder tr = tr();
			for (String text : list) {
				XmlBuilder td = td(text);
				tr.child(td);
			}
			table.child(tr);
		}
		return table;
	}
	
	public static XmlBuilder table(List<Map<String, Object>> dataList, Pair<String, String>[] keyTitlePair) {
		XmlBuilder table = table();
		if(!ListUtils.isEmpty(keyTitlePair)) {
			XmlBuilder tr = tr();
			for (Pair<String, String> pair : keyTitlePair) {
				if(pair.second == null) {
					pair.second = pair.first;
				}
				tr.child(td(pair.second));
			}
			table.child(tr);
		}
		if(!ListUtils.isEmpty(dataList)) {
			for (Map<String, Object> map : dataList) {
				XmlBuilder tr = tr();
				for (Pair<String, String> pair : keyTitlePair) {
					tr.child(td(MapUtils.getString(map, pair.first, "")));
				}
				table.child(tr);
			}
		}
		
		return table;
	}
	
	/**
	 * 0属性项
	 * @param tag
	 * @param id
	 * @param classes
	 * @param text
	 * @return
	 */
	public static XmlBuilder any(String tag, String id, String[] classes, String title, String text) {
		XmlBuilder builder = new XmlBuilder(tag);
		if(id != null) {
			builder.attr("id", id);
		}
		if(!ListUtils.isEmpty(classes)) {
			builder.attr("class", ListUtils.toString(classes, " "));
		}
		
		if(title != null) {
			builder.attr("title", title);
		}
		
		if(text != null) {
			builder.val(text);
		}
		
		return builder;
	}
	
	/**
	 * 1属性项
	 * @param tag
	 * @param id
	 * @param classes
	 * @param text
	 * @param attrKey1
	 * @param attrVal1
	 * @return
	 */
	public static XmlBuilder any(String tag, String id, String[] classes, String title, String text, String attrKey1, String attrVal1) {
		XmlBuilder builder = any(tag, id, classes, title, text);
		builder.attr(attrKey1, attrVal1);
		return builder;
	}
	
	/**
	 * 2属性项
	 * @param tag
	 * @param id
	 * @param classes
	 * @param text
	 * @param attrKey1
	 * @param attrVal1
	 * @param attrKey2
	 * @param attrVal2
	 * @return
	 */
	public static XmlBuilder any(String tag, String id, String[] classes, String title, String text, String attrKey1, String attrVal1, String attrKey2, String attrVal2) {
		XmlBuilder builder = any(tag, id, classes, title, text, attrKey1, attrVal1);
		builder.attr(attrKey2, attrVal2);
		return builder;
	}
	
	/**
	 * 3属性项
	 * @param tag
	 * @param id
	 * @param classes
	 * @param text
	 * @param attrKey1
	 * @param attrVal1
	 * @param attrKey2
	 * @param attrVal2
	 * @param attrKey3
	 * @param attrVal3
	 * @return
	 */
	public static XmlBuilder any(String tag, String id, String[] classes, String title, String text, String attrKey1, String attrVal1, String attrKey2, String attrVal2, String attrKey3, String attrVal3) {
		XmlBuilder builder = any(tag, id, classes, title, text, attrKey1, attrVal1, attrKey2, attrVal2);
		builder.attr(attrKey3, attrVal3);
		return builder;
	}
	
	/**
	 * 4属性项
	 * @param tag
	 * @param id
	 * @param classes
	 * @param text
	 * @param attrKey1
	 * @param attrVal1
	 * @param attrKey2
	 * @param attrVal2
	 * @param attrKey3
	 * @param attrVal3
	 * @param attrKey4
	 * @param attrVal4
	 * @return
	 */
	public static XmlBuilder any(String tag, String id, String[] classes, String title, String text, String attrKey1, String attrVal1, String attrKey2, String attrVal2, String attrKey3, String attrVal3, String attrKey4, String attrVal4) {
		XmlBuilder builder = any(tag, id, classes, title, text, attrKey1, attrVal1, attrKey2, attrVal2, attrKey3, attrVal3);
		builder.attr(attrKey4, attrVal4);
		return builder;
	}
	
	/**
	 * 5属性项
	 * @param tag
	 * @param id
	 * @param classes
	 * @param text
	 * @param attrKey1
	 * @param attrVal1
	 * @param attrKey2
	 * @param attrVal2
	 * @param attrKey3
	 * @param attrVal3
	 * @param attrKey4
	 * @param attrVal4
	 * @param attrKey5
	 * @param attrVal5
	 * @return
	 */
	public static XmlBuilder any(String tag, String id, String[] classes, String title, String text, String attrKey1, String attrVal1, String attrKey2, String attrVal2, String attrKey3, String attrVal3, String attrKey4, String attrVal4, String attrKey5, String attrVal5) {
		XmlBuilder builder = any(tag, id, classes, title, text, attrKey1, attrVal1, attrKey2, attrVal2, attrKey3, attrVal3, attrKey4, attrVal4);
		builder.attr(attrKey5, attrVal5);
		return builder;
	}
	
	/**
	 * n属性项
	 * @param tag
	 * @param id
	 * @param classes
	 * @param text
	 * @param attrMap
	 * @return
	 */
	public static XmlBuilder any(String tag, String id, String[] classes, String title, String text, Map<String, Object> attrMap) {
		XmlBuilder builder = any(tag, id, classes, title, text);
		if(!MapUtils.isEmpty(attrMap)) {
			builder.attrMap(attrMap);
		}
		return builder;
	}
	
}
