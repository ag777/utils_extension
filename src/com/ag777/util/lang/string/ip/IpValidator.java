package com.ag777.util.lang.string.ip;

import java.util.List;
import java.util.regex.Pattern;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;

import sun.net.util.IPAddressUtil;

/**
 * ip地址验证辅助类
 * <p>
 *  包含网段的正则验证和拆分
 * </p>
 * 
 * @author ag777
 * @version create on 2018年02月27日,last modify at 2019年06月10日
 */
public class IpValidator {

	public final static Pattern PATTERN_IP;	//单ip验证,不包含网段(严格版)
	public final static Pattern PATTERN_IPRANGE;	//ip验证，包含网段(严格版)
	
	public final static Pattern P_IPV6_SINGLE;		//afb8:afb8:afb8:afb8:afb8:afb8:afb:abcd
	public final static Pattern P_IPV6_ASTERISK;         //afb8:afb8:afb8:afb8:afb8:afb8:afb:*
	public final static Pattern P_IPV6_SEGMENT;    	//afb8:afb8:afb8:afb8:afb8:afb8:1234-2500
	
	static {
		PATTERN_IP = Pattern.compile(
				"^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9])\\." //第一节不能为0
				+"((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[0-9])\\.){2}" //第二三节可以为0
				+ "(25[0-4]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9])"	//第四节不能为0,也不能为255,最后一位255是留给广播地址用的.0的话是网段的意思.
				);
		PATTERN_IPRANGE = Pattern.compile("^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9])\\."//第一节不能为0
				+"(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[0-9])\\."//第二节可以为0
				+"("	//第三节分两种情况,如果第三节带-或者为*,则第四节一定为*
					+ "(((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[0-9])-(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9]))|\\*)\\.\\*"	//前半段可以为0,后半段不能为0
					+ "|"
					+ "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[0-9])\\."	//不带-不为*的情况,可以为0
					+ "((25[0-4]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9])(-(25[0-4]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9]))?|\\*)"	//第四节不能为0,也不能为255,最后一位255是留给广播地址用的.0的话是网段的意思.
				+ ")$");	
		
		P_IPV6_SINGLE = Pattern.compile("("+
			"(([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|"+
			"(([0-9A-Fa-f]{1,4}:){6}(((:[0-9A-Fa-f]{1,4}){1,2})|:))|"+
			"(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,3})|:))|"+
			"(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,4})|:))|"+
			"(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,5})|:))|"+
			"(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,6})|:))|"+
			"(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,7})|:))|"+
			"(:(((:[0-9A-Fa-f]{1,4}){1,7})|:))"+
			")");		//afb8:afb8:afb8:afb8:afb8:afb8:afb:abcd
		
		P_IPV6_ASTERISK = Pattern.compile("("+
			"(([0-9A-Fa-f]{1,4}:){7}([*]))|"+
			"(([0-9A-Fa-f]{1,4}:){6}(:)[*])|"+
			"(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}):)|:)[*])|"+
			"(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,2}:)|:)[*])|"+
			"(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,3}:)|:)[*])|"+
			"(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,4}:)|:)[*])|"+
			"(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,5}:)|:)[*])|"+
			"(:(((:[0-9A-Fa-f]{1,4}){1,6}:)|:)[*])"+
			")");         //afb8:afb8:afb8:afb8:afb8:afb8:afb:*
		
		P_IPV6_SEGMENT = Pattern.compile("("+
			"(([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}-[0-9A-Fa-f]{1,4})|"+
			"(([0-9A-Fa-f]{1,4}:){6}(((:[0-9A-Fa-f]{1,4})))-[0-9A-Fa-f]{1,4})|"+
			"(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2}))-[0-9A-Fa-f]{1,4})|"+
			"(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3}))-[0-9A-Fa-f]{1,4})|"+
			"(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4}))-[0-9A-Fa-f]{1,4})|"+
			"(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5}))-[0-9A-Fa-f]{1,4})|"+
			"(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6}))-[0-9A-Fa-f]{1,4})|"+
			"(:(((:[0-9A-Fa-f]{1,4}){1,6}:)|:)((([0-9A-Fa-f]{1,4})))-[0-9A-Fa-f]{1,4})"+
			")");   	//afb8:afb8:afb8:afb8:afb8:afb8::1234-2500
	}
	
	private IpValidator() {
	}
	
	/**
	 *  判断是否是内网ip
	 *  <p>
	 *  代码参考:https://yq.aliyun.com/ziliao/151375
	 *  注意:该方法处理127.0.0.1不算做内网ip
	 *  以下网段为内网ip:
	 *  10.0.0.0/8：10.0.0.0～10.255.255.255 
		172.16.0.0/12：172.16.0.0～172.31.255.255 
		192.168.0.0/16：192.168.0.0～192.168.255.255
	 *  </p>
	 *  
	 * @param src ip
	 * @return 是否是内网地址
	 */
	public static boolean isLan(String src) {
		if(StringUtils.isBlank(src)) {
			throw new IllegalArgumentException("参数字符串不能为空");
		}
		if(!isIp(src)) {
			throw new IllegalArgumentException("参数字符串:"+src+"不是ip");
		}
		byte[] addr = IPAddressUtil.textToNumericFormatV4(src);
		final byte b0 = addr[0];
	    final byte b1 = addr[1];
	    //10.x.x.x/8
	    final byte SECTION_1 = 0x0A;
	    //172.16.x.x/12
	    final byte SECTION_2 = (byte) 0xAC;
	    final byte SECTION_3 = (byte) 0x10;
	    final byte SECTION_4 = (byte) 0x1F;
	    //192.168.x.x/16
	    final byte SECTION_5 = (byte) 0xC0;
	    final byte SECTION_6 = (byte) 0xA8;
	    switch (b0) {
	        case SECTION_1:
	            return true;
	        case SECTION_2:
	            if (b1 >= SECTION_3 && b1 <= SECTION_4) {
	                return true;
	            }
	        case SECTION_5:
	            switch (b1) {
	                case SECTION_6:
	                    return true;
	            }
	        default:
	            return false;

	    }
	}
	
	/**
	 * 是否为ip
	 * @param src ip
	 * @return 是否为ip
	 */
	public static boolean isIp(String src) {
		if(StringUtils.isBlank(src)) {
			throw new IllegalArgumentException("参数字符串不能为空");
		}
		return PATTERN_IP.matcher(src).matches();
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
	 * @param src ip
	 * @return 是否为网段
	 */
	public static boolean isIpOrRange(String src) {
		return PATTERN_IPRANGE.matcher(src).matches();
	}
	
	public static boolean isIpV6(String src) {
		return P_IPV6_SINGLE.matcher(src).matches();
	}

	/**
	 * 是否是ipV6网段格式xx-xx
	 * @param src
	 * @return
	 */
	public static boolean isIpV6Segment(String src) {
		return P_IPV6_SEGMENT.matcher(src).matches();
	}
	
	/**
	 * 是否是ipV6带星号
	 * @param src
	 * @return
	 */
	public static boolean isIpV6Asterisk(String src) {
		return P_IPV6_ASTERISK.matcher(src).matches();
	}
	
	/**
	 * 是否是ipB6网段
	 * @param src
	 * @return
	 */
	public static boolean isIpV6Range(String src) {
		return isIpV6Segment(src) || isIpV6Asterisk(src);
	}
	
	/**
	 * 是否是ipV6单个或网段形式,不论是xx-xx形式还是星号形式
	 * @param src
	 * @return
	 */
	public static boolean isIpV6OrRange(String src) {
		return isIpV6(src) || isIpV6Range(src);
	}
	
	/**
	 * 拆分网段
	 * <p>
	 * 	先将*替换为各网段,1-3节的*替换为1-255,第四节替换为1-154
	 * 逐节拆分网段为一个个ip并进行拼接
	 * ipV6目前不支持包含ipV4的形式(如xx:xx:192.168.1.3-4)，网段位也只支持最后一个冒号后面(如支持xx:xx:*,但不支持xx:*:xx)
	 * 所有不支持拆分的网段均返回本身构成的列表,参数传null返回空列表
	 * </p>
	 * @param ip ip
	 * @return ip列表
	 */
	public static List<String> splitNetSegment(String ip) {
		if(ip == null) {
			return ListUtils.newArrayList();
		}
		if(! (isIpOrRange(ip) || isIpV6OrRange(ip)) || isIp(ip) || isIpV6(ip)) { //非ip格式或者为单个ip格式
			return ListUtils.of(ip);
		}
		String prefix = null;
		if(ip.contains(":")) {
			int index = ip.lastIndexOf(":");
			prefix = ip.substring(0, index+1);	//带冒号
			ip = ip.substring(index+1);
		}
		if(ip.contains(".")) {	//ipV4地址
			String[] group = ip.split("\\.");
			for(int i=0;i<group.length;i++) {
				if("*".equals(group[i])) {
					if(i<group.length-1) {
						group[i] = "1-255";
					} else {	//最后一位
						group[i] = "1-255";//"1-254";
					}
				}
			}
			List<String> resultList = splitItem(group, 0, null);
			if(prefix != null) {	//ipV6地址补充前缀
				for (int i = 0; i < resultList.size(); i++) {
					resultList.set(i, prefix+resultList.get(i));
				}
			}
			return resultList;
		} else {	//ipV6地址
			List<String> resultList = ListUtils.newArrayList();
			ip = ip.replace("*", "0000-ffff");
			String[] group = ip.split("-");
			int start = Integer.parseInt(group[0], 16);
			int end = Integer.parseInt(group[1], 16);
			for (;start <= end; start++) {
				resultList.add(prefix+Integer.toHexString(start));
			}
			return resultList;
		}
		
	}
	
	/**
	 * 拆分ip中的一节
	 * <p>
	 * 比如拆分1-10,得到{1,2,3,4,5,6,7,8,9,10}
	 * </p>
	 * @param groups ip用逗号拆分出的组
	 * @param index 初始数值
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
//		List<String> list = splitNetSegment("2001::1:1-2");//fe80:0000:0000:0000:0204:61ff:192.168.2-4.1
//		System.out.println(isIpV6OrRange("2001::1:0000-2"));
//		System.out.println(isIpRange("192.168.1-3.*"));
//		System.out.println(list.size());
//		for (String item : list) {
//			System.out.println(item);
//		}
//		System.out.println(isLan("172.32.0.1"));
		
		String[] ips = {
				"2001::1:*",
				"2001::1:0000-aaaa",
				"2001::1:1"
		};
		for (String ip : ips) {
			System.out.println(splitNetSegment(ip).size());
		}
	}
}
