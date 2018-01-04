package com.ag777.util.lang.string;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.http.HttpUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.string.model.ApiTranslatePojo;

/**
 * 在线翻译工具类
 * <p>
 * 调用的是百度翻译
 * </p>
 * 
 * @author ag777
 * @version create on 2017年10月16日,last modify at 2018年01月04日
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
	 * @throws IOException 
	 */
	public static Optional<String> en2Zh(String source) throws IOException {
		return translate(Language.ENGLISH, Language.CHINESE, source);
	}
	
	/**
	 * 汉译英
	 * @param source
	 * @return
	 * @throws IOException 
	 */
	public static Optional<String> zh2En(String source) throws IOException {
		return translate(Language.CHINESE, Language.ENGLISH, source);
	}
	
	public static Optional<String> translate(Language from, Language to, String source) throws IOException
    {
       return translate(from.toString(), to.toString(), source);
    }
	
	public static Optional<String> translate(String from, String to, String source) throws IOException {
		if(source == null) {
			return Optional.empty();
		}
		if(StringUtils.isBlank(source)) {
			return Optional.of("");
		}
		
        Optional<String> json=HttpUtils.doGet(BASEURL, getParams(from.toString(), to.toString(), source));
        if(json.isPresent()) {
        	ApiTranslatePojo translateMode=GsonUtils.get().fromJson(json.get(), ApiTranslatePojo.class);
            
            if(translateMode!=null&&translateMode.getData()!=null&&translateMode.getData().size()==1)
            {
                return Optional.ofNullable(translateMode.getData().get(0).getDst());
            }
        } else {
        	throw new IOException("连接失败");
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
