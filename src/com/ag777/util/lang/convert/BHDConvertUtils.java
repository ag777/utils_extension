package com.ag777.util.lang.convert;

import com.ag777.util.lang.StringUtils;

/**
 * 有关 进制转换 工具类
 * 
 * @author ag777
 * @version create on 2018年11月23日,last modify at 2018年11月23日
 */
public class BHDConvertUtils {

	private BHDConvertUtils() {}
	
	/**
	 * byte转二进制字符串
	 * @param b
	 * @return
	 */
	public static String byte2Binary(Byte b)  {
		if(b == null) {
			return null;
		}
		return Long.toString(b & 0xff, 2);
	}
	
	/**
	 * 16进制转byte数组
	 * <p>
	 * 会全部转化为大写再进行计算<br>
	 * 如果传入参数不为偶数，则会丢失最后一个字符
	 * </p>
	 * 
	 * @param hexStr 十六进制字符串
	 * @return
	 * @throws NumberFormatException 一般是传入字符串包含不为16进制的字符
	 */
	public static byte[] hex2Bytes(String hexStr) throws NumberFormatException {   
	    if (hexStr == null) {
	        return null;   
	    }   
	    hexStr = hexStr.toUpperCase();   
	    int length = hexStr.length() / 2;	//向下取整主动丢弃最后一个字符
	    char[] hexChars = hexStr.toCharArray();   
	    byte[] result = new byte[length];   
	    for (int i = 0; i < length; i++) {   
	        int pos = i * 2;
	        String temp = StringUtils.concat(hexChars[pos], hexChars[pos + 1]);
	        result[i] = (byte) Integer.parseInt(temp, 16);   
	    }   
	    return result;   
	}
	
	/**
	 * byte数组转16进制字符串
	 * @param bytes
	 * @return
	 */
	public static StringBuilder bytes2Hex(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb;
	}
	
	/**
	 * 16进制转2进制
	 * @param src
	 * @return
	 * @throws NumberFormatException 一般是传入字符串包含不为16进制的字符
	 */
	public static String hex2Binary(String src) throws NumberFormatException {
		if(src == null) {
			return null;
		}
		if(src.startsWith("0x")) {
			src = src.substring(2, src.length());
		}
		StringBuilder sb = new StringBuilder();
		int length = src.length();
		src = src.toUpperCase();
		for (int i = 0; i < length; i++) {
			switch(src.charAt(i)) {
				case '0':
					sb.append("0000");
					break;
				case '1':
					sb.append("0001");
					break;
				case '2':
					sb.append("0010");
					break;
				case '3':
					sb.append("0011");
					break;
				case '4':
					sb.append("0100");
					break;
				case '5':
					sb.append("0101");
					break;
				case '6':
					sb.append("0110");
					break;
				case '7':
					sb.append("0111");
					break;
				case '8':
					sb.append("1000");
					break;
				case '9':
					sb.append("1001");
					break;
				case 'A':
					sb.append("1010");
					break;
				case 'B':
					sb.append("1011");
					break;
				case 'C':
					sb.append("1100");
					break;
				case 'D':
					sb.append("1101");
					break;
				case 'E':
					sb.append("1110");
					break;
				case 'F':
					sb.append("1111");
					break;
				default:
					throw new NumberFormatException("含有非法字符:"+src.charAt(i)+",位置:"+i);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 二进制转16进制
	 * <p>
	 * 会先转为Integer类型
	 * </p>
	 * @param src
	 * @return
	 */
	public static String binary2Hex(String src) {
		if(src == null) {
			return null;
		}
		if(src.length()%4 != 0) {
			throw new RuntimeException("2进制串的长度不为4的倍数");
		}
		if(!src.matches("^(0|1)*$")) {
			throw new RuntimeException("2进制串包含0,1以外的字符");
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<src.length();i=i+4) {
			String item = src.substring(i, i+4);
			int tem=Integer.parseInt(item, 2);
			sb.append(Integer.toHexString(tem).toUpperCase());
		}
		return sb.toString();
	}
}
