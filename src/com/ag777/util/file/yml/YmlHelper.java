package com.ag777.util.file.yml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import com.ag777.util.lang.collection.MapUtils;

/**
 * yml工具类(对snakeyaml的使用进行做简单的参考)
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>snakeyaml-1.26.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2020年08月27日,last modify at 2020年08月27日
 */
public class YmlHelper {

	private Map<String, Object> map;
	
	public YmlHelper() {
		init();
	}
	public YmlHelper(Map<String, Object> map) {
		if(map != null) {
			this.map = map;
		} else {
			init();
		}
		
	}

	public static YmlHelper read(File file) throws FileNotFoundException {
		Map<String, Object> map = YmlUtils.readMap(file);
		return new YmlHelper(map);
	}
	
	private void init() {
		this.map = MapUtils.newHashMap();
	}
	
	public Map<String, Object> getMap() {
		return map;
	}
	
	public Map<String, Object> getFlatMap() {
		return flatMap(null, map, MapUtils.newHashMap());
	}
	
	public void save(Writer writer) {
		YmlUtils.write(map, writer);
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> flatMap(String curKey, Map<String, Object> map, Map<String, Object> resultMap) {
		Iterator<String> itor = map.keySet().iterator();
		while(itor.hasNext()) {
			String key = itor.next();
			Object value = map.get(key);
			//获取完value之后，拼接key:a.b.c
			if(curKey != null) {
				key = curKey+"."+key;
			}
			if(value instanceof Map) {
				flatMap(key, (Map<String, Object>) value, resultMap);
			} else {
				resultMap.put(key, value);
			}
		}
		return resultMap;
	}
	
}
