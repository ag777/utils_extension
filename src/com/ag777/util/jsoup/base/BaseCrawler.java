package com.ag777.util.jsoup.base;

import java.io.IOException;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import com.ag777.util.jsoup.JsoupBuilder;
import com.ag777.util.jsoup.JsoupUtils;
import com.ag777.util.lang.StringUtils;

public abstract class BaseCrawler {

	protected JsoupUtils u;
	
	public BaseCrawler(JsoupUtils u) {
		this.u = u;
		init(u);
	}
	
	public abstract void init(JsoupUtils u);
	
	protected static JsoupBuilder builder(String cookiesStr, Integer retryTimes) throws IOException {
		JsoupBuilder builder = JsoupBuilder.newInstance();
		if(retryTimes != null && retryTimes > 0) {
			builder.retryTimes(retryTimes);
		}
		if(!StringUtils.isBlank(cookiesStr)) {
			builder.cookies(cookiesStr);
		}
		return builder;
	}
}
