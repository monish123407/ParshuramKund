package com.parshuramKund.Util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AadharEncryptionUtil {

    private static final String ALGORITHM = "AES";
    // 16-byte key for AES-128. In production, this should come from a secure configuration.
    private static final String SECRET_KEY = "ParshuramMelaKey"; // Exactly 16 chars (128 bits)

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while encrypting Aadhaar number", e);
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while decrypting Aadhaar number", e);
        }
    }

    public static String mask(String aadharNumber) {
        if (aadharNumber == null || aadharNumber.isEmpty()) {
            return null;
        }
        // Normalize: remove spaces or hyphens if any
        String clean = aadharNumber.replaceAll("[\\s-]", "");
        if (clean.length() < 4) {
            return clean;
        }
        String lastFour = clean.substring(clean.length() - 4);
        return "XXXX-XXXX-" + lastFour;
    }
}
