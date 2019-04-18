package com.ag777.util.lang.string.ip;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ag777.util.lang.Console;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;

/**
 * ip地址验证辅助类
 * <p>
 *  ip列表拆分,整合成网段的工具
 * </p>
 * 
 * @author ag777
 * @version create on 2019年04月09日,last modify at 2019年04月17日
 */
public class IpSplitUtils {

	private IpSplitUtils() {}
	
	/**
	 * 根据每个列表的最大限制数,拆分ip列表，并尽可能整合成网段的表示方式
	 * @param ips ip数组，每一项ip可以是个网段
	 * @param excludeIpList 排除ip列表，每项都可以是个网段
	 * @param limit 每个ip列表的最大限制数
	 * @return ip(网段)列表
	 */
	public static List<String> split(String[] ips, List<String> excludeIpList,int limit) {
		return split(Arrays.stream(ips), excludeIpList, limit);
	}
	
	/**
	 * 根据每个列表的最大限制数,拆分ip列表，并尽可能整合成网段的表示方式
	 * @param ipList ip列表，每一项ip可以是个网段
	 * @param excludeIpList 排除ip列表，每项都可以是个网段
	 * @param limit 每个ip列表的最大限制数
	 * @return ip(网段)列表
	 */
	public static List<String> split(List<String> ipList, List<String> excludeIpList, int limit) {
		return split(ipList.stream(), excludeIpList, limit);
	}
	
	/**
	 * 根据每个列表的最大限制数,拆分ip列表，并尽可能整合成网段的表示方式
	 * @param ips ip流，每一项ip可以是个网段
	 * @param excludeIpList 排除ip列表，每项都可以是个网段
	 * @param limit 每个ip列表的最大限制数
	 * @return ip(网段)列表
	 */
	public static List<String> split(Stream<String> ips, List<String> excludeIpList, int limit) {
		List<String> ipV6List = ListUtils.newArrayList();
		List<String> ipList = ips
			.filter(ip->{	//过滤ipv4以外的地址
				boolean isV4 = IpValidator.isIpRange(ip);
				if(!isV4) {
					ipV6List.add(ip);
				}
				
				return isV4;
			})
			.map(IpValidator::splitNetSegment)	//每一项转化为单个ip列表
			.flatMap(List::stream)	//扁平化
			.distinct()	//排重
			.sorted(Comparator.comparingLong(ip->StringUtils.toLong(ip.replace(".", ""))))	//排序，从小到大
			.collect(Collectors.toList());

		/*其余的ip，比如ipv6，都认为是单个ip单独加在列表末尾*/
		for (String ipV6 : ipV6List) {
			if(!ipList.contains(ipV6)) {	//排重添加
				ipList.add(ipV6);
			}
		}
		
		/*清空临时列表的数据,这步可以不做*/
		ipV6List.clear();
		
		if(!ListUtils.isEmpty(excludeIpList)) {
			ipList.removeAll(
					excludeIpList.stream()
						.map(ip->IpValidator.isIpRange(ip)?IpValidator.splitNetSegment(ip):ListUtils.of(ip))
						.flatMap(List::stream)
						.distinct()
						.collect(Collectors.toList()));
		}
		
		return ListUtils.splitList(ipList, limit).stream()	//根据需求分割列表
			.map(IpSplitUtils::integration)	//整合列表项，将可以合并成网段的ip进行合并
			.collect(Collectors.toList());
	}
	
	
	/**
	 *将ip列表尽量整合成网段的形式
	 * @param ips ip(单个)列表
	 * @return ip(网段列表)
	 */
	private static String integration(List<String> ips) {
		List<String> ipV6s = ListUtils.newArrayList();
		Iterator<String> itor = ips.iterator();
		while(itor.hasNext()) {
			String ip = itor.next();
			if(!IpValidator.isIpRange(ip)) {
				ipV6s.add(ip);
				itor.remove();
			}
		}
		
		
		List<String> resultList = ListUtils.newArrayList();
		String[] temp = ips.get(0).split("\\.");
		String temp0 = StringUtils.concat(temp[0],'.',temp[1],'.',temp[2], '.');
		int start = StringUtils.toInt(temp[3]);
		int end = start;
		for(int i=1; i<ips.size(); i++) {
			String curIp = ips.get(i);
			if(curIp.startsWith(temp0)) {	//和上一个ip处于同一网段,需要进行最后一位对比，如果相差1则后期格式化为网段
				int cur = StringUtils.toInt(curIp.replace(temp0, ""));
				if(cur-end == 1) {
					end = cur;
				} else {	//说明在同一个网段，但是需要重新做一个网段划分
					resultList.add(integration(temp0, start, end));
					start = cur;
					end = cur;
				}
			} else {	//和上一个ip处于不同网段,当前网段格式化，并进度下一个网段的统计
				resultList.add(integration(temp0, start, end));
				/*重新进行计算*/
				temp = curIp.split("\\.");
				temp0 = StringUtils.concat(temp[0],'.',temp[1],'.',temp[2], '.');
				start = StringUtils.toInt(temp[3]);
				end = start;
			}
		}
		resultList.add(integration(temp0, start, end));
		resultList.addAll(ipV6s);
		return ListUtils.toString(resultList, ",");
	}
	
	/**
	 * 将网段和开始/结束值整合成一个网段,如integration("192.168.162.", 1, 10)=>"192.168.162.1-10"
	 * @param temp1 网段,如192.168.162.
	 * @param start 开始值
	 * @param end 结束值
	 * @return 一个ip(网段)
	 */
	private static String integration(String temp1, int start, int end) {
		if(start==end) {
			return temp1+start;
		}
		return StringUtils.concat(temp1, start, '-', end);
	}
	
	public static void main(String[] args) {
		Console.prettyLog(
				split("192.168.162.1-9,192.168.161.*,192.168.1.2,2001::1:1/64".split(","),ListUtils.of("192.168.162.8", "2001::1:1/64"), 200)
				);
		
	}
}
