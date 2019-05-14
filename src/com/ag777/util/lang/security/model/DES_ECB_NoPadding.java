package com.ag777.util.lang.security.model;

/**
 * 加密算法 DES/ECB/NoPadding
 * @author ag777
 * @version create on 2018年06月29日,last modify at 2019年05月14日
 */
public class DES_ECB_NoPadding extends AlgorithmType {
	
	@Override
	public Integer keyLength() {
		return 8;
	}
	@Override
	public String transformation() {
		return "DES/ECB/NoPadding";
	}
	@Override
	public String algorithm() {
		return "DES";
	}
}
