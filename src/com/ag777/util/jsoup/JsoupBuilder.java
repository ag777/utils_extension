package com.ag777.util.jsoup;

import java.io.IOException;

/**
 * JsoupUtils配套的配置类
 * 
 * @author ag777
 * @version create on 2017年10月17日,last modify at 2017年10月17日
 */
public class JsoupBuilder {

	private Integer timeOut;
	private Integer retryTimes;
	private Proxy proxy;
	private String userAgent;
	
	private JsoupBuilder() {}
	
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
	
	public class Proxy {
		String ip;
		int port;
	}
}
