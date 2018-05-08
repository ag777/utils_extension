package com.ag777.util.lang;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加解密工具
 * <p>
 * aes加解密方法和c++开发人员联调通过
 * 	需要jar包:
 * <ul>
 * <li>commons-codec-1.10.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version last modify at 2017年05月08日
 */
public class EncryptUtils {

	private static final String AES_CBC_NOPADDING= "AES/CBC/NoPadding";
	private static final String AES= "AES";
	
	private EncryptUtils() {
	}
	
	/*-------------aes加解密---------------------------*/
	/**
	 * aes加密,增加\0作为字符串尾缀(经base64转换)
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encryptAes(String data, String key) throws Exception {
		StringBuilder sb = new StringBuilder(data).append("\0");
		//字符串补到16的整数倍
		int start = sb.toString().getBytes().length%16;
		if(start != 0) {
			int last = 16-start;
			for(int i=0;i<last;i++) {
				sb.append("\0");
			}
		}
		return base64Encode(encryptAesToBytes(sb.toString(), key));
	}
	
	/**
	 * aes解密,去除字符串尾缀\0(经base64转换)
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String decryptAes(String data, String key) throws Exception {
		if(StringUtils.isEmpty(data)) {
			return null;
		}
		String temp =decryptAesByBytes(base64Decode(data), key);
		return RegexUtils.find(temp, "([^\0]*)");
	}
	
	
	
	/**
     * aes加密,向量用0x00数组
     * @param content
     * @param encryptKey
     * @return
     * @throws Exception
     */
    public static byte[] encryptAesToBytes(String content, String encryptKey) throws Exception {  
    	Cipher cipher = Cipher.getInstance(AES_CBC_NOPADDING);// 创建密码器
    	SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), AES);
    	cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
        byte[] result = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return result; 
        
    }  
    
    
    /**
     * aes解密,向量用0x00数组
     * @param encryptBytes
     * @param decryptKey
     * @return
     * @throws Exception
     */
    public static String decryptAesByBytes(byte[] encryptBytes, String decryptKey) throws Exception {  
    	Cipher cipher = Cipher.getInstance(AES_CBC_NOPADDING);// 创建密码器
    	SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), AES);
    	cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
        return new String(cipher.doFinal(encryptBytes), StandardCharsets.UTF_8); // 解密
    }  
    
    /*-------base64-----------*/
    /**
     * base64加密(byte[]->String)
     * 
     * @param src
     * @return
     */
    public static String base64Encode(byte[] src){  
    	Base64.Encoder encoder = Base64.getEncoder();
        return new String(encoder.encode(src), StandardCharsets.UTF_8); 
    }  
      
    /** 
     * base64解密(String->byte[])
     * 
     * @param src 
     * @return 
     * @throws IllegalArgumentException 
     */  
    public static byte[] base64Decode(String src) throws IllegalArgumentException{  
    	Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);  
    }  
    
}
