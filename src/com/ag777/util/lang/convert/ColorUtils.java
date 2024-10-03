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
	 * 将十六进制字符串转换为Color对象
	 * @param hexStr 十六进制颜色字符串，格式为"#RRGGBB"
	 * @return 对应的Color对象
	 * @throws NumberFormatException 如果字符串格式不正确
	 */
	public static Color toColor(String hexStr) throws NumberFormatException {
		int rgb = toInt(hexStr);
		return toColor(rgb);
	}

	/**
	 * 将整数颜色值转换为Color对象
	 * @param rgb 整数颜色值，0xRRGGBB格式
	 * @return 对应的Color对象
	 */
	public static Color toColor(int rgb) {
		return new Color(rgb);
	}

	/**
	 * 将三个整数分别作为红、绿、蓝分量来创建Color对象
	 * @param r 红色分量，取值范围0-255
	 * @param g 绿色分量，取值范围0-255
	 * @param b 蓝色分量，取值范围0-255
	 * @return 对应的Color对象
	 */
	public static Color toColor(int r, int g, int b) {
		return new Color(r, g, b);
	}

	/**
	 * 将Color对象转换为十六进制颜色字符串
	 * @param color Color对象
	 * @return 十六进制颜色字符串，格式为"#RRGGBB"
	 */
	public static String toHex(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		return toHex(r, g, b);
	}

	/**
	 * 将RGB分量转换为十六进制颜色字符串
	 * @param r 红色分量，取值范围0-255
	 * @param g 绿色分量，取值范围0-255
	 * @param b 蓝色分量，取值范围0-255
	 * @return 十六进制颜色字符串，格式为"RRGGBB"
	 */
	public static String toHex(int r, int g, int b) {
		return toHexString(r)+toHexString(g)+toHexString(b);
	}

	/**
	 * 将整数颜色值转换为十六进制颜色字符串
	 * @param rgb 整数颜色值，0xRRGGBB格式
	 * @return 十六进制颜色字符串，格式为"RRGGBB"
	 */
	public static String toHex(int rgb) {
		return Integer.toHexString(rgb);
	}

	/**
	 * 将Color对象转换为整数颜色值
	 * @param color Color对象
	 * @return 整数颜色值，0xRRGGBB格式
	 * @throws NumberFormatException 如果颜色值无法转换为整数
	 */
	public static int toInt(Color color) throws NumberFormatException {
		String hexStr = toHex(color);
		return toInt(hexStr);
	}

	/**
	 * 将十六进制颜色字符串转换为整数颜色值
	 * @param hexStr 十六进制颜色字符串，格式为"#RRGGBB"
	 * @return 整数颜色值，0xRRGGBB格式
	 * @throws NumberFormatException 如果字符串格式不正确
	 */
	public static int toInt(String hexStr) throws NumberFormatException {
		if(hexStr.startsWith("#")) { // 如果是#开头则删除第一个字符(#)，否则影响转化
			hexStr = hexStr.substring(1);
		}
		return Integer.parseInt(hexStr, 16);
	}

	/**
	 * 将整数颜色值分解为RGB分量
	 * @param rgb 整数颜色值，0xRRGGBB格式
	 * @return 包含红、绿、蓝分量的整数数组，每个分量取值范围0-255
	 */
	public static int[] toRGB(int rgb) {
		int R = (rgb & 0xff0000) >> 16;
		int G = (rgb & 0xff00) >> 8;
		int B = (rgb & 0xff);
		return new int[] {R, G, B};
	}

	/**
	 * 将十六进制颜色字符串分解为RGB分量
	 * @param hexStr 十六进制颜色字符串，格式为"RRGGBB"
	 * @return 包含红、绿、蓝分量的整数数组，每个分量取值范围0-255
	 */
	public static int[] toRGB(String hexStr) {
		int[] rgb = new int[3];
		for(int i=0,j=0;i<6;i+=2, j++) {
			String temp = hexStr.substring(i, i+2);
			rgb[j] = Integer.parseInt(temp, 16);
		}
		return rgb;
	}

	/**
	 * 将整数分量转换为两位的十六进制字符串
	 * @param r 整数分量，取值范围0-255
	 * @return 两位的十六进制字符串，如果位数不足则补0
	 */
	private static String toHexString(int r) {
		String hex = Integer.toHexString(r);
		if(hex.length()<2) {
			return "0"+hex;
		}
		return hex;
	}
	
}
