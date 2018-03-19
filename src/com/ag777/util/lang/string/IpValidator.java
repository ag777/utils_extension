package com.ag777.util.lang.string;

import java.util.List;
import java.util.regex.Pattern;

import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;

/**
 * ip地址验证辅助类
 * <p>
 *  包含网段的正则验证和拆分
 * </p>
 * 
 * @author ag777
 * @version create on 2018年02月27日,last modify at 2018年03月15日
 */
public class IpValidator {

	public static Pattern PATTERN_IPRANGE;	//ip验证，包含网段
	
	static {
		PATTERN_IPRANGE = Pattern.compile("^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9])\\."//第一节不能为0
				+"(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[0-9])\\."//第二节可以为0
				+"("	//第三节分两种情况,如果第三节带-或者为*,则第四节一定为*
					+ "(((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[0-9])-(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9]))|\\*)\\.\\*"	//前半段可以为0,后半段不能为0
					+ "|"
					+ "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[0-9])\\."	//不带-不为*的情况,可以为0
					+ "((25[0-4]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9])(-(25[0-4]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9]))?|\\*)"
				+ ")$");	//第四节不能为0
	}
	
	private IpValidator() {
	}
	
	/**
	 * 是否为ip或网段
	 * <p>
	 *  String[] fix = {	//应该返回true
				"192.168.1.2",
				"192.0.0.1",
				"192.168.1.2-100",
				"192.168.1.*",
				"192.168.1-255.*",
				"192.168.*.*",
				"192.168.0-255.*"
		};
		
		String[] not_fix = { //应该返回false
				"192.168.1.255",
				"192.168.1.254-255",
				"192.168.1-100.1-20",
				"192.168.-1.2",
				"192.168.244.0",
				"192.168.0-0.*"
		};
		System.out.println("以下应该返回true===");
		for (String ip : fix) {
			System.out.println(StringUtils.concat(ip, ":", isIpRange(ip)));
		}
		System.out.println("以下应该返回false===");
		for (String ip : not_fix) {
			System.out.println(StringUtils.concat(ip, ":", isIpRange(ip)));
		}
	 * </p>
	 * @param src
	 * @return
	 */
	public static boolean isIpRange(String src) {
		return PATTERN_IPRANGE.matcher(src).matches();
	}
	
	/**
	 * 拆分网段
	 * <p>
	 * 	先将*替换为各网段,1-3节的*替换为1-255,第四节替换为1-154
	 * 逐节拆分网段为一个个ip并进行拼接
	 * </p>
	 * @param ip
	 * @return
	 */
	public static List<String> splitNetSegment(String ip) {
		String[] group = ip.split("\\.");
		for(int i=0;i<group.length;i++) {
			if("*".equals(group[i])) {
				if(i<group.length-1) {
					group[i] = "1-255";
				} else {
					group[i] = "1-254";
				}
			}
		}
		return splitItem(group, 0, null);
	}
	
	/**
	 * 拆分ip中的一节
	 * <p>
	 * 比如拆分1-10,得到{1,2,3,4,5,6,7,8,9,10}
	 * </p>
	 * @param groups
	 * @param index
	 * @param header
	 * @return
	 */
	private static List<String> splitItem(String[] groups, int index, String header) {
		String item = groups[index];
		boolean isLast = (groups.length == index+1);
		if(!StringUtils.isEmpty(header)) {
			header += ".";
		} else {
			header = "";
		}
		List<String> list = ListUtils.newArrayList();
		if(item.contains("-")) {
			String[] group1 = item.split("-");
			int min = StringUtils.toInt(group1[0]);
			int max = StringUtils.toInt(group1[1]);
			if(min>max) {
				throw new RuntimeException("最小值不能大于最大值");
			}
			for(int j=min;j<=max;j++) {
				String temp = header+j;
				if(!isLast) {
					list.addAll(splitItem(groups, index+1, temp));
				} else {
					list.add(temp);
				}
				
			}
		} else {
			String temp = header+item;
			if(!isLast) {
				list.addAll(splitItem(groups, index+1, temp));
			} else {
				list.add(temp);
			}
			
		}
		return list;
	}
	
	public static void main(String[] args) {
		List<String> list = splitNetSegment("192.168.1-3.*");
		System.out.println(isIpRange("192.168.1-3.*"));
		for (String item : list) {
			System.out.println(item);
		}
	}
}
