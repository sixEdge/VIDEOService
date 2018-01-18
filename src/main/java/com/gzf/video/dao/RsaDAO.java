package com.gzf.video.dao;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.util.StringUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static javax.crypto.Cipher.DECRYPT_MODE;

@Bean
public class RsaDAO {

    private static final KeyPairGenerator KEY_PAIR_GENERATOR;

    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("RSA not support: " + e.getMessage());
        }
    }


    public static class RSAKeyPair {
        private final byte[] publicKeyStr;
        private final PrivateKey privateKey;

        private RSAKeyPair(final byte[] publicKeyStr, final PrivateKey privateKey) {
            this.publicKeyStr = publicKeyStr;
            this.privateKey = privateKey;
        }

        public byte[] getPublicKeyStr() {
            return publicKeyStr;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }
    }


    private RSAKeyPair generateKeyPair() {
        KeyPair keyPair = KEY_PAIR_GENERATOR.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKeyPair(
                StringUtil.base64Encode(rsaPublicKey.getEncoded()),
                rsaPrivateKey
        );
    }

    /*private static final int MAX_KEY_PAIR_NUMBER = 256;

    private static final Queue<RSAKeyPair> rsaKeyPairs = new ConcurrentLinkedQueue<>();

    private static final AtomicBoolean needGenerate = new AtomicBoolean(true);*/

    /**
     * Get a pair of rsa key. <br />
     * <s>Will generate MAX_KEY_PAIR_NUMBER key pairs when no key pair left.</s>
     */
    public RSAKeyPair getKeyPair() {
        return generateKeyPair();
        /*
        RSAKeyPair pair = rsaKeyPairs.poll();
        if (pair == null) {
            if (needGenerate.compareAndSet(true, false)) {
                AsyncTask.execute(() -> {
                    try {
                        for (int i = 0; i < MAX_KEY_PAIR_NUMBER; i++) {
                            rsaKeyPairs.add(generateKeyPair());
                        }
                    } finally {
                        needGenerate.set(true);
                    }
                });
            }
            return generateKeyPair();
        }
        return pair;*/
    }


    public byte[] decode(String encodedString, PrivateKey privateKey) {
        Cipher cipher;
        byte[] bs;

        try {
            cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            cipher.init(DECRYPT_MODE, privateKey);
            bs = cipher.doFinal(StringUtil.base64Decode(encodedString));
        } catch ( NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException e) {
            return null;
        }

        return bs;
    }
}
