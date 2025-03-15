package com.example.PVault.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.PVault.entityClasses.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;



@Service
public class BackupAndRestoreService 
{
	@Autowired
	passwordService passwordService;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	KeyManagementService keyManagementService;

	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	
	public void backupUserSavedPasswords(String username) throws InvalidKeyException, IllegalBlockSizeException, 
																 BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKey secretKey = null;
		
		String masterkey = KeyManagementService.getUserMasterKey(username);  // Get Master Key for encryption and backup
		
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(masterkey.getBytes(StandardCharsets.UTF_8));
        secretKey = new SecretKeySpec(keyBytes, "AES");    // Derive AES key from master key
	
  
		List<Object[]> res = passwordService.getWebsiteAndPassword(username);
		HashMap<String, pwd> pwdDetails = new HashMap<String, pwd>();
		
		for(int i=0; i<res.size(); i++)
		{
			Object[] row = res.get(i);
			pwd temppwd = new pwd((String) row[1], (String) row[2]);
			pwdDetails.put((String) row[0], temppwd);
		}
		
		String encryptedData = null;
		
		try 
		{
			encryptedData = encryptMap(pwdDetails, secretKey);
		} 
		catch (Exception e) {}
        
        User user = userService.getUser(username);
        
        passwordService.addUserBackup(user.getId(), encryptedData); // Save backup to DB

        String userKey = Base64.getEncoder().encodeToString(secretKey.getEncoded()); // Convert SecretKey to String
        
        String toEmail = username;
        String mailSubject = "Master Key for password backup on " + java.time.LocalDate.now().toString();
        String mailBody = "Hi " + username 
  		      			   + "\n\n\n"
  		      			   + "Master Key :" + userKey
  		      			   + "\n\n"
  		      			   + "Please note down this key. This will be needed to restore backup if any accidental changes occours";
        
        try 
        {
			mailService.sendEmail(toEmail, mailSubject, mailBody);
		} 
        catch (MessagingException e) {}

        // Key Deletion from Server
        secretKey = null;
	}
	
	// Encrypt HashMap
	private static String encryptMap(HashMap<String, pwd> map, SecretKey key) throws Exception 
	{
        String json = objectMapper.writeValueAsString(map); // Convert to JSON
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(json.getBytes()));
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void restoreUserSavedPasswords(String username) throws InvalidKeyException, IllegalBlockSizeException, 
	                                                              BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		 //TODO
		
		SecretKey secretKey = null;
		
		String masterkey = KeyManagementService.getUserMasterKey(username);  // Get Master Key for encryption and backup
		
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(masterkey.getBytes(StandardCharsets.UTF_8));
        secretKey = new SecretKeySpec(keyBytes, "AES");    // Derive AES key from master key
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


    // Decrypt HashMap
	private static HashMap<String, pwd> decryptMap(String encryptedData, SecretKey key) throws Exception 
	{
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    String json = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData))); // Decrypt to JSON
	    return objectMapper.readValue(json, new TypeReference<HashMap<String, pwd>>() {}); // Convert JSON back to HashMap
	}
}





class AESUtils 
{
	public static SecretKey restoreSecretKey(String keyStr) 
	{
		byte[] decodedKey = Base64.getDecoder().decode(keyStr);
	    return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	}
}

class pwd
{
	String websiteName;
	String Password;
	
	public pwd(String websiteName, String Password)
	{
		this.websiteName = websiteName;
		this.Password = Password;
	}
	
	public String getWebsiteName() 
	{
		return websiteName;
	}
	public void setWebsiteName(String websiteName) 
	{
		this.websiteName = websiteName;
	}
	public String getPassword() 
	{
		return Password;
	}
	public void setPassword(String password) 
	{
		Password = password;
	}
}







