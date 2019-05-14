package com.ag777.util.lang.security.model;

/**
 * 加密算法 DESede
 * @author ag777
 * @version create on 2018年06月29日,last modify at 2019年05月14日
 */
public class ThreeDES extends AlgorithmType{
	
	@Override
	public Integer keyLength() {
		return 24;
	}
	@Override
	public String transformation() {
		return "DESede";
	}
	@Override
	public String algorithm() {
		return "DESede";
	}
}
