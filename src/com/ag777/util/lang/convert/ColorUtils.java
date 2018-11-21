package com.ag777.util.lang.convert;

import java.awt.Color;

/**
 * 
 * @author ag777
 * @version create on 2018年11月07日,last modify at 2018年11月07日
 */
public class ColorUtils {

	private ColorUtils() {}
	
	/**
	 * 根据一个16进制的数字获取一个颜色
	 * @param hexStr 请先去除开头的0x(如果有的话)
	 * @return
	 * @throws NumberFormatException
	 */
	public static Color getByHex(String hexStr) throws NumberFormatException {
		int rgb = Integer.parseInt(hexStr, 16);
		return new Color(rgb);
	}
	
	/**
	 * 从一个颜色中提取出16进制的色值(返回的字符串不包含开头的0x)
	 * @param color
	 * @return
	 */
	public static String getHex(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		return Integer.toHexString(r)+Integer.toHexString(g)+Integer.toHexString(b);
	}
	
	/**
	 * 从一个颜色中取出10进制的int型色值
	 * @param color
	 * @return
	 */
	public static int getInt(Color color) {
		String rgb = getHex(color);
		return Integer.parseInt(rgb, 16);
	}
	
}
