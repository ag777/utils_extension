package com.ag777.util.lang.string;

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
 * @version create on 2018年02月27日,last modify at 2018年04月17日
 */
public class IpValidator {

	public static Pattern PATTERN_IP;	//单ip验证,不包含网段(严格版)
	public static Pattern PATTERN_IPRANGE;	//ip验证，包含网段(严格版)
	
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
	 * @param src
	 * @return
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
	 * @param src
	 * @return
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
//		List<String> list = splitNetSegment("192.168.1-3.*");
//		System.out.println(isIpRange("192.168.1-3.*"));
//		for (String item : list) {
//			System.out.println(item);
//		}
		System.out.println(isLan("172.32.0.1"));
	}
}
