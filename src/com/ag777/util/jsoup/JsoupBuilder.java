package com.ag777.util.jsoup;

import java.io.IOException;
import java.util.Map;

import com.ag777.util.lang.collection.MapUtils;

/**
 * JsoupUtils配套的配置类
 * 
 * @author ag777
 * @version create on 2017年10月17日,last modify at 2017年10月29日
 */
public class JsoupBuilder {

	private Integer timeOut;
	private Integer retryTimes;
	private Proxy proxy;
	private String userAgent;
	private boolean ignoreContentType;
	private Map<String, String> headerMap;
	private Map<String, String> cookieMap;
	
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
		cookies.forEach((key, value)->{
			cookie(key, value);
		});
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
		headerMap.put(key, value);
		return this;
	}
	
	public JsoupBuilder headers(Map<String, String> headers) {
		headers.forEach((key, value)->{
			header(key, value);
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
	
	public class Proxy {
		String ip;
		int port;
	}
	
	public boolean ignoreContentType() {
		return ignoreContentType;
	}
}
