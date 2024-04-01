package com.ag777.util.lang.exception.model;

/**
 * 图片格式不支持异常
 * <p>
 * 通常是因为加载了一个不是图片的文件而抛出
 * </p>
 * 
 * @author ag777
 * @version create on 2018年05月08日,last modify at 2018年05月08日
 */
public class ImageNotSupportException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8699087188610146686L;

	public ImageNotSupportException(String errMsg) {
		super(errMsg);
	}
	
}
