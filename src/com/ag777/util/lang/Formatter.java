package com.ag777.util.lang;

import java.util.ArrayList;

/**
 * 单位格式化
 * 
 * @author ag777
 * @version create on 2017年06月13日,last modify at 2018年06月15日
 */
public class Formatter {

	/**
	 * 保留decimalPlaces位小数
	 * @param num
	 * @param decimalPlaces 保留小数位数
	 * @return
	 */
	public static String num(double num, int decimalPlaces) {
		return StringUtils.formatNum(num, decimalPlaces);
	}
	
	/**
	 * 存储单位格式化输出，初始单位Byte
	 * 
	 * @param b
	 * @return
	 */
	public static String storage(long b) {
		StringBuffer sb = new StringBuffer();
		if (b < 1024) {
			sb.append(b);
			sb.append("B");
		}
		if (1024 * 1024 > b && b >= 1024) {
			String num = String.valueOf((double) (b / 1024));
			sb.append(num.substring(0, num.indexOf(".") + 2));
			sb.append("KB");
		}
		if (1024 * 1024 * 1024 > b && b >= 1024 * 1024) {
			String num = String.valueOf((double) (b / (1024 * 1024)));
			sb.append(num.substring(0, num.indexOf(".") + 2));
			sb.append("MB");
		}
		if (1024 * 1024 * 1024 * 1024 >= b
				&& b >= 1024 * 1024 * 1024) {
			String num = String
					.valueOf((double) (b / (1024 * 1024 * 1024)));
			sb.append(num.substring(0, num.indexOf(".") + 2));
			sb.append("GB");
		}
		return sb.toString();
	}
	
	
	/**
	 * json字符串的格式化(用于输出文件)
	 * 
	 * @param json
	 * @param fillStringUnit 换行后添加的字符串，一般传\t或者4个空格
	 * @return
	 */
	public static String formatJson(String json, String fillStringUnit) {
		if (json == null || json.trim().length() == 0) {
			return null;
		}

		int fixedLenth = 0;
		ArrayList<String> tokenList = new ArrayList<String>();
		{
			String jsonTemp = json;
			// 预读取
			while (jsonTemp.length() > 0) {
				String token = getToken(jsonTemp);
				jsonTemp = jsonTemp.substring(token.length());
				token = token.trim();
				tokenList.add(token);
			}
		}

		for (int i = 0; i < tokenList.size(); i++) {
			String token = tokenList.get(i);
			int length = token.getBytes().length;
			if (length > fixedLenth && i < tokenList.size() - 1 && tokenList.get(i + 1).equals(":")) {
				fixedLenth = length;
			}
		}

		StringBuilder buf = new StringBuilder();
		int count = 0;
		for (int i = 0; i < tokenList.size(); i++) {

			String token = tokenList.get(i);

			if (token.equals(",")) {
				buf.append(token);
				doFill(buf, count, fillStringUnit);
				continue;
			}
			if (token.equals(":")) {
				buf.append(" ").append(token).append(" ");
				continue;
			}
			if (token.equals("{")) {
				String nextToken = tokenList.get(i + 1);
				if (nextToken.equals("}")) {
					i++;
					buf.append("{ }");
				} else {
					count++;
					buf.append(token);
					doFill(buf, count, fillStringUnit);
				}
				continue;
			}
			if (token.equals("}")) {
				count--;
				doFill(buf, count, fillStringUnit);
				buf.append(token);
				continue;
			}
			if (token.equals("[")) {
				String nextToken = tokenList.get(i + 1);
				if (nextToken.equals("]")) {
					i++;
					buf.append("[ ]");
				} else {
					count++;
					buf.append(token);
					doFill(buf, count, fillStringUnit);
				}
				continue;
			}
			if (token.equals("]")) {
				count--;
				doFill(buf, count, fillStringUnit);
				buf.append(token);
				continue;
			}

			buf.append(token);
			// 左对齐
			if (i < tokenList.size() - 1 && tokenList.get(i + 1).equals(":")) {
				int fillLength = fixedLenth - token.getBytes().length;
				if (fillLength > 0) {
					for (int j = 0; j < fillLength; j++) {
						buf.append(" ");
					}
				}
			}
		}
		return buf.toString();
	}

	
	/*---------------------内部工具方法-------------------------*/
	private static String getToken(String json) {
		StringBuilder buf = new StringBuilder();
		boolean isInYinHao = false;
		while (json.length() > 0) {
			String token = json.substring(0, 1);
			json = json.substring(1);

			if (!isInYinHao && (token.equals(":") || token.equals("{") || token.equals("}") || token.equals("[") || token.equals("]") || token.equals(","))) {
				if (buf.toString().trim().length() == 0) {
					buf.append(token);
				}

				break;
			}

			if (token.equals("\\")) {
				buf.append(token);
				buf.append(json.substring(0, 1));
				json = json.substring(1);
				continue;
			}
			if (token.equals("\"")) {
				buf.append(token);
				if (isInYinHao) {
					break;
				} else {
					isInYinHao = true;
					continue;
				}
			}
			buf.append(token);
		}
		return buf.toString();
	}

	/**
	 * 添加换行符及缩进字符
	 * 
	 * @param buf
	 * @param count
	 * @param fillStringUnit
	 */
	private static void doFill(StringBuilder buf, int count, String fillStringUnit) {
		buf.append(SystemUtils.lineSeparator());
		for (int i = 0; i < count; i++) {
			buf.append(fillStringUnit);
		}
	}
	
	
}
