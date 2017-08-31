package com.ag777.util.lang;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * @Description 加解密工具
 * 需要jar包 commons-codec-1.10.jar
 * @author ag777
 * Time: last modify at 2017/8/31.
 * Mark: aes加解密方法和c++开发人员联调通过
 */
public class EncryptUtils {

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
		String result = (data.equals("")||data==null) ? null : decryptAesByBytes(base64Decode(new String(data.getBytes("ISO8859-1"),"UTF-8")), key);
		return RegexUtils.find(result, "([^\0]*)");
	}
	
	
	
	/**
     * aes加密,向量用0x00数组
     * @param content
     * @param encryptKey
     * @return
     * @throws Exception
     */
    public static byte[] encryptAesToBytes(String content, String encryptKey) throws Exception {  
    	Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");// 创建密码器
    	SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "AES");
    	cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
        byte[] result = cipher.doFinal(content.getBytes("utf-8"));
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
    	Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");// 创建密码器
    	SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "AES");
    	cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
        return new String(cipher.doFinal(encryptBytes), "UTF-8"); // 解密
    }  
    
    /*-------base64-----------*/
    /** 
     * base 64 encode 
     * @param bytes 待编码的byte[] 
     * @return 编码后的base 64 code 
     */  
    public static String base64Encode(byte[] bytes){  
        return Base64.encodeBase64String(bytes);  
    }  
      
    /** 
     * base 64 decode 
     * @param base64Code 待解码的base 64 code 
     * @return 解码后的byte[] 
     * @throws Exception 
     */  
    public static byte[] base64Decode(String base64Code) throws Exception{  
        return (base64Code==null||base64Code.equals("")) ? null : Base64.decodeBase64(base64Code);  
    }  
    
}
