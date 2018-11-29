package com.ag777.util.remote.syslog.model;

import java.lang.Exception;

/**
 *有关<code>syslog</code> 发送异常类
 * 
 * @author ag777
 * @version create on 2018年11月29日,last modify at 2018年11月29日
 */
public class SyslogException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3375273682251014006L;

	public SyslogException(String msg) {
		super(msg);
	}
}
