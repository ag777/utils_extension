package com.ag777.util.remote.svn.exception;

/**
 * svn检出异常类
 * 
 * @author ag777
 * @version create on 2018年12月28日,last modify at 2018年12月28日
 */
public class SVNCheckoutException extends Exception {
	private static final long serialVersionUID = -1246632309743766304L;
	public SVNCheckoutException(String message) {super(message);}
	public SVNCheckoutException(String message, Exception e) {super(message, e);}
}
