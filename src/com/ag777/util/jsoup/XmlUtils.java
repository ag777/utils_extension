package com.ag777.util.jsoup;

import java.io.IOException;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.SystemUtils;
import com.ag777.util.lang.collection.CollectionAndMapUtils;

public class XmlUtils {
	
	public static Document load(String filePath) throws IOException {
		String content = FileUtils.readText(filePath, SystemUtils.lineSeparator());
		return Jsoup.parse(content);
	}
	
	
	public static Map<String,Object> getMap(String filePath, String tagName, String attr) throws IOException {
		Document doc = load(filePath);
		Map<String,Object> map = CollectionAndMapUtils.newHashMap();
		
		Elements elements = doc.getElementsByTag(tagName);
		for (Element element : elements) {
			if(element.hasAttr(attr)) {
				String key = element.attr(attr);
				String value = element.text();
				map.put(key, value);
			}
		}
		return map;
	}
	
}
