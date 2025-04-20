package com.example.PVault.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Service
public class Encryptor 
{

    @Value("${encrypt.key}")
    private String secretKey;

    private SecretKeySpec keySpec;

    
    @PostConstruct
    public void init() 
    {
        keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
    }

    public String encrypt(String plainText) 
    {
        try 
        {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } 
        
        catch(Exception e) {throw new RuntimeException("Encryption failed", e);}
    }

    public String decrypt(String cipherText) 
    {
        try 
        {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded));
        } 
        catch(Exception e) {throw new RuntimeException("Decryption failed", e);}
    }
}








