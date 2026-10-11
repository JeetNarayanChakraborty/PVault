package com.example.PVault.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;



@Service
public class Encryptor 
{
    @Value("${encrypt.key}")
    private String secretKey;

    private SecretKeySpec keySpec;

    @PostConstruct
    public void init() { keySpec = new SecretKeySpec(secretKey.getBytes(), "AES"); }


    public String encrypt(String plainText)
    {
        try
        {
            // Define standard GCM parameters (12-byte IV, 128-bit authentication tag)
            final int IV_LENGTH_BYTES = 12;
            final int TAG_LENGTH_BITS = 128;

            // Generate a highly secure, random Initialization Vector (IV)
            byte[] iv = new byte[IV_LENGTH_BYTES];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Initialize the cipher with the gold-standard AES/GCM/NoPadding
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            // Encrypt the plaintext string (explicitly specifying UTF_8 prevents platform bugs)
            byte[] encrypted = cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Pack the IV and Ciphertext together into a single byte array
            byte[] combined = ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            // Return as a clean Base64 string
            return Base64.getEncoder().encodeToString(combined);
        }

        catch(Exception e)
        {
            throw new RuntimeException("Encryption failed securely", e);
        }
    }

    public String decrypt(String cipherText)
    {
        try
        {
            // Define standard GCM parameters matching the encryption configuration
            final int IV_LENGTH_BYTES = 12;
            final int TAG_LENGTH_BITS = 128;

            // Decode the Base64 string back into bytes
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // Extract the IV from the first 12 bytes
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);

            // Extract the actual ciphertext from the remaining bytes
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            // Initialize the cipher with AES/GCM/NoPadding and the extracted IV
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            // Decrypt the text and enforce UTF-8 string reconstruction
            byte[] plainTextBytes = cipher.doFinal(encrypted);
            return new String(plainTextBytes, java.nio.charset.StandardCharsets.UTF_8);
        }

        catch(Exception e)
        {
            throw new RuntimeException("Decryption failed securely", e);
        }
    }
}



