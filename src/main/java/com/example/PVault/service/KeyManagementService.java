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
	private static HttpSession session;
	static passwordService passwordService;
	

    public KeyManagementService(HttpSession session) 
    {
        this.session = session;
    }
	
	public static String getUserMasterKey(String username) throws InvalidKeyException, IllegalBlockSizeException, 
	                                                        BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		String keytoDecryptMasterKey = (String) session.getAttribute("AESEncyptionKeyForMasterKey");
		String encryptedMasterKey = passwordService.getMasterKey(username);
		
		SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(keytoDecryptMasterKey), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMasterKey));
        return new String(decryptedBytes);
	}
}
