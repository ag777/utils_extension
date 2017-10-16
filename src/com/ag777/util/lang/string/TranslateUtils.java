package com.ag777.util.lang.string;

import java.util.Map;
import java.util.Optional;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.http.HttpUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.string.model.ApiTranslatePojo;

/**
 * 在线翻译工具类
 * <p>
 * 调用的是百度翻译
 * </p>
 * 
 * @author ag777
 * @version create on 2017年10月16日,last modify at 2017年10月16日
 */
public class TranslateUtils {

	private static final String BASEURL = "http://fanyi.baidu.com/transapi";
	
	public static enum Language {
		CHINESE {
			@Override
			public String toString() {
				return "zh";
			}
		},
		ENGLISH {
			@Override
			public String toString() {
				return "en";
			}
		};
	}
	
	private TranslateUtils() {}
	
	/**
	 * 英译汉
	 * @param source
	 * @return
	 */
	public static Optional<String> enToZh(String source) {
		return translate(Language.ENGLISH, Language.CHINESE, source);
	}
	
	/**
	 * 汉译英
	 * @param source
	 * @return
	 */
	public static Optional<String> zhToEn(String source) {
		return translate(Language.CHINESE, Language.ENGLISH, source);
	}
	
	public static Optional<String> translate(Language from, Language to, String source)
    {
       return translate(from.toString(), to.toString(), source);
    }
	
	public static Optional<String> translate(String from, String to, String source) {
		 try {
	            String json=HttpUtils.doGet(BASEURL, getParams(from.toString(), to.toString(), source));
	            
	            ApiTranslatePojo translateMode=GsonUtils.get().fromJson(json, ApiTranslatePojo.class);
	            
	            if(translateMode!=null&&translateMode.getData()!=null&&translateMode.getData().size()==1)
	            {
	                return Optional.ofNullable(translateMode.getData().get(0).getDst());
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return Optional.empty();
	}
	
	private static Map<String, Object> getParams(String from, String to, String source) {
		return MapUtils.of(
				"from", from,
				"to", to,
				"query", source);
	}
}
