package com.ag777.util.jsoup;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import com.ag777.util.lang.collection.MapUtils;

/**
 * JsoupUtils配套的配置类
 * 
 * @author ag777
 * @version create on 2017年10月17日,last modify at 2018年05月14日
 */
public class JsoupBuilder {

	private Integer timeOut;
	private Integer retryTimes;
	private Proxy proxy;
	private String userAgent;
	private boolean ignoreContentType;
	private Map<String, String> headerMap;
	private Map<String, String> cookieMap;
	private Map<String, String> dataMap;
	
	private JsoupBuilder() {
		ignoreContentType = false;
	}
	
	public static JsoupBuilder newInstance() {
		return new JsoupBuilder();
	}
	
	public JsoupUtils connect(String url) throws IOException {
		return JsoupUtils.connect(url, this);
	}
	
	public JsoupBuilder timeOut(Integer timeOut) {
		this.timeOut = timeOut;
		return this;
	}

	public JsoupBuilder retryTimes(Integer retryTimes) {
		this.retryTimes = retryTimes;
		return this;
	}

	public JsoupBuilder proxy(String ip, int port) {
		proxy = new Proxy();
		proxy.ip = ip;
		proxy.port = port;
		return this;
	}

	public JsoupBuilder userAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}
	
	public JsoupBuilder clearCookie() {
		cookieMap = null;
		return this;
	}

	public JsoupBuilder cookie(String key, String value) {
		if(cookieMap == null) {
			synchronized (JsoupBuilder.class) {
				if(cookieMap == null) {
					cookieMap = MapUtils.newHashTable();
				}
			}
		}
		cookieMap.put(key, value);
		return this;
	}
	
	public JsoupBuilder cookies(Map<String, String> cookies) {
		if(MapUtils.isEmpty(cookies)) {
			return this;
		}
		cookies.forEach((key, value)->{
			cookie(key, value);
		});
		return this;
	}
	
	public JsoupBuilder cookies(String cookiesStr) {
		Map<String, Object> cookieMap = MapUtils.ofMap(cookiesStr, ";", "=");
		Iterator<String> itor = cookieMap.keySet().iterator();
		while(itor.hasNext()) {
			String key = itor.next();
			cookie(key, cookieMap.get(key).toString());
		}
		return this;
	}
	
	public JsoupBuilder header(String key, String value) {
		if(headerMap == null) {
			synchronized (JsoupBuilder.class) {
				if(headerMap == null) {
					headerMap = MapUtils.newHashTable();
				}
			}
		}
		if(value != null) {
			headerMap.put(key, value);
		}
		return this;
	}
	
	/**
	 * 注意:该方法会略过值为null的键
	 * @param headers
	 * @return
	 */
	public <K, V>JsoupBuilder headers(Map<K, V> headers) {
		if(MapUtils.isEmpty(headers)) {
			return this;
		}
		headers.forEach((key, value)->{
			header(key.toString(), value.toString());
		});
		return this;
	}
	
	public JsoupBuilder data(String key, String value) {
		if(dataMap == null) {
			synchronized (JsoupBuilder.class) {
				if(dataMap == null) {
					dataMap = MapUtils.newHashTable();
				}
			}
		}
		if(value != null) {
			dataMap.put(key, value);
		}
		return this;
	}
	
	/**
	 * 注意:该方法会略过值为null的键
	 * @param datas
	 * @return
	 */
	public <K, V>JsoupBuilder dataMap(Map<K, V> datas) {
		if(MapUtils.isEmpty(datas)) {
			return this;
		}
		datas.forEach((key, value)->{
			data(key.toString(), value.toString());
		});
		return this;
	}
	
	public JsoupBuilder ignoreContentType(boolean ignoreContentType) {
		this.ignoreContentType = ignoreContentType;
		return this;
	}
	
	//--get
	public Integer timeOut() {
		return timeOut;
	}

	public Integer retryTimes() {
		return retryTimes;
	}

	public Proxy proxy() {
		return proxy;
	}

	public String userAgent() {
		return userAgent;
	}
	
	public Map<String, String> cookies() {
		return cookieMap;
	}
	
	public Map<String, String> headers() {
		return headerMap;
	}
	
	public Map<String, String> dataMap() {
		return dataMap;
	}
	
	public class Proxy {
		String ip;
		int port;
	}
	
	public boolean ignoreContentType() {
		return ignoreContentType;
	}
}
