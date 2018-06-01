package com.ag777.util.lang;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import com.ag777.util.lang.model.Charsets;


/**
 * 编码转换辅助类
 * 
 * @author ag777
 * @version 
 */
public class CodingUtils {

	/**
	 * 将字符编码转换成US-ASCII码
	 */
	public static String toASCII(String str) throws UnsupportedEncodingException {
		return CodingTest(str, Charsets.US_ASCII);
	}

	/**
	 * 将字符编码转换成ISO-8859-1码
	 */
	public static String toISO_8859_1(String str) throws UnsupportedEncodingException {
		return CodingTest(str, Charsets.ISO_8859_1);
	}

	/**
	 * 将字符编码转换成UTF-8码
	 */
	public static String toUTF_8(String str) throws UnsupportedEncodingException {
		return CodingTest(str, Charsets.UTF_8);
	}

	/**
	 * 将字符编码转换成UTF-16BE码
	 */
	public static String toUTF_16BE(String str) throws UnsupportedEncodingException {
		return CodingTest(str, Charsets.UTF_16BE);
	}

	/**
	 * 将字符编码转换成UTF-16LE码
	 */
	public static String toUTF_16LE(String str) throws UnsupportedEncodingException {
		return CodingTest(str, Charsets.UTF_16LE);
	}

	/**
	 * 将字符编码转换成UTF-16码
	 */
	public static String toUTF_16(String str) throws UnsupportedEncodingException {
		return CodingTest(str, Charsets.UTF_16);
	}

	/**
	 * 将字符编码转换成GBK码
	 */
	public static String toGBK(String str) throws UnsupportedEncodingException {
		return CodingTest(str, Charsets.GBK);
	}

	/**
	 * 解决网页参数乱码
	 */
	public static String HtmlDecoder(String str) throws UnsupportedEncodingException {
		return URLDecoder.decode(str, Charsets.UTF_8.toString());
	}
	
	/**
	 * 转换网页参数为乱码
	 */
	public static String HtmlEncoder(String str) throws UnsupportedEncodingException {
		return URLEncoder.encode(str, Charsets.UTF_8.toString());
	}
	
	/**
	 * 字符串编码转换的实现方法
	 * 
	 * @param str
	 *            待转换编码的字符串
	 * @param newCharset
	 *            目标编码
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String CodingTest(String str, Charset newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// 用默认字符编码解码字符串。
			byte[] bs = str.getBytes();
			// 用新的字符编码生成字符串
			return new String(bs, newCharset);
		}
		return null;
	}

	/**
	 * 字符串编码转换的实现方法
	 * 
	 * @param str
	 *            待转换编码的字符串
	 * @param oldCharset
	 *            原编码
	 * @param newCharset
	 *            目标编码
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String CodingTest(String str, String oldCharset, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// 用旧的字符编码解码字符串。解码可能会出现异常。
			byte[] bs = str.getBytes(oldCharset);
			// 用新的字符编码生成字符串
			return new String(bs, newCharset);
		}
		return null;
	}
}
