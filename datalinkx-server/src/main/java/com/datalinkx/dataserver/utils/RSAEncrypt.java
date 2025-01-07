package com.datalinkx.dataserver.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA非对称加解密实现
 * （使用公钥加密，使用私钥解密）
 */
@Slf4j
public class RSAEncrypt {


    // RSA最大解密密文大小
    private static final int MAX_DECRYPT_BLOCK = 128;
    /**
     * 公钥与私钥组
     */
    private static Map<String, Object> keyMap = new HashMap<>();

    private static String PUB_KEY = "pubKey";

    static {
        try {
            // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            // 初始化密钥对生成器，密钥大小为96-1024位
            keyPairGen.initialize(1024, new SecureRandom());
            // 生成一个密钥对，保存在keyPair中
            KeyPair keyPair = keyPairGen.generateKeyPair();
            // 得到公钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            // 得到私钥
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            String strPublicKey = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
            // 将公钥和私钥保存到Map
            keyMap.put(strPublicKey, privateKey);
            keyMap.put(PUB_KEY, strPublicKey);
        } catch (Exception e) {
            log.error(" RSAEncrypt init error ", e);
        }
    }


    public static String getPubKey() {
        return keyMap.get(PUB_KEY).toString();
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encrypt(String str, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.getDecoder().decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] encode = Base64.getEncoder().encode(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
        return new String(encode);
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 明文
     */
    private static String decrypt(String str, String privateKey) throws Exception {
        // 获取公钥
        byte[] decoded = Base64.getDecoder().decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(2, priKey);
        byte[] data = Base64.getDecoder().decode(str.getBytes("UTF-8"));

        // 返回UTF-8编码的解密信息
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * 128;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String(decryptedData, "UTF-8");
    }

    /**
     * RSA私钥解密
     *
     * @param str 加密字符串
     * @return 明文
     */
    public static String decrypt(String str) throws Exception {
        str = str.replaceAll(" ", "+");
        String strPublicKey = getPubKey();
        Object privatePair = keyMap.get(strPublicKey);
        RSAPrivateKey privateKey = (RSAPrivateKey) privatePair;
        String strPrivateKey = new String(Base64.getEncoder().encode((privateKey.getEncoded())));
        return decrypt(str, strPrivateKey);
    }

    public static void main(String[] args) throws Exception {

        //加密字符串
        String message = "admin";
        String strPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCY+YaU2zE6Z+7iUEN239fam0yewYGfD14v93j+nMMXdLcVYogcfW0osHkvd505De2PKuS2UkSpiAZ5sJVKJyx/ZBhI2sPiVc2Pale63LBxuhVhl0EsmJSSbOdgR6pjuMXnpI1KV5c9WkplI1o3gUhpIXjeMcYVosafkna69sOmJwIDAQAB";
        System.out.println("随机生成的公钥为:" + strPublicKey);

        String messageEn = encrypt(message, strPublicKey);


        System.out.println(message + "加密后的字符串为:" + messageEn);
//        String messageDe = decrypt(messageEn);
//        System.out.println("解密后的字符串为:" + messageDe);

    }
}

    