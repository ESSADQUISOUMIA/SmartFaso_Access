package com.example.facialrecognition.service;

import com.example.facialrecognition.security.CryptoUtils;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

public class EncryptionService {

    private static final SecretKey SECRET_KEY = CryptoUtils.generateSecretKey(); // Générer une clé unique au démarrage ou la charger depuis un endroit sécurisé

    public byte[] encrypt(byte[] data) throws GeneralSecurityException {
        return CryptoUtils.encrypt(data, SECRET_KEY);
    }

    public byte[] decrypt(byte[] encryptedData) throws GeneralSecurityException {
        return CryptoUtils.decrypt(encryptedData, SECRET_KEY);
    }
}