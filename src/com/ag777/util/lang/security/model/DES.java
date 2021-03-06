package com.ag777.util.lang.security.model;

/**
 * 加密算法 DES
 * @author ag777
 * @version create on 2018年06月29日,last modify at 2019年05月14日
 */
public class DES extends AlgorithmType {
	
	@Override
	public Integer keyLength() {
		return 8;
	}
	@Override
	public String transformation() {
		return "DES";
	}
	@Override
	public String algorithm() {
		return "DES";
	}
}
