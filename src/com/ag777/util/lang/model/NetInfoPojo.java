package com.ag777.util.lang.model;

import java.util.List;

/**
 * 网口信息存放类
 * <p>
 * com.ag777.util.lang.NetworkInterfaceUtils的辅助类
 * </p>
 * 
 * @author ag777
 * @version create on 2018年06月13日,last modify at 2026年04月15日
 */
public class NetInfoPojo {

	private String name; // ethxx
	private String displayName; // 网卡名称
	private Integer index; // 网卡索引
	private Boolean isUp; // 是否已经开启并运行
	private Boolean isLoopback; // 是否回环接口（用于本机自通信，如127.0.0.1，不经过物理网卡）
	private Boolean isVirtual; // 是否是虚拟接口
	private Boolean isPointToPoint; // 是否点对点接口（链路两端仅有两个节点，如PPP/隧道/VPN）
	private Boolean supportsMulticast; // 是否支持组播（一次发送可被一组主机同时接收）
	private Integer mtu; // 最大传输单元（MTU，Maximum Transmission Unit）：该网卡单个数据包可承载的最大字节数，不含二层帧头；常见值如以太网1500，过大可能分片、过小会增加包数量）
	private String mac; // mac地址
	private List<NetAddressPojo> addressList; // 地址列表（同一项内包含IP版本、掩码/前缀，避免并行列表错位）

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Boolean getIsUp() {
		return isUp;
	}

	public void setIsUp(Boolean isUp) {
		this.isUp = isUp;
	}

	public Boolean getIsLoopback() {
		return isLoopback;
	}

	public void setIsLoopback(Boolean isLoopback) {
		this.isLoopback = isLoopback;
	}

	public Boolean getIsVirtual() {
		return isVirtual;
	}

	public void setIsVirtual(Boolean isVirtual) {
		this.isVirtual = isVirtual;
	}

	public Boolean getIsPointToPoint() {
		return isPointToPoint;
	}

	public void setIsPointToPoint(Boolean isPointToPoint) {
		this.isPointToPoint = isPointToPoint;
	}

	public Boolean getSupportsMulticast() {
		return supportsMulticast;
	}

	public void setSupportsMulticast(Boolean supportsMulticast) {
		this.supportsMulticast = supportsMulticast;
	}

	public Integer getMtu() {
		return mtu;
	}

	public void setMtu(Integer mtu) {
		this.mtu = mtu;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public List<NetAddressPojo> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<NetAddressPojo> addressList) {
		this.addressList = addressList;
	}

	/**
	 * 网卡地址信息。
	 */
	public static class NetAddressPojo {
		private String ip; // IP地址
		private Integer ipVersion; // IP版本：4 或 6
		private Integer prefixLength; // 网络前缀长度，如 IPv4 的 24
		private String mask; // IPv4子网掩码，如 255.255.255.0；IPv6通常为空

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public Integer getIpVersion() {
			return ipVersion;
		}

		public void setIpVersion(Integer ipVersion) {
			this.ipVersion = ipVersion;
		}

		public Integer getPrefixLength() {
			return prefixLength;
		}

		public void setPrefixLength(Integer prefixLength) {
			this.prefixLength = prefixLength;
		}

		public String getMask() {
			return mask;
		}

		public void setMask(String mask) {
			this.mask = mask;
		}
	}
}
