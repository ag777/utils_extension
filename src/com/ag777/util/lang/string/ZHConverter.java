package com.ag777.util.lang.string;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


/**
 * 中文简体繁体转换工具
 * <p>
 * 开源库https://github.com/rookiefly/java-zhconverter
 * </p>
 * 
 * @author rookiefly
 * @version create on 2017年10月13日,last modify at 2018年02月07日
 */
public class ZHConverter {

    private Properties charMap = new Properties();

    public static final int TRADITIONAL = 0;
    public static final int SIMPLIFIED = 1;
    private static final int NUM_OF_CONVERTERS = 2;
    private static final ZHConverter[] converters = new ZHConverter[NUM_OF_CONVERTERS];
    private static final String[] propertyFiles = new String[2];

    static {
        propertyFiles[TRADITIONAL] = "resource/zh2Hant.properties";
        propertyFiles[SIMPLIFIED] = "resource/zh2Hans.properties";
    }

    /**
     * @param converterType 0 for traditional and 1 for simplified
     * @return
     */
    public static ZHConverter getInstance(int converterType) {

        if (converterType >= 0 && converterType < NUM_OF_CONVERTERS) {

            if (converters[converterType] == null) {
                synchronized (ZHConverter.class) {
                    if (converters[converterType] == null) {
                        converters[converterType] = new ZHConverter(propertyFiles[converterType]);
                    }
                }
            }
            return converters[converterType];

        } else {
            return null;
        }
    }

    private ZHConverter(String propertyFile) {

        InputStream is = getClass().getClassLoader().getResourceAsStream(propertyFile);
        if (is != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is));
                charMap.load(reader);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 静态转换方法
     *
     * @param text
     * @param converterType
     * @return
     */
    public static String convert(String text, int converterType) {
    	if(text == null) {
    		return null;
    	}
        ZHConverter instance = getInstance(converterType);
        return instance.convert(text);
    }

    /**
     * @param in
     * @return
     */
    public String convert(String in) {
    	if(in == null) {
    		return null;
    	}
        StringBuilder outString = new StringBuilder();
        StringBuilder stackString = new StringBuilder();

        for (int i = 0; i < in.length(); i++) {

            char c = in.charAt(i);
            String key = "" + c;
            stackString.append(key);
            if (charMap.containsKey(stackString.toString())) {
                outString.append(charMap.get(stackString.toString()));
                stackString.setLength(0);
            } else {
                CharSequence sequence = stackString.subSequence(0, stackString.length() - 1);
                stackString.delete(0, stackString.length() - 1);
                flushStack(outString, new StringBuilder(sequence));
            }
        }

        flushStack(outString, stackString);

        return outString.toString();
    }

    private void flushStack(StringBuilder outString, StringBuilder stackString) {
        while (stackString.length() > 0) {
            if (charMap.containsKey(stackString.toString())) {
                outString.append(charMap.get(stackString.toString()));
                stackString.setLength(0);

            } else {
                outString.append("" + stackString.charAt(0));
                stackString.delete(0, 1);
            }

        }
    }

    public Properties getDict() {
        return charMap;
    }

}
