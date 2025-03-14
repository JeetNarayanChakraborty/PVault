package com.example.PVault.service;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;



@Service
public class KeyManagementService 
{
	private final HttpSession session;

    public KeyManagementService(HttpSession session) 
    {
        this.session = session;
    }
	
	private SecretKey getUserMasterKey(String username, SecretKey keyToDecryptMasterKey)
	{
		String key = (String) session.getAttribute("AESEncyptionKeyForMasterKey");
		
		
		
		
		
		
		SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
		
		
		
		
		
		
		
		
		
	}
}
