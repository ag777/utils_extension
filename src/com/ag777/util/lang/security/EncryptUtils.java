package com.ag777.util.lang.security;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

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
 * @version create on 2018年06月29日,last modify at 2018年07月18日
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
     * MD5 16位加密
     * <p>
     * 返回字符串长度为16位
     * </p>
     * 
     * @param src
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String md5_16(String src) throws NoSuchAlgorithmException {
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
     * @param algorithm 具体的加密算法
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
     * @param transformation
     * @param algorithm
     * @param params
     * @param random
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
    	return toByte(src.getBytes(StandardCharsets.UTF_8), key, transformation, algorithm, Cipher.ENCRYPT_MODE, params, random);
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
    			deBase64_2Bytes(convertCharset(src)), key, transformation, algorithm, Cipher.DECRYPT_MODE, params, random);
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
    private static byte[] toByte(byte[] bytes, String key, String transformation, String algorithm, int opmode, AlgorithmParameterSpec params, SecureRandom random) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
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
    public static Cipher getCipher(String key, String transformation, String algorithm, int opmode, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
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
    
	private static  SecretKeySpec getSecretKeySpec(String key, String algorithm) {
    	return new SecretKeySpec(key.getBytes(), algorithm);
    }
	
	/*===================枚举================*/
	/**
	 * 加密算法类型
	 * @author ag777
	 *
	 */
	public enum AlgorithmType {
		AES {
			private Pattern p = Pattern.compile("\0.*$");
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
				StringBuilder sb = new StringBuilder(src).append("\0");
				//字符串补到16的整数倍
				int start = sb.toString().getBytes().length%16;
				if(start != 0) {
					int last = 16-start;
					for(int i=0;i<last;i++) {
						sb.append("\0");
					}
				}
				return sb.toString();
			}
			@Override
			public String afterDe(String result) {
				return p.matcher(result).replaceFirst("");
			}
			@Override
			public AlgorithmParameterSpec params() {
				return new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
			}
		},
		DES {
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
		},
		THREEDES {
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
		};
		/**
		 * 密码字节限制
		 * @return
		 */
		public Integer keyLength() {
			return null;
		}
		/**
		 * 使用算法
		 * @return
		 */
		public String transformation() {
			return null;
		}
		/**
		 * 实例化Cipher的参数
		 * @return
		 */
		public String algorithm() {
			return null;
		}
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
	
	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
		AlgorithmType type = AlgorithmType.AES;
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
