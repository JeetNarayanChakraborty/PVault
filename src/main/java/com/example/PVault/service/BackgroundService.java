package com.example.PVault.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.PVault.entityClasses.User;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpSession;


@Service
public class BackgroundService 
{
	@Autowired
	private EntityManager entityManager;
	
	@Autowired
    private HttpSession session;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	BackupAndRestoreService backupAndRestoreService;
	
	
	public List<String> fetchData() 
	{
	    String getUsernames = "SELECT u.username " +
	    					  "FROM users u JOIN password_history ph " +
	    					  "ON u.password = ph.Password " + 
	    					  "WHERE ph.created_Time <= :timeThreshold";

	    Query query = entityManager.createNativeQuery(getUsernames);
	    query.setParameter("timeThreshold", LocalDateTime.now().minusSeconds(2592000));
	    
	    return query.getResultList();
	}
	
	@Scheduled(fixedRate = 86400000)                   //Runs every day
	public final void sendPasswordChangeReminder()
	{
		List<String> result = fetchData();
		
		for(String username : result) 
		{
		    String mailSubject = "PVault Password change due";
		    
		    String mailBody = "Hi " + username + "\n\n"
		    		         + "It's been more than 30 days since you changed your PVault login credentials\n"
		    		         + "Please change the credential in any of the below ways\n\n\n"
		    		         + "For Automatic change:- http://localhost:8080/main/loginPasswordAutoChange \n"
		    		         + "For Manual change:- coming soon :)";
		    
		    try 
		    {
				mailService.sendEmail(username, mailSubject, mailBody);
			} 
		    
		    catch(MessagingException e) 
		    {
				e.printStackTrace();
			}
		}
	}
	
	@Scheduled(fixedRate = 60000) // Runs every 7 days
    public void scheduleBackup() 
	{
		List<User> users = userService.getAllUsers();
			
		for(User u : users)
		{
	        try 
	        {
				backupAndRestoreService.backupUserSavedPasswords(u.getUsername());
			} 
	        
	        catch(InvalidKeyException e) {e.printStackTrace();} 
	        catch(IllegalBlockSizeException e) {e.printStackTrace();} 
	        catch(BadPaddingException e) {} 
	        catch(NoSuchAlgorithmException e) {e.printStackTrace();} 
	        catch(NoSuchPaddingException e) {e.printStackTrace();}
		}
    }
}


















