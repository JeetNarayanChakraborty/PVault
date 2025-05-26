package com.example.PVault.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.example.PVault.entityClasses.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class passwordService 
{
	private final Logger log = LoggerFactory.getLogger(passwordService.class);
	
	@Autowired
	passwordRepository PasswordRepository;
	
	@Autowired
	Encryptor encryptor;
	
	@Async
	public CompletableFuture<Void> addPassword(Password password) 
	{
	    String executionID = UUID.randomUUID().toString();
	    int maxRetries = 3;
	    int retryCount = 0;
	    int[] backoffTimes = {1000, 2000, 4000}; // Backoff times in milliseconds
	    boolean success = false;
	    Exception lastException = null;

	    while(retryCount <= maxRetries && !success) 
	    {
	        try 
	        {
	            if(retryCount > 0) 
	            {
	                // Wait before retry
	                try 
	                {
	                    Thread.sleep(backoffTimes[retryCount - 1]);    
	                } 
	                
	                catch(InterruptedException ie) 
	                {
	                    Thread.currentThread().interrupt();
	                    return CompletableFuture.completedFuture(null);
	                }
	            }
	            
	            // Encrypt the password before saving
	            password.setPassword(encryptor.encrypt(password.getPassword()));
	            
	            // Attempt to save the password
	            PasswordRepository.save(password);
	            success = true;
	        } 
	        
	        catch(Exception e) 
	        {
	            retryCount++;
	            lastException = e;
	            
	            if(retryCount > maxRetries) 
	            {
	                break;
	            }
	        }
	    }
	    
	    if(success) 
	    {
	        log.info("Successfully saved password after {} attempts, execution: {}", 
	                retryCount, executionID);
	    } 
	    
	    else 
	    {
	        log.error("Failed to save password after {} attempts, execution: {}, error: {}", 
	                 retryCount, executionID, lastException.getMessage());
	    }
	    
	    return CompletableFuture.completedFuture(null);
	}
	
	
	public Password getPasswordByID(String ID)
	{
		Password password = PasswordRepository.findPasswordDetailsById(ID);
		password.setPassword(encryptor.decrypt(password.getPassword()));  // Decrypt password
		return password;  // Return decrypted password
	}
    
    public List<Password> getAllPasswords(String username)
    {
    	List<Password> passwords = PasswordRepository.findAllPasswordsByUsername(username);
    
    	for(Password password : passwords) 
		{
			password.setPassword(encryptor.decrypt(password.getPassword()));  // Decrypt each password
		}
    	
		return passwords;  // Return list of decrypted passwords
    }
    
    public void updatePassword(String ID, String UpdatedPassword)
    {
    	Password rawPassword = PasswordRepository.findPasswordDetailsById(ID);  // Get raw password
    	rawPassword.setPassword(encryptor.encrypt(UpdatedPassword));  // Decrypt password
    	PasswordRepository.save(rawPassword);
    }

    public void deletePassword(String ID) 
    {
    	PasswordRepository.deleteById(ID); // Delete website's password by ID
    }
    
    public List<Object[]> getWebsiteAndPassword(String username)
    {
    	List<Object[]> passwords = PasswordRepository.findWebsiteAndPasswordByUsername(username);
    	
    	for(Object[] passwordDetails : passwords)
    	{
    		passwordDetails[2] = encryptor.decrypt((String) passwordDetails[2]);  // Decrypt each password	
    	}
    	
    	return passwords;  // Return list of decrypted passwords
    }
    
    public void addUserBackup(String userID, String backup)
    {
    	PasswordRepository.addBackup(userID, backup);
    }
    
    public List<String> getUserBackUp(String userID)
    {
    	return PasswordRepository.getBackup(userID);
    }
    
    public void addMasterKey(String username, String masterKey)
    {
    	PasswordRepository.addMasterKey(username, masterKey);
    }
    
    public String getMasterKey(String username)
    {
    	return PasswordRepository.getMasterKeyByUsername(username);
    }
    
    public void addAESEncryptionKeyForMasterKey(String username, String key)
	{
		PasswordRepository.addAESEncryptionKeyForMasterKey(username, key);
	}
    
    public String getAESEncryptionKeyForMasterKey(String username)
	{
		return PasswordRepository.getAESEncryptionKeyForMasterKey(username);
	}
    
    public void deleteOldBackups(String userID)
    {
    	PasswordRepository.deleteOldBackupsBeyondLimit(userID);
    }
}







