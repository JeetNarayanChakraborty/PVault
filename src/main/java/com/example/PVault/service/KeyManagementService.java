package com.example.PVault.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;



@Service
public class KeyManagementService 
{
	static passwordService passwordService;
	

	public KeyManagementService(passwordService passwordService) 
	{
	    this.passwordService = passwordService;
	}
	
	public String getUserMasterKey(String username) throws InvalidKeyException, IllegalBlockSizeException, 
	                                                        BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		String keytoDecryptMasterKey = passwordService.getAESEncryptionKeyForMasterKey(username);
		String encryptedMasterKey = passwordService.getMasterKey(username);
		
		if(keytoDecryptMasterKey == null) 
		{
	        throw new IllegalStateException("key to decrypt master key is missing for user: " + username);
	    }
		
		if(encryptedMasterKey == null)
		{
	        throw new IllegalStateException("Encrypted master key is missing for user: " + username);
	    }
		
		
		
		
		
		SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(keytoDecryptMasterKey), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMasterKey));
        return new String(decryptedBytes);
	}
}




