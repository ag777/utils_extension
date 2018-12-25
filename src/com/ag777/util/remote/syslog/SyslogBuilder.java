package com.ag777.util.remote.syslog;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.interf.Disposable;
import com.ag777.util.remote.syslog.model.SyslogException;


/**
 * java发送syslog的构建工具类
 * <p>
 * 依赖于SyslogUtils.java
 * </p>
 * 
 * @author ag777
 * @version create on 2018年11月29日,last modify at 2018年11月29日
 */
public class SyslogBuilder implements Disposable{

	private int facility;
	private int level;
	private String tag;
	
	public SyslogBuilder() {
		facility = SyslogUtils.SYSLOG_FACILITY_DEFAULT;
		level = SyslogUtils.LEVEL_EMERGENCY;
		tag = "";
	}
	
	public SyslogBuilder facility(int facility) {
		this.facility = facility;
		return this;
	}
	
	public SyslogBuilder level(int level) {
		this.level = level;
		return this;
	}
	
	public SyslogBuilder tag(String tag) {
		this.tag = StringUtils.emptyIfNull(tag);
		return this;
	}
	
	@Override
	public void dispose() {
		tag = null;
	}
	
	/**
	 * 通过udp的形式发送数据
	 * @param hostName
	 * @param port
	 * @param msg
	 * @throws UnknownHostException
	 * @throws SyslogException
	 */
	public void sendByUdp(String hostName, int port, String msg) throws UnknownHostException, SyslogException {
		InetAddress adress = InetAddress.getByName(hostName);
		SyslogUtils.sendByUdp(adress, port, facility, level, tag, msg, true);
	}

}
