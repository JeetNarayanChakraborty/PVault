package com.example.PVault.serviceTests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.HttpSession;
import com.example.PVault.service.KeyManagementService;
import com.example.PVault.service.passwordService;



@ExtendWith(MockitoExtension.class)
public class KeyManagementServiceTest 
{
    @Mock
    private HttpSession session;

    @Mock
    private passwordService passwordService;

    @InjectMocks
    private KeyManagementService keyManagementService;
    
    private String encryptAES(String plainText, String key) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Test
    public void testGetUserMasterKey() throws Exception 
    {
        String username = "user";
        String keyToDecryptMasterKey = Base64.getEncoder().encodeToString("1234567890123456".getBytes());
        String encryptedMasterKey = encryptAES("masterKey", "1234567890123456");

        when(session.getAttribute("AESEncryptionKeyForMasterKey")).thenReturn(keyToDecryptMasterKey);
        when(passwordService.getMasterKey(username)).thenReturn(encryptedMasterKey);

        String masterKey = keyManagementService.getUserMasterKey(username);

        assertEquals("masterKey", masterKey);
    }
}
