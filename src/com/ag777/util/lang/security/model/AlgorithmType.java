package com.ag777.util.lang.security.model;

import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * 算法基础类
 * 
 * @author ag777
 * @version create on 2018年06月29日,last modify at 2019年05月14日
 */
public abstract class AlgorithmType {
	/**
	 * 密码字节限制
	 * @return
	 */
	public abstract Integer keyLength();
	/**
	 * 使用算法
	 * @return
	 */
	public abstract String transformation();
	/**
	 * 实例化Cipher的参数
	 * @return
	 */
	public abstract String algorithm();
	
	/**
	 * 算法参数,可以是向量
	 * @return
	 */
	public AlgorithmParameterSpec params() {
		return null;
	}
	/**
	 * 一个随机源,参与加密
	 * @return
	 */
	public SecureRandom random() {
		return null;
	}
	
	/**
	 * 加密前执行
	 * @param src 需要加密的串
	 * @return
	 */
	public String preEn(String src) {
		return src;
	}
	/**
	 * 解密后执行
	 * @param result 解密后的串
	 * @return
	 */
	public String afterDe(String result) {
		return result;
	}
}
