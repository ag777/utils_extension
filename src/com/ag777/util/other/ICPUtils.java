package com.ag777.util.other;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.jsoup.select.Elements;
import com.ag777.util.jsoup.JsoupBuilder;
import com.ag777.util.jsoup.JsoupUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.exception.model.ValidateException;

/**
 * 备案信息获取工具类
 * 
 * @author ag777
 * @version create on 2018年08月14日,last modify at 2018年08月17日
 */
public class ICPUtils {

	private static final String URL_ICP_SEARCH = "http://icp.chinaz.com/searchs";
	
	private ICPUtils() {}
	
	/**
	 * 通过icp.chinaz.com网站查询指定URL的网站备案信息
	 * 
	 * @param urlStr
	 * @return 
	 * @return
	 * @throws ValidateException 解析失败
	 */
	public static Optional<String> getByChinaz(String urlStr) throws ValidateException {
		try {
			String domain = getDomain(urlStr);
			
			JsoupUtils u = JsoupBuilder.newInstance().dataMap(
					MapUtils.of(
							"btn_search", "查询",
							"urls", domain)).connect(URL_ICP_SEARCH);
			Elements trs = u.select(".Tool-batchTable>tbody>tr");
			Elements tds = trs.get(0).children();
			/*
			 * 0:域名
			 * 1:主办单位名称
			 * 2:单位性质
			 * 3:网站备案/许可证号
			 * 4:网站名称
			 * 5:网站首页网址
			 * 6:审核时间
			 */
			String code = tds.get(3).text().trim();
			if(StringUtils.isEmpty(code) || "未备案或者备案取消".equals(code)) {
				return Optional.empty();
			}
			return Optional.ofNullable(code);
		} catch(ValidateException ex) {
			throw ex;
		} catch(IOException ex) {
			throw new ValidateException("访问["+URL_ICP_SEARCH+"失败");
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new ValidateException("页面解析失败:"+ex.getMessage());
		}
	}
	
	/**
	 * 从字符串类型中截取域名
	 * <p>
	 * 将字符串类型的url转化为URL对象，调用其方法解析出域名
	 * </p>
	 * @param urlStr
	 * @return
	 * @throws ValidateException 解析url失败
	 */
	private static String getDomain(String urlStr) throws ValidateException {
		if(StringUtils.isBlank(urlStr)) {
			throw new ValidateException("url不能为空");
		}
		if(!urlStr.startsWith("http")) {
			urlStr = "http://"+urlStr;
		}
		try {
			URL url = new URL(urlStr);
			return url.getHost();
		} catch (MalformedURLException e) {
			throw new ValidateException("解析url失败:"+e.getMessage());
		}
		
	}
	
	public static void main(String[] args) throws ValidateException {
		System.out.println(
				getByChinaz("baidu.com"));
	}
}
