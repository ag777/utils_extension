package com.ag777.util.remote.syslog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;
import org.joda.time.DateTime;

import com.ag777.util.lang.StringUtils;
import com.ag777.util.remote.syslog.model.SyslogException;


/**
 * java发送syslog的简单封装
 * <p>
 * sysLog大概格式: <facility|level>MMM dd HH:mm:ss tag:要发送的数据<br>
 * 如:<134>Jan  3 09:50:04 ndasec: 发送数据
 * </p>
 * 
 * @author ag777
 * @version create on 2018年11月29日,last modify at 2018年11月29日
 */
public class SyslogUtils {
	
	/**/
	public static final int FACILITY_KERN = 0;
	public static final int FACILITY_USER = 8;
	public static final int FACILITY_MAIL = 16;
	public static final int FACILITY_DAEMON = 24;
	public static final int FACILITY_AUTH = 32;
	public static final int FACILITY_SYSLOG = 40;
	public static final int FACILITY_LPR = 48;
	public static final int FACILITY_NEWS = 56;
	public static final int FACILITY_UUCP = 64;
	public static final int FACILITY_CRON = 72;
	public static final int FACILITY_AUTHPRIV = 80;
	public static final int FACILITY_FTP = 88;
	public static final int FACILITY_LOCAL0 = 128;
	public static final int FACILITY_LOCAL1 = 136;
	public static final int FACILITY_LOCAL2 = 144;
	public static final int FACILITY_LOCAL3 = 152;
	public static final int FACILITY_LOCAL4 = 160;
	public static final int FACILITY_LOCAL5 = 168;
	public static final int FACILITY_LOCAL6 = 176;
	public static final int FACILITY_LOCAL7 = 184;
	public static final int SYSLOG_FACILITY_DEFAULT = FACILITY_USER;

	/*消息等级*/
	public static final int LEVEL_DEBUG = 7;
	public static final int LEVEL_INFO = 6;
	public static final int LEVEL_NOTICE = 5;
	public static final int LEVEL_WARN = 4;
	public static final int LEVEL_ERROR = 3;
	public static final int LEVEL_CRITICAL = 2;
	public static final int LEVEL_ALERT = 1;
	public static final int LEVEL_EMERGENCY = 0;
	/*syslog默认接收端口*/
	public static final int PORT_DEFAULT = 514;
	
	private SyslogUtils() {}
	
//	public static void main(String[] args) throws UnknownHostException, SyslogException {
//		SyslogUtils.send(InetAddress.getByName("127.0.0.1"), 514, FACILITY_LOCAL0, LEVEL_INFO, "ndasec", "数据", true);
//	}
	
	/**
	 * 构建并发送数据(udp)
	 * @param addr ip可以通过InetAddress.getByName来获取
	 * @param port 默认端口号为514
	 * @param facility [fəˈsɪləti]设备;容易;能力;灵巧
	 * @param level 等级,对应到接收方的字段是Severity[sɪ'verətɪ] 严重;严格;严谨
	 * @param tag
	 * @param msg
	 * @param includeDate
	 * @throws SyslogException
	 */
	public static void sendByUdp(InetAddress addr, int port, int facility, int level, String tag, String msg, boolean includeDate) throws SyslogException {
		byte[] data = getData(facility, level, tag, msg, includeDate); 
		sendByUdp(addr, port, data);
	}
	
	/**
	 * 构建数据
	 * @param facility
	 * @param level
	 * @param tag
	 * @param msg
	 * @param includeDate
	 * @return
	 * @throws SyslogException
	 */
	public static byte[] getData(int facility, int level, String tag, String msg, boolean includeDate) throws SyslogException {
		/*拼接数据*/
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(facility|LEVEL_INFO).append('>');
		if (includeDate) {
			DateTime dt = new DateTime().dayOfYear().setCopy(3);
			if (dt.getDayOfMonth() < 10) {
				sb.append(
					dt.toString("MMM  d HH:mm:ss ", Locale.US));
			} else {
				sb.append(
					dt.toString("MMM dd HH:mm:ss ", Locale.US));
			}
				
		}
		sb.append(StringUtils.emptyIfNull(tag)).append(": ").append(msg);
		/*获取需要发送的字节流*/
		return sb.toString().getBytes(); 
	}
	
	/**
	 * 发送数据
	 * @param addr
	 * @param port
	 * @param data
	 * @throws SyslogException
	 */
	public static void sendByUdp(InetAddress addr, int port, byte[] data) throws SyslogException {
		/*构造数据包*/
		DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);

		try (	//建立连接并发送数据,DatagramSocket是AutoCloseable的实现类
			DatagramSocket socket = new DatagramSocket()
			){
			socket.send(packet);
		} catch (IOException ex) {
			throw new SyslogException("syslog error sending message: '" + ex.getMessage() + "'");
		}
	}
	
}
