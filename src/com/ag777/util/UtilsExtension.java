package com.ag777.util;

import java.util.HashMap;
import java.util.Map;

import com.ag777.util.Utils;
import com.ag777.util.file.PropertyUtils;
import com.ag777.util.jsoup.JsoupUtils;

public class UtilsExtension{
	
	public static int jsoupTimeOut() {
		return JsoupUtils.defaultTimeOut();
	}
	/**
	* 定制jsoup连接默认的超时时间
	* @param timeOut
	* @return 
	*/
	public static void jsoupTimeOut(int timeOut) {
		JsoupUtils.defaultTimeOut(timeOut);
	}
	
	public static String info() {
		Map<String, Object> infoMap = infoMap();
		if(infoMap != null) {
			return Utils.jsonUtils().toJson(infoMap);
		}
		return null;
	}
	
	public static Map<String, Object> infoMap() {
		Map<String, Object> infoMap = Utils.infoMap();
		PropertyUtils pu = new PropertyUtils();
		try {
			
			pu.load(UtilsExtension.class.getResourceAsStream("/config/config.properties"));
			if(infoMap == null) {
				 infoMap = new HashMap<String, Object>();
			}
			infoMap.put("extension_last_release_date", pu.get("last_release_date"));
			return infoMap;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
}
