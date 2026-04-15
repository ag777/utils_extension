package com.ag777.util.lang;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.model.NetInfoPojo;
import com.ag777.util.lang.model.NetInfoPojo.NetAddressPojo;

/**
 * 网口信息获取工具类。
 * <p>
 * 主要用于获取本机各网络接口的基础信息（网卡名、状态、MAC、IP、掩码等）。
 * </p>
 * <p>
 * 注意：该类基于 Java 标准库 {@link NetworkInterface}，无法直接获取默认网关信息。
 * </p>
 *
 * @author ag777
 * @version create on 2018年06月13日,last modify at 2026年04月15日
 */
public class NetworkInterfaceUtils {

	/**
	 * 工具类不允许实例化。
	 */
	private NetworkInterfaceUtils() {
	}

	/**
	 * 获取网口信息列表（默认不包含 down 状态网卡）。
	 *
	 * @return 网口信息列表；当系统无可用网卡时返回空列表
	 * @throws SocketException 获取网卡信息失败时抛出
	 */
	public static List<NetInfoPojo> getNetInfoList() throws SocketException {
		return getNetInfoList(false);
	}

	/**
	 * 获取网口信息列表。
	 * <p>
	 * 结果中不包含回环网卡；当 {@code includeDown=false} 时，同时过滤掉 down 状态网卡。
	 * </p>
	 * <p>
	 * 仅收集有效 IPv4/IPv6 地址。若某网卡无任何有效 IP，则不会出现在结果中。
	 * </p>
	 *
	 * @param includeDown 是否包含 down 状态网卡
	 * @return 网口信息列表；当系统无可用网卡时返回空列表
	 * @throws SocketException 获取网卡信息失败时抛出
	 */
	public static List<NetInfoPojo> getNetInfoList(boolean includeDown) throws SocketException {
		List<NetInfoPojo> netInfoList = ListUtils.newArrayList();
		Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		if (allNetInterfaces == null) {
			return netInfoList;
		}
		while (allNetInterfaces.hasMoreElements()) {

			NetworkInterface netInterface = allNetInterfaces.nextElement();
			if (netInterface == null || netInterface.isLoopback()) {
				continue;
			}
			if (!includeDown && !netInterface.isUp()) {
				continue;
			}
			String ethName = netInterface.getName();

			NetInfoPojo ni = new NetInfoPojo();
			String mac = getMac(netInterface);
			ni.setName(ethName); // ethxx
			ni.setDisplayName(netInterface.getDisplayName()); // 网卡名称
			ni.setIndex(netInterface.getIndex()); // 网卡索引
			ni.setMac(mac);
			ni.setIsUp(netInterface.isUp()); // 是否已经开启并运行
			ni.setIsLoopback(netInterface.isLoopback()); // 是否回环接口
			ni.setIsVirtual(netInterface.isVirtual()); // 是否是虚拟接口
			ni.setIsPointToPoint(netInterface.isPointToPoint()); // 是否点对点接口
			ni.setSupportsMulticast(netInterface.supportsMulticast()); // 是否支持组播
			ni.setMtu(netInterface.getMTU()); // 最大传输单元

			List<NetAddressPojo> addressList = ListUtils.newArrayList();

			/* 获取IP地址及其前缀/掩码 */
			List<InterfaceAddress> interfaceAddresses = netInterface.getInterfaceAddresses();
			for (InterfaceAddress interfaceAddress : interfaceAddresses) {
				if (interfaceAddress == null) {
					continue;
				}
				InetAddress address = interfaceAddress.getAddress();
				if (address == null || address.isAnyLocalAddress() || address.isLoopbackAddress()) {
					continue;
				}
				NetAddressPojo netAddress = new NetAddressPojo();
				if (address instanceof Inet4Address) {
					String ip = address.getHostAddress();
					if (isBlank(ip)) {
						continue;
					}
					netAddress.setIp(ip);
					netAddress.setIpVersion(4);
					netAddress.setPrefixLength(getNetworkPrefixLength(interfaceAddress));
					netAddress.setMask(getMask(interfaceAddress));
					addressList.add(netAddress);
				} else if (address instanceof Inet6Address) {
					String ip = normalizeIpv6(address.getHostAddress());
					if (isBlank(ip)) {
						continue;
					}
					netAddress.setIp(ip);
					netAddress.setIpVersion(6);
					netAddress.setPrefixLength(getNetworkPrefixLength(interfaceAddress));
					addressList.add(netAddress);
				}

			}

			if (addressList.isEmpty()) {
				continue;
			}

			ni.setAddressList(addressList);
			netInfoList.add(ni);
		}

		return netInfoList;
	}

	/**
	 * 获取 MAC 地址列表（默认不包含 down 状态网卡）。
	 *
	 * @return MAC 地址列表（去重且保持发现顺序）
	 * @throws SocketException 获取网卡信息失败时抛出
	 */
	public static List<String> getMacList() throws SocketException {
		return getMacList(false);
	}

	/**
	 * 获取 MAC 地址列表。
	 * <p>
	 * 会过滤回环网卡；当 {@code includeDown=false} 时，同时过滤 down 状态网卡。
	 * </p>
	 *
	 * @param includeDown 是否包含 down 状态网卡
	 * @return MAC 地址列表（去重且保持发现顺序）
	 * @throws SocketException 获取网卡信息失败时抛出
	 */
	public static List<String> getMacList(boolean includeDown) throws SocketException {
		Set<String> macSet = new LinkedHashSet<String>();
		Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		if (allNetInterfaces == null) {
			return ListUtils.newArrayList();
		}
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = allNetInterfaces.nextElement();
			if (netInterface == null || netInterface.isLoopback()) {
				continue;
			}
			if (!includeDown && !netInterface.isUp()) {
				continue;
			}
			String mac = getMac(netInterface);
			if (!isBlank(mac)) {
				macSet.add(mac);
			}

		}
		List<String> list = ListUtils.newArrayList();
		list.addAll(macSet);
		return list;
	}

	/**
	 * 从接口地址对象中提取 IPv4 子网掩码。
	 *
	 * @param interfaceAddress 接口地址对象
	 * @return 子网掩码字符串（如 255.255.255.0）；若参数为空、非 IPv4 或前缀非法则返回 {@code null}
	 */
	public static String getMask(InterfaceAddress interfaceAddress) {
		if (interfaceAddress == null) {
			return null;
		}
		InetAddress address = interfaceAddress.getAddress();
		if (!(address instanceof Inet4Address)) {
			return null;
		}
		return getMask(getNetworkPrefixLength(interfaceAddress));
	}

	/**
	 * 获取指定网卡的 MAC 地址。
	 *
	 * @param networkInterface 网卡对象
	 * @return MAC 地址字符串（大写十六进制，以冒号分隔）；当网卡为空或无硬件地址时返回 {@code null}
	 * @throws SocketException 访问网卡硬件地址失败时抛出
	 */
	public static String getMac(NetworkInterface networkInterface) throws SocketException {
		if (networkInterface == null) {
			return null;
		}
		byte[] mac = networkInterface.getHardwareAddress();
		if (mac != null) {
			return getMac(mac);
		}
		return null;
	}

	/**
	 * 获取接口网络前缀长度。
	 *
	 * @param interfaceAddress 接口地址对象
	 * @return 网络前缀长度（IPv4 常见为 24，对应 255.255.255.0）
	 */
	private static int getNetworkPrefixLength(InterfaceAddress interfaceAddress) {
		return interfaceAddress.getNetworkPrefixLength();
	}

	/**
	 * 将前缀长度转换为 IPv4 子网掩码字符串。
	 *
	 * @param mask 前缀长度，范围 0~32
	 * @return 子网掩码字符串；当前缀长度不在合法范围时返回 {@code null}
	 */
	private static String getMask(int mask) {
		if (mask < 0 || mask > 32) {
			return null;
		}
		switch (mask) {
		case 0:
			return "0.0.0.0";
		case 8:
			return "255.0.0.0";
		case 16:
			return "255.255.0.0";
		case 24:
			return "255.255.255.0";
		case 32:
			return "255.255.255.255";
		default:
			int bits = 0xFFFFFFFF << (32 - mask);
			StringBuilder maskStr = new StringBuilder();
			for (int i = 3; i >= 0; i--) {
				int value = (bits >> (i * 8)) & 0xFF;
				maskStr.append(value);
				if (i > 0) {
					maskStr.append(".");
				}
			}
			return maskStr.toString();
		}

	}

	/**
	 * 将 MAC 字节数组转换为标准字符串格式。
	 *
	 * @param mac MAC 字节数组
	 * @return 大写十六进制 MAC 字符串（如 AA:BB:CC:DD:EE:FF）；当入参为空时返回 {@code null}
	 */
	public static String getMac(byte[] mac) {
		if (mac == null || mac.length == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			if (i > 0) {
				sb.append(':');
			}
			String hexStr = Integer.toHexString(mac[i] & 0xFF).toUpperCase();
			if (hexStr.length() < 2) {
				sb.append('0');
			}
			sb.append(hexStr);
		}
		return sb.toString();
	}

	/**
	 * 规范化 IPv6 字符串，去除 zone id（如 "%eth0"、"%12"）。
	 *
	 * @param ipV6 IPv6 字符串
	 * @return 规范化后的 IPv6；当入参为空白时原样返回
	 */
	private static String normalizeIpv6(String ipV6) {
		if (isBlank(ipV6)) {
			return ipV6;
		}
		int index = ipV6.indexOf('%');
		if (index > -1) {
			return ipV6.substring(0, index);
		}
		return ipV6;
	}

	/**
	 * 判断字符串是否为空白。
	 *
	 * @param str 待判断字符串
	 * @return {@code true} 表示 {@code null} 或仅包含空白字符；否则为 {@code false}
	 */
	private static boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}

	/**
	 * 示例入口。
	 *
	 * @param args 启动参数
	 * @throws SocketException 获取网卡信息失败时抛出
	 */
	public static void main(String[] args) throws SocketException {
		getNetInfoList();
	}
}
