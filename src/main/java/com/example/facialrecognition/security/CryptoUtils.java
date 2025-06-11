package com.example.facialrecognition.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class CryptoUtils {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256; // Taille de la clé en bits

    // Génère une nouvelle clé secrète
    public static SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            return keyGen.generateKey();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Erreur lors de la génération de la clé secrète", e);
        }
    }

    // Convertit une clé SecretKey en String Base64
    public static String encodeKey(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Convertit un String Base64 en SecretKey
    public static SecretKey decodeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    // Chiffre les données
    public static byte[] encrypt(byte[] data, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    // Déchiffre les données
    public static byte[] decrypt(byte[] encryptedData, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }
}