package com.gzf.video.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static javax.crypto.Cipher.DECRYPT_MODE;

public class RSASecurityService {

    private static final BASE64Encoder BASE_64_ENCODER = new BASE64Encoder();
    private static final BASE64Decoder BASE_64_DECODER = new BASE64Decoder();

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
        private final String publicKeyStr;
        private final PrivateKey privateKey;

        private RSAKeyPair(final String publicKeyStr, final PrivateKey privateKey) {
            this.publicKeyStr = publicKeyStr;
            this.privateKey = privateKey;
        }

        public String getPublicKeyStr() {
            return publicKeyStr;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }
    }


    public RSAKeyPair doGenerateKeyPair() {
        KeyPair keyPair = KEY_PAIR_GENERATOR.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKeyPair(
                BASE_64_ENCODER.encode(rsaPublicKey.getEncoded()),
                rsaPrivateKey
        );
    }

    public String doDecode(String encodedString, PrivateKey privateKey) throws IOException {
        Cipher cipher;
        byte[] bs;

        try {
            cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            cipher.init(DECRYPT_MODE, privateKey);
            bs = cipher.doFinal(BASE_64_DECODER.decodeBuffer(encodedString));
        } catch ( NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException e) {
            return null;
        }

        return new String(bs).trim();
    }


    private static final RSASecurityService INSTANCE = new RSASecurityService();

    public static RSASecurityService getINSTANCE() {
        return INSTANCE;
    }

    private RSASecurityService() {}
}
