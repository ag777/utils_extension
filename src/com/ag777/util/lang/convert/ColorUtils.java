package com.ag777.util.lang.convert;

import java.awt.Color;

/**
 * 
 * @author ag777
 * @version create on 2018年11月07日,last modify at 2020年03月26日
 */
public class ColorUtils {

	private ColorUtils() {}
	
	/**
	 * 根据一个16进制的数字获取一个颜色
	 * @param hexStr 请先去除开头的0x(如果有的话)
	 * @return
	 * @throws NumberFormatException
	 */
	public static Color toColor(String hexStr) throws NumberFormatException {
		int rgb = toInt(hexStr);
		return toColor(rgb);
	}
	
	/**
	 * int型色值转化为color
	 * @param rgb int型色值
	 * @return
	 */
	public static Color toColor(int rgb) {
		return new Color(rgb);
	}
	
	/**
	 * 将rgb转化为颜色
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return
	 */
	public static Color toColor(int r, int g, int b) {
		return new Color(r, g, b);
	}
	
	/**
	 * 从一个颜色中提取出16进制的色值(返回的字符串不包含开头的0x)
	 * @param color
	 * @return
	 */
	public static String toHex(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		return toHex(r, g, b);
	}
	
	/**
	 * 将rgb转化为16进制色值
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @return
	 */
	public static String toHex(int r, int g, int b) {
		return toHexString(r)+toHexString(g)+toHexString(b);
	}
	
	/**
	 * 将int型色值转化为16进制色值(实际上就是10进制转16进制)
	 * @param rgb int型色值
	 * @return
	 */
	public static String toHex(int rgb) {
		return Integer.toHexString(rgb);
	}
	
	/**
	 * 从一个颜色中取出10进制的int型色值
	 * @param color
	 * @return
	 * @throws NumberFormatException
	 */
	public static int toInt(Color color) throws NumberFormatException {
		String hexStr = toHex(color);
		return toInt(hexStr);
	}
	
	/**
	 * 将16进制的色值转化为int色值
	 * @param hexStr 16进制色值
	 * @return
	 * @throws NumberFormatException
	 */
	public static int toInt(String hexStr) throws NumberFormatException {
		if(hexStr.startsWith("#")) {	//如果是#开头则删除第一个字符(#)，否则影响转化
			hexStr = hexStr.substring(1);
		}
		return Integer.parseInt(hexStr, 16);
	}
	
	/**
	 * 获取rgb
	 * @param rgb int型色值
	 * @return
	 */
	public static int[] toRGB(int rgb) {
		int R = (rgb & 0xff0000) >> 16;
		int G = (rgb & 0xff00) >> 8;
		int B = (rgb & 0xff);
		return new int[] {R, G, B};
	}
	
	/**
	 * 获取rgb
	 * @param hexStr 16进制色值
	 * @return
	 */
	public static int[] toRGB(String hexStr) {
		int[] rgb = new int[3];
		for(int i=0,j=0;i<6;i+=2,j++) {
			String temp = hexStr.substring(i, i+2);
			rgb[j] = Integer.parseInt(temp, 16);
		}
		return rgb;
	}
	
	/**
	 * 转化rgb 到16进制(固定两位)
	 * @param r r 或 g 或 b
	 * @return 16进制字符串
	 */
	private static String toHexString(int r) {
		String hex = Integer.toHexString(r);
		if(hex.length()<2) {
			return "0"+hex;
		}
		return hex;
	}
	
}
