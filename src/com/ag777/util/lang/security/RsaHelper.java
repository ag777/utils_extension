package com.ag777.util.lang.security;

import com.ag777.util.lang.model.Pair;
import com.ag777.util.security.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * 非对称加密辅助类
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/8/19 11:42
 */
public class RsaHelper {

    private final Charset charset;
    private final Key key;
    private final int keySize;

    /**
     *
     * @param key 密匙
     * @param keySize 密匙长度
     * @param charset 文字编码
     */
    public RsaHelper(Key key, int keySize, Charset charset) {
        this.key = key;
        this.charset = charset;
        this.keySize = keySize;
    }

    public static RsaHelper newInstance(KeyPair pair, int keySize, boolean isPublic, Charset charset) {
        if (isPublic) {
            return new RsaHelper(pair.getPublic(), keySize, charset);
        } else {
            return new RsaHelper(pair.getPrivate(), keySize, charset);
        }
    }

    public static RsaHelper newInstance(String key, int keySize, boolean isPublic, Charset charset) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] kb = Base64Utils.decode(key.getBytes(charset));
        Key k = isPublic?RSAUtils.getPublicKey(kb):RSAUtils.getPrivateKey(kb);
        return new RsaHelper(k, keySize, charset);
    }

    public String encode2Str(String text) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        byte[] src = text.getBytes(charset);
        byte[] result = RSAUtils.encrypt(src, key, keySize / 8 -11);
        return Base64Utils.encode2Str(result, charset);
    }

    public String decode2Str(String text) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        byte[] src = Base64Utils.decode(text.getBytes(charset));
        byte[] result = RSAUtils.decrypt(src, key, keySize / 8);
        return new String(result, charset);
    }

    /**
     * 创建base64后的公私钥
     * @param keyLength 密钥长度，范围：512～2048, 一般1024
     * @return 公钥，私钥
     */
    public static Pair<String, String> newKeyPair(int keyLength, Charset charset) {
        KeyPair pair = RSAUtils.generateRSAKeyPair(keyLength);
        assert pair != null;
        String publicKey = getPublicKeyStr(pair, charset);
        String privateKey = getPrivateKeyStr(pair, charset);
        return new Pair<>(publicKey, privateKey);
    }


    public static String getPublicKeyStr(KeyPair pair, Charset charset) {
        return Base64Utils.encode2Str(pair.getPublic().getEncoded(), charset);
    }

    public static String getPrivateKeyStr(KeyPair pair, Charset charset) {
        return Base64Utils.encode2Str(pair.getPrivate().getEncoded(), charset);
    }

    public static void main(String[] args) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Charset charset = StandardCharsets.UTF_8;
        String text = "撒旦教爱丽丝到家撒旦教撒旦教爱丽丝到家撒旦教爱丽丝到家啦是可敬的撒旦教爱丽丝到家啦是可敬的撒旦教爱丽丝到家啦是可敬的啦是可敬的撒旦教爱丽丝到家撒旦教爱丽丝到家啦是可敬的撒旦教爱丽丝到家啦是可敬的撒旦教爱丽丝到家啦是可敬的啦是可敬的撒旦教爱丽丝到家撒旦教爱丽丝到家啦是可敬的撒旦教爱丽丝到家啦是可敬的撒旦教爱丽丝到家啦是可敬的啦是可敬的爱丽丝到家啦是可敬的撒旦教爱丽丝到家啦是可敬的撒旦教爱丽丝到家啦是可敬的啦是可敬的";
        int keySize = 512;
        Pair<String, String> pair = newKeyPair(keySize, charset);
        RsaHelper hPublic = newInstance(pair.first, keySize, true, charset);
        RsaHelper hPrivate= newInstance(pair.second, keySize, false, charset);
        // 公钥加密 私钥解密
        String en = hPublic.encode2Str(text);
        String de = hPrivate.decode2Str(en);
        System.out.println(de);

        // 私钥加密 公钥解密
        en = hPrivate.encode2Str(text);
        de = hPublic.decode2Str(en);
        System.out.println(de);
    }
}
