package com.ag777.util.lang.security.model;

import java.security.spec.AlgorithmParameterSpec;
import java.util.regex.Pattern;

import javax.crypto.spec.IvParameterSpec;

import com.ag777.util.lang.security.EncryptUtils;

/**
 * 加密算法 AES/CBC/NoPadding
 * @author ag777
 * @version create on 2018年06月29日,last modify at 2019年05月14日
 */
public class AES_CBC_NOPADDING extends AlgorithmType{

	private static Pattern p = Pattern.compile("\0.*$");
	
	//向量
	private AlgorithmParameterSpec params;
	
	public AES_CBC_NOPADDING() {
		params = new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
	}
	public AES_CBC_NOPADDING(String iv) {
		params = new IvParameterSpec(iv.getBytes());
	}
	
	@Override
	public Integer keyLength() {
		return 16;
	}
	@Override
	public String transformation() {
		return "AES/CBC/NoPadding";
	}
	@Override
	public String algorithm() {
		return "AES";
	}
	@Override
	public String preEn(String src) {
		return EncryptUtils.padding(src, 16);
	}
	@Override
	public String afterDe(String result) {
		return p.matcher(result).replaceFirst("");
	}
	@Override
	public AlgorithmParameterSpec params() {
		return params;
	}
}
