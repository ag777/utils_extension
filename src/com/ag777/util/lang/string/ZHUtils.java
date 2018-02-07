package com.ag777.util.lang.string;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 中文工具类
 * <p>
 * 修改自开源库https://github.com/rookiefly/java-zhconverter
 * </p>
 * 
 * @author rookiefly
 * @version create on 2017年10月13日,last modify at 2018年02月07日
 */
public class ZHUtils {

	/**
	 * 转换为繁体中文
	 * @param in
	 * @return
	 */
    public static String toTraditional(String in) {
        return ZHConverter.convert(in, ZHConverter.TRADITIONAL);
    }

    /**
     * 转换为简体中文
     * @param in
     * @return
     */
    public static String toSimplified(String in) {
        return ZHConverter.convert(in, ZHConverter.SIMPLIFIED);
    }

    /**
     * 是否为繁体
     * @param in
     * @return
     */
    public static boolean isTraditional(String in) {
        ZHConverter converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
        Properties dict = converter.getDict();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            String key = "" + c;
            if (dict.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含中文
     * @param str
     * @return
     */
    public static boolean isContainsChinese(String str) {

        String regEx = "[\u4e00-\u9fa5]";
        Pattern pat = Pattern.compile(regEx);
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg;
    }

    /**
     * 是否都为中文
     * @param str
     * @return
     */
    public static boolean isAllChinese(String str) {

        boolean flag = true;
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (!isContainsChinese(str.substring(i, i + 1))) {
                flag = false;
                break;
            }
        }

        return flag;
    }
}
