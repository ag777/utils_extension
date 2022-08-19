package com.ag777.util.lang.security;

import com.ag777.util.lang.IOUtils;
import com.ag777.util.security.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA: 既能用于数据加密也能用于数字签名的算法
 * RSA算法原理如下：
    1.随机选择两个大质数p和q，p不等于q，计算N=pq
    2.选择一个大于1小于N的自然数e，e必须与(p-1)(q-1)互素
    3.用公式计算出d：d×e = 1 (mod (p-1)(q-1))
    4.销毁p和q
    5.最终得到的N和e就是“公钥”，d就是“私钥”，发送方使用N去加密数据，接收方只有使用d才能解开数据内容
    基于大数计算，比DES要慢上几倍，通常只能用于加密少量数据或者加密密钥
    私钥加解密都很耗时，服务器要求解密效率高，客户端私钥加密，服务器公钥解密比较好一点
 * 实现分段加密：
 *  RSA非对称加密内容长度有限制，1024位key的最多只能加密127位数据，
 *  否则就会报错(javax.crypto.IllegalBlockSizeException: Data must not be longer than 117 bytes)
 *  最近使用时却出现了“不正确的长度”的异常，研究发现是由于待加密的数据超长所致。
 * RSA 算法规定：
 *  待加密的字节数不能超过密钥的长度值除以 8 再减去 11（即：KeySize / 8 - 11），
 *  而加密后得到密文的字节数，正好是密钥的长度值除以 8（即：KeySize / 8）
 * Created by ag777 on 2022/8/19.
 */
public class RSAUtils {

	public static final String RSA = "RSA"; // 非对称加密密钥算法
//    /**
//     * android系统的RSA实现是"RSA/None/NoPadding"，而标准JDK实现是"RSA/None/PKCS1Padding" ，
//     * 这造成了在android机上加密后无法在服务器上解密的原因,所以android要和服务器相同即可。
//     */
//    public static final String ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding"; //加密填充方式
    public static final int DEFAULT_KEY_SIZE = 2048; //秘钥默认长度
    public static final byte[] DEFAULT_SPLIT = "#PART#".getBytes();    // 当要加密的内容超过bufferSize，则采用partSplit进行分块加密
    public static final int DEFAULT_BUFFERSIZE = (DEFAULT_KEY_SIZE / 8) - 11; // 当前秘钥支持加密的最大字节数


    private RSAUtils() {
        throw new UnsupportedOperationException("constrontor cannot be init");
    }

    /**
     * 随机生成RSA密钥对
     *
     * @param keyLength 密钥长度，范围：512～2048
     *                  一般1024
     *
     *  使用：
     *     KeyPair keyPair=RSAUtils.generateRSAKeyPair(RSAUtils.DEFAULT_KEY_SIZE);
           公钥
           RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
           私钥
           RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
     * @return 公私钥
     */
    public static KeyPair generateRSAKeyPair(int keyLength) {

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
            kpg.initialize(keyLength);
            return kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥对字符串进行加密
     * @param data 原文
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 得到公钥
        PublicKey key = getPublicKey(publicKey);
        // 数据加密
        return encrypt(data, key);
    }

    /**
     * 私钥加密
     *
     * @param data       待加密数据
     * @param privateKey 密钥
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 得到私钥
        PrivateKey key = getPrivateKey(privateKey);
        // 数据加密
        return encrypt(data, key);
    }

    /**
     * 公钥解密
     *
     * @param data      待解密数据
     * @param publicKey 密钥
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPublicKey(byte[] data, byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 得到公钥
        PublicKey key = getPublicKey(publicKey);
        // 数据解密
        return decrypt(data, key);
    }

    /**
     * 使用私钥进行解密
     */
    public static byte[] decryptByPrivateKey(byte[] data, byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 得到私钥
        PrivateKey key = getPrivateKey(privateKey);;
        // 数据解密
        return decrypt(data, key);
    }



    public static PublicKey getPublicKey(byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        return kf.generatePublic(keySpec);
    }

    public static PrivateKey getPrivateKey(byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        return kf.generatePrivate(keySpec);
    }

    public static byte[] encrypt(byte[] data, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return encrypt(data, key, 0);
    }

    public static byte[] decrypt(byte[] data, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return decrypt(data, key, 0);
    }

    /**
     * 分段加密
     * @param data 数据
     * @param key 秘钥
     * @param segmentSize  分段大小（小于等于0不分段）
     * @return 加密结果
     */
    public static byte[] encrypt(byte[] data, Key key, int segmentSize) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        // Cipher负责完成加密或解密工作，基于RSA
        Cipher cipher = Cipher.getInstance(RSA);
        // 根据公钥，对Cipher对象进行初始化
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] resultBytes;

        if (segmentSize > 0) {
            resultBytes = cipherDoFinal(cipher, data, segmentSize); //分段加密
        } else {
            resultBytes = cipher.doFinal(data);
        }

        return resultBytes;
    }

    /**
     * 分段解密
     * @param data 数据
     * @param key 秘钥
     * @param segmentSize  分段大小（小于等于0不分段）
     * @return 解密结果
     */
    public static byte[] decrypt(byte[] data, Key key, int segmentSize) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Cipher负责完成加密或解密工作，基于RSA
        Cipher deCipher = Cipher.getInstance(RSA);
        // 根据公钥，对Cipher对象进行初始化
        deCipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decBytes;//deCipher.doFinal(srcBytes);
        if (segmentSize > 0) {
            decBytes = cipherDoFinal(deCipher, data, segmentSize); //分段加密
        } else {
            decBytes = deCipher.doFinal(data);
        }

        return decBytes;
    }

    private static byte[] cipherDoFinal(Cipher cipher, byte[] srcBytes, int segmentSize) throws IllegalBlockSizeException, BadPaddingException {
        if (segmentSize <= 0) {
            throw new RuntimeException("分段大小必须大于0");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int inputLen = srcBytes.length;
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > segmentSize) {
                    cache = cipher.doFinal(srcBytes, offSet, segmentSize);
                } else {
                    cache = cipher.doFinal(srcBytes, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * segmentSize;
            }
            return out.toByteArray();
        } finally {
            IOUtils.close(out);
        }
    }
    
    public static void main(String[] args) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
		String text = "啊哈哈啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊";
        // 生成新的密匙对
        int keySize = 512;
        KeyPair pair = generateRSAKeyPair(keySize);
        assert pair != null;
        int segmentSizeEn = keySize / 8 - 11;
        int segmentSizeDe = keySize / 8;
        // 公钥加密 私钥解密
        byte[] en = encrypt(text.getBytes(), pair.getPublic(), segmentSizeEn);
		System.out.println(Base64Utils.encode2Str(en, StandardCharsets.UTF_8));
		byte[] de = decrypt(en, pair.getPrivate(), segmentSizeDe);
		System.out.println(new String(de));

        // 私钥加密 公钥解密
        en = encrypt(text.getBytes(), pair.getPrivate(), segmentSizeEn);
        System.out.println(Base64Utils.encode2Str(en, StandardCharsets.UTF_8));
        de = decrypt(en, pair.getPublic(), segmentSizeDe);
        System.out.println(new String(de));


	}
}
