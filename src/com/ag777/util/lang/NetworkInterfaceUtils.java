package com.ag777.util.lang;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.model.NetInfoPojo;

/**
 * 网口信息获取类
 * 
 * @author ag777
 * @version create on 2018年06月13日,last modify at 2018年07月05日
 */
public class NetworkInterfaceUtils {

	private NetworkInterfaceUtils() {
	}

	/**
	 * 获取网口信息
	 * <p>
	 * 获取不到默认网关,查了一堆资料,可能只能通过执行命令来获取了
	 * </p>
	 * 
	 * @return
	 * @throws SocketException
	 */
	public static List<NetInfoPojo> getNetInfoList() throws SocketException {
		List<NetInfoPojo> netInfoList = ListUtils.newArrayList();
		Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		while (allNetInterfaces.hasMoreElements()) {
			
			NetworkInterface netInterface = allNetInterfaces.nextElement();
			String ethName = netInterface.getName();
			if(ethName.equals("lo")) {	//略过lo也就是127.0.0.1对应的地址
				continue;
			}
			NetInfoPojo ni = new NetInfoPojo();
			String mac = getMac(netInterface);
			ni.setName(ethName);		//ethxx
			ni.setDisplayName(netInterface.getDisplayName());	//网卡名称
			ni.setMac(mac);
			ni.setIsUp(netInterface.isUp());
			ni.setIsVirtual(netInterface.isVirtual());	//是否已经开启并运行
			/*获取ipV4,ipV6地址,和子网掩码*/
			List<InterfaceAddress> interfaceAddresses = netInterface.getInterfaceAddresses();
			for (InterfaceAddress interfaceAddress : interfaceAddresses) {

				InetAddress address = interfaceAddress.getAddress();
				if(address instanceof Inet4Address) {
					String mask = getMask(interfaceAddress);
					ni.setMask(mask);
					ni.setIpV4(address.getHostAddress());
				} else if(address instanceof Inet6Address) {
					ni.setIpV6(address.getHostAddress());
				}
				
			}
			netInfoList.add(ni);
		}
		
		return netInfoList;
	}
	
	/**
	 * 获取子网掩码
	 * @param interfaceAddress
	 * @return
	 */
	public static String getMask(InterfaceAddress interfaceAddress){
		return getMask(getNetworkPrefixLength(interfaceAddress));
	}
	
	/**
	 * 获取mac
	 * @param networkInterface
	 * @return
	 * @throws SocketException
	 */
	public static String getMac(NetworkInterface networkInterface) throws SocketException {
		byte[] mac = networkInterface.getHardwareAddress();
		if(mac != null) {
			return getMac(mac);
		}
		return null;
	}
	
	
	/**
	 * 获取子网掩码int类型
	 * @param interfaceAddress
	 * @return
	 */
	private static int getNetworkPrefixLength(InterfaceAddress interfaceAddress) {
		return interfaceAddress.getNetworkPrefixLength();
	}
	
	/**
	 * 转换int类型的子网掩码为字符串类型
	 * <p>
	 * 做了几个基本的判断增加运行效率
	 * </p>
	 * @param mask
	 * @return
	 */
	private static String getMask(int mask) {
		switch (mask) {
			case 8:
				return "255.0.0.0";
			case 16:
				return "255.255.0.0";
			case 24:
				return "255.255.255.0";
			case 32:
				return "255.255.255.255";
			default:
				mask = (-1 >> (31 - (mask - 1))) << (31 - (mask - 1));
				StringBuilder maskStr = new StringBuilder();
				byte[] maskIp = new byte[4];
				for (int i = 0; i < maskIp.length; i++) {
					maskIp[i] = (byte) (mask >> (maskIp.length - 1 - i) * 8);
					maskStr.append((maskIp[i] & 0xff));
					if (i < maskIp.length - 1) {
						maskStr.append(".");
					}
				}
				return maskStr.toString();
		}

	}
	
	/**
	 * 转化字节数组的mac为字符串类型
	 * @param mac
	 * @return
	 */
	public static String getMac(byte[] mac) {
		StringBuilder sb = null;
		for (int i = 0; i < mac.length; i++) {
			if(sb == null) {
				sb = new StringBuilder();
			} else {
				sb.append(':');
			}
			byte b = mac[i];
			int intValue = 0;
			if (b >= 0)
				intValue = b;
			else
				intValue = 256 + b;
			String hexStr = Integer.toHexString(intValue);
			if(hexStr.length() < 2) {
				sb.append("0");
			}
			sb.append(hexStr.toUpperCase());
		}
		return sb!=null?sb.toString():null;
	}
	
	public static void main(String[] args) throws SocketException {
		getNetInfoList();
	}
}
