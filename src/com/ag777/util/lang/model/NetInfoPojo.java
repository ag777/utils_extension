package com.ag777.util.lang.model;

/**
 * 网口信息存放类
 * <p>
 * com.ag777.util.lang.NetworkInterfaceUtils的辅助类
 * </p>
 * 
 * @author ag777
 * @version create on 2018年06月13日,last modify at 2018年06月13日
 */
public class NetInfoPojo {

	private String name;				//ethxx
	private String displayName;	//网卡名称
	private Boolean isUp;			//是否开启
	private String mac;				//mac地址
	private String ipV4;				//ipV4地址
	private String ipV6;				//ipV6地址
	private String mask;				//子网掩码
	
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
	public Boolean getIsUp() {
		return isUp;
	}
	public void setIsUp(Boolean isUp) {
		this.isUp = isUp;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getIpV4() {
		return ipV4;
	}
	public void setIpV4(String ipV4) {
		this.ipV4 = ipV4;
	}
	public String getIpV6() {
		return ipV6;
	}
	public void setIpV6(String ipV6) {
		this.ipV6 = ipV6;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
}
