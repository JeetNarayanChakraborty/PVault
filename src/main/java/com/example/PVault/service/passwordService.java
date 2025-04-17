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
		return PasswordRepository.findPasswordDetailsById(ID);
	}
    
    public List<Password> getAllPasswords(String username)
    {
    	return PasswordRepository.findAllPasswordsByUsername(username);  //Get user passwords
    }
    
    public void updatePassword(String ID, String UpdatedPassword)
    {
    	Password intendedWebsitePassword = PasswordRepository.findPasswordDetailsById(ID);  // Update Website password
    	intendedWebsitePassword.setPassword(UpdatedPassword);
    	PasswordRepository.save(intendedWebsitePassword);
    }

    public void deletePassword(String ID) 
    {
    	PasswordRepository.deleteById(ID); // Delete website's password by ID
    }
    
    public List<Object[]> getWebsiteAndPassword(String username)
    {
    	return PasswordRepository.findWebsiteAndPasswordByUsername(username);
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

	public Object getUserBackUp(long anyLong) 
	{
		return PasswordRepository.getUserBackUp(anyLong);
	}
}







