package com.ag777.util.lang.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.ag777.util.lang.security.model.AES_CBC_NOPADDING;
import com.ag777.util.lang.security.model.AlgorithmType;

/**
 * 有关加解密的工具类
 * <p>
 * aes加解密方法和c++开发人员联调通过
 * 	需要jar包:
 * <ul>
 * <li>commons-codec-1.10.jar</li>
 * </ul>
 * 该包用于md5和sha1加密
 * </p>
 * 
 * @author ag777
 * @version create on 2018年06月29日,last modify at 2019年05月14日
 */
public class EncryptUtils {
	
	/*===========================加密===========================*/
	
	/**
	 * base64加密
	 * @param src
	 * @return
	 */
	 public static String base64(String src) {
		 if(src == null) {
			 return null;
		 }
		 return base64(src.getBytes(StandardCharsets.UTF_8));
	 }
	 
	/**
	 * base64编码
	 * 
	 * @param bytes
	 *            待编码的byte[]
	 * @return 编码后的base 64 code
	 */
	public static String base64(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}
	   
	 /**
     * MD5(从输入流) 16位加密
     * <p>
     * 返回字符串长度为16位
     * </p>
     * 
     * @param in 输入流,可以是文件的输入流
     * @return
	 * @throws IOException 
     */
    public static String md5_16(InputStream in) throws IOException  {
    	if(in == null) {
    		return null;
    	}
    	return md5_32(in).substring(8, 24);
    }
	
	/**
	 * MD5(从输入流) 32位加密
    * <p>
    * 返回字符串长度为32位
    * </p>
    * 
	 * @param in 输入流,可以是文件的输入流
	 * @throws IOException
	 */
	public static String md5_32(InputStream in) throws IOException {
		return DigestUtils.md5Hex(in);
	}
	
    /**
     * MD5 16位加密
     * <p>
     * 返回字符串长度为16位
     * </p>
     * 
     * @param src
     * @return
     */
    public static String md5_16(String src) {
    	if(src == null) {
    		return null;
    	}
    	return md5_32(src).substring(8, 24);
    }
    
   /**
    * MD5 32位加密
    * <p>
    * 返回字符串长度为32位
    * </p>
    * 
    * @param src
    * @return
    */
    public static String md5_32(String src) {
    	if(src == null) {
    		return null;
    	}
    	return DigestUtils.md5Hex(src.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * md5 32位加盐,二次加密
     * <p>
     * 返回字符串长度为32位
     * </p>
     * 
     * @param src
     * @return
     */
    public static String md5ByRL(String src){
    	if(src == null) {
    		return null;
    	}
    	if (src.length() != 32) {
    		src = md5_32(src);
		}
		StringBuilder sb = new StringBuilder();
		String[] array = new String[4];
		for (int i = 0, j = 0; j < 4; i += 8, j++) {
			String s = src.substring(i, i + 8);
			if (j % 2 == 1) {
				array[j] = s.toLowerCase();
			} else
				array[j] = s.toUpperCase();
		}
		for (int i = 0; i < 4; i++) {
			sb.append(array[4 - i - 1]);
		}
		return md5_32(sb.toString());
    }
    
    /**
     * sha1加密
     * <p>
     * 返回字符串长度为40位
     * </p>
     * 
     * @param src
     * @return
     */
    public static String sha1(String src) {
    	if(src == null) {
    		return null;
    	}
		return DigestUtils.sha1Hex(src.getBytes(StandardCharsets.UTF_8));
	}
    
    /**
     * 加密
     * @param src
     * @param key
     * @param algorithmType
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static String encrypt(String src, String key, AlgorithmType algorithmType) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    	return en(
    			algorithmType.preEn(src), 
    			key, 
    			algorithmType.transformation(), 
    			algorithmType.algorithm(), 
    			algorithmType.params(),
    			algorithmType.random());
    }
    
    /**
     * 加密
     * <p>
     * 基本流程:
     * ①先把需要加密的字符串转为字节数组
     * ②根据密匙key和算法algorithm生成对应的SecretKeySpec(暂成为"钥匙".下同)
     * ③根据transformation得到对应的Cipher(释义为暗号)
     * ④将②中得到的钥匙,algorithm,params设置到Cipher里
     * ⑤使用Cipher加密①得到的字节数组得到加密的字节数组
     * ⑥将⑤的结果通过base64加密转为字符串返回
     * </p>
     * 
     * @param src
     * @param key
     * @param transformation 用于获取Cipher,一般什么加密方法用是什么,比如"AES"
     * @param algorithm 美 [ˈælɡəˌrɪðəm] 具体的加密算法
     * @param params 参数,可以为向量
     * @param random 相当于随机数生成工具
     * @return
     * @throws InvalidKeyException 一般是密匙字节数不对抛出的异常
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static String en(String src, String key, String transformation, String algorithm, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    	if(src == null) {
    		return null;
    	}
    	return base64(
        		en2Byte(src, key, transformation, algorithm, params, random));
    }
    
    /**
     * 加密
     * @param src
     * @param key
     * @param transformation 用于获取Cipher,一般什么加密方法用是什么,比如"AES"
     * @param algorithm 美 [ˈælɡəˌrɪðəm] 具体的加密算法
     * @param params 参数,可以为向量
     * @param random 相当于随机数生成工具
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] en(byte[] src, byte[] key, String transformation, String algorithm, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    	if(src == null) {
    		return null;
    	}
    	return toByte(src, key, transformation, algorithm, Cipher.ENCRYPT_MODE, params, random);
    }
    
    /*===========================解密===========================*/
    
    /**
     * base64解密
     * @param src
     * @return
     */
    public static String deBase64(String src) {
    	if(src == null) {
    		return null;
    	}
        return bytes2Str(
        		Base64.decodeBase64(convertCharset(src)));
    }
    
    /**
     * base64解密
     * <p>
     * 结果为字节数组
     * </p>
     * 
     * @param src
     * @return
     */
    public static byte[] deBase64_2Bytes(String src) {
    	if(src == null) {
    		return null;
    	}
        return Base64.decodeBase64(src);  
    }
    
    /**
     * 解密
     * @param src
     * @param key
     * @param algorithmType
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */
    public static String decrypt(String src, String key, AlgorithmType algorithmType) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
    	return algorithmType.afterDe(
    			de(
	    			src, 
	    			key, 
	    			algorithmType.transformation(), 
	    			algorithmType.algorithm(), 
	    			algorithmType.params(),
	    			algorithmType.random()));
    }
    
    /**
     * 解密
     * @param src
     * @param key
     * @param transformation 用于获取Cipher,一般什么加密方法用是什么,比如"AES"
     * @param algorithm 美 [ˈælɡəˌrɪðəm] 具体的加密算法
     * @param params 参数,可以为向量
     * @param random 相当于随机数生成工具
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] decrypt(byte[] src, byte[] key, String transformation, String algorithm, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
    	if(src == null) {
    		return null;
    	}
    	return toByte(src, key, transformation, algorithm, Cipher.DECRYPT_MODE, params, random);
    }
    
    /**
     * 解密
     * <p>
     * 基本流程:
     * ①先把需要解密的字符串转换为utf-8格式new String(src.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)
     * ②对①进行base64解密
     * ③根据密匙key和算法algorithm生成对应的SecretKeySpec(暂成为"钥匙".下同)
     * ④根据transformation得到对应的Cipher(释义为暗号)
     * ⑤将③中得到的钥匙,algorithm,params设置到Cipher里
     * ⑥使用Cipher加密①得到的字节数组得到加密的字节数组
     * ⑦将⑥的结果直接转为字符串返回
     * </p>
     * 
     * @param src
     * @param key
     * @param transformation 用于获取Cipher,一般什么加密方法用是什么,比如"AES"
     * @param algorithm 美 [ˈælɡəˌrɪðəm] 具体的加密算法
     * @param params 参数,可以为向量
     * @param random 相当于随机数生成工具
     * @return
     * @throws InvalidKeyException 一般是密匙字节数不对抛出的异常
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */
    private static String de(String src, String key, String transformation, String algorithm, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
    	if(src == null) {
    		return null;
    	}
    	return bytes2Str(
    			de2Byte(src, key, transformation, algorithm, params, random));
    }
	
    /**
	 * 将字符串填充\0, 直到byte长度为16的倍数,可以用于在noPadding的基础上模拟ZeroPadding
	 * <p>
	 * 如:padding("123",16),则就是填充16-3个\0
	 * </p>
	 * @param src 源字符串
	 * @param blockLength 单位长度
	 * @return
	 */
	public static String padding(String src, int blockLength) {
		StringBuilder sb = new StringBuilder(src).append("\0");
		//字符串补到blockLength的整数倍
		int start = sb.toString().getBytes().length%blockLength;
		if(start != 0) {
			int last = blockLength-start;
			for(int i=0;i<last;i++) {
				sb.append("\0");
			}
		}
		return sb.toString();
	}
    
	/*=============内部方法==================*/
	/**
	 * 字符串编码转换iso_8859_1=>utf-8
	 * @param src
	 * @return
	 */
    private static String convertCharset(String src) {
    	return new String(src.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }
    
    /**
     * 字节数组直接转为字符串
     * @param bytes
     * @return
     */
    private static String bytes2Str(byte[] bytes) {
    	return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /**
     * 加密
     * @param src
     * @param key
     * @param transformation
     * @param algorithm
     * @param params
     * @param random
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    private static byte[] en2Byte(String src, String key, String transformation, String algorithm, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    	return toByte(src.getBytes(StandardCharsets.UTF_8), key.getBytes(), transformation, algorithm, Cipher.ENCRYPT_MODE, params, random);
    }
    
    /**
     * 解密
     * @param src
     * @param key
     * @param transformation
     * @param algorithm
     * @param params
     * @param random
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */
    private static byte[] de2Byte(String src, String key, String transformation, String algorithm, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
    	return toByte(
    			deBase64_2Bytes(convertCharset(src)), key.getBytes(), transformation, algorithm, Cipher.DECRYPT_MODE, params, random);
    }
    /**
     * 加密或解密
     * @param bytes
     * @param key
     * @param transformation
     * @param algorithm
     * @param opmode
     * @param params
     * @param random
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    private static byte[] toByte(byte[] bytes, byte[] key, String transformation, String algorithm, int opmode, AlgorithmParameterSpec params, SecureRandom random) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    	return toByte(bytes, getCipher(key, transformation, algorithm, opmode, params, random));
    }
	
    /**
     * 加密或解密
     * @param bytes
     * @param cipher
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private static byte[] toByte(byte[] bytes, Cipher cipher) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    	return cipher.doFinal(bytes);
    }
    
    /**
     * 获取密匙Cipher[saɪfɚ]
     * @param key
     * @param transformation
     * @param algorithm
     * @param opmode
     * @param params
     * @param random
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static Cipher getCipher(byte[] key, String transformation, String algorithm, int opmode, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
    	return getCipher(getSecretKeySpec(key, algorithm), transformation, opmode, params, random);
    }
    
    /**
     * 获取密匙Cipher[saɪfɚ]
     * @param key 秘钥，java.security.Key对象,可以是SecretKeySpec,PublicKey等等
     * @param transformation
     * @param opmode Cipher.ENCRYPT_MODE 或者 Cipher.DECRYPT_MODE
     * @param params 
     * @param random 
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static Cipher getCipher(Key key, String transformation, int opmode, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		Cipher c = Cipher.getInstance(transformation);
    	c.init(opmode, key, params, random);		//用密钥和一组算法参数初始化
    	return c;
    }
    
	private static  SecretKeySpec getSecretKeySpec(byte[] key, String algorithm) {
    	return new SecretKeySpec(key, algorithm);
    }
	
	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
		AlgorithmType type = new AES_CBC_NOPADDING();
		String key = "ssssaaa|ssssaaa|";
		String src = "哈哈嗝~233";
		
		System.out.println(
				decrypt(
						encrypt(src, key, type),
						key,
						type));
		System.out.println(md5_32("123123"));
		System.out.println(md5ByRL(src));
		System.out.println(sha1("sdsad").length());
	}
}
