package com.example.PVault;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.PVault.entityClasses.Password;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.BackupAndRestoreService;
import com.example.PVault.service.MailService;
import com.example.PVault.service.UserService;
import com.example.PVault.service.passwordService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import security.AuthenticationService;
import security.OTPService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;



@Controller
@RequestMapping("/main")
public class MainController 
{
	@Autowired
	UserService userService;
	
	@Autowired
	passwordService passwordService;
	
	@Autowired
	AuthenticationService authService;
	
	@Autowired
	OTPService otpService;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	BackupAndRestoreService backupAndRestoreService;
	
	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	
	
	
	@GetMapping("/getHome")
	public String getHome(HttpServletRequest request)
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) 
		{
			return "mainPage";
		}
		
		return "homePage";
	}
	
	@GetMapping("/getRegistration")
	public String getRegistration()
	{
		return "registration";
	}
	
	@GetMapping("/getAddPassword")
	public String getAddPassword()
	{
		return "addPassword";
	}
	
	@GetMapping("/getMainPage")
	public String getMainPage()
	{
		return "mainPage";
	}
	
	@CircuitBreaker(name = "userRegistrationCB", fallbackMethod = "userRegistrationFallback")
	@KafkaListener(topics = "user-registration-event", groupId = "user-group")
	public void userRegistration(User registrationFormData, HttpServletRequest request)
	{
		//check received event
		System.out.println("Received user details from user event producer: " + registrationFormData);
		
		//Take password input
		String inputPassword = registrationFormData.getPassword();
		
		//Encrypt password
		String hashedPassword = authService.encrypt(inputPassword);
		registrationFormData.setPassword(hashedPassword);
		
		request.getSession().setAttribute("username", registrationFormData.getUsername());
		request.getSession().setAttribute("UserID", registrationFormData.getId());
			
		//Add to Database
		userService.addUser(registrationFormData);
	}
	
	public ResponseEntity<String> userRegistrationFallback(User registrationFormData, 
			                                               HttpServletRequest request, Throwable t) 
	{
	    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
	    		body("Service is temporarily unavailable. Please try again later. :)");
	}
	
	@CircuitBreaker(name = "generateMasterKeyCB", fallbackMethod = "generateMasterKeyFallback")
	@KafkaListener(topics = "generate-master-key-event", groupId = "user-group")
	public void generateMasterKey(HttpServletRequest request, String username) throws NoSuchAlgorithmException, 
	                                                                                  NoSuchPaddingException, InvalidKeyException, 
	                                                                                  IllegalBlockSizeException, BadPaddingException
	{
		String executionId = UUID.randomUUID().toString();
	    
	    int maxRetries = 3;
	    int retryCount = 0;
	    int[] backoffTimes = {1000, 2000, 4000}; 
	    boolean success = false;
	    Exception lastException = null;
	    
	    log.info("Starting operation: {}", executionId);

	    while(retryCount <= maxRetries && !success) 
	    {
	        try 
	        {
	            if(retryCount > 0) 
	            {
	                try 
	                {
	                    Thread.sleep(backoffTimes[retryCount - 1]);
	                    log.info("Retry attempt {}, execution: {}", retryCount, executionId);
	                } 
	                
	                catch(InterruptedException ie) 
	                {
	                    Thread.currentThread().interrupt();
	                    log.warn("Operation interrupted: {}", executionId);
	                }
	            }
	            
	            request.getSession().setAttribute("username", username);
	    		String masterKey = UUID.randomUUID().toString().substring(0, 16); // Generate Master key
	    		
	    		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
	            keyGen.init(256);
	            SecretKey secretKey = keyGen.generateKey();          //Create AES key to encrypt master key
	            String AESEncyptionKeyForMasterKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
	    		
	            request.getSession().setAttribute("AESEncyptionKeyForMasterKey", AESEncyptionKeyForMasterKey); //Save the AES key for master key in session
	    		
	            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(AESEncyptionKeyForMasterKey), "AES");
	            Cipher cipher = Cipher.getInstance("AES");
	            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);              // Encrypt Master key using AES 
	            byte[] encryptedBytes = cipher.doFinal(masterKey.getBytes());
	            String encryptedMasterKey = Base64.getEncoder().encodeToString(encryptedBytes);
	    		
	    		passwordService.addMasterKey(username, encryptedMasterKey);   // Save it to DB
	    		
	    		String toEmail = username;
	    		String mailSubject = "Master Key for password backup";
	    		String mailBody = "Hi " + username
	    				          + "/n/n"
	    				          + "Master key is generated and securely stored";
	    				         
	    		try 
	    		{
	    			mailService.sendEmail(toEmail, mailSubject, mailBody);
	    		} 
	    		catch(MessagingException e) 
	    		{
	    			e.printStackTrace();
	    		}
	    		
	            success = true;
	        } 
	        
	        catch(Exception e) 
	        {
	            retryCount++;
	            lastException = e;
	            log.warn("Failed attempt {}, execution: {}", retryCount, executionId);
	            
	            if(retryCount > maxRetries) 
	            {
	                log.error("All retries failed for execution: {}, error: {}", 
	                         executionId, e.getMessage());
	                break;
	            }
	        }
	    }
	    
	    if(success) 
	    {
	        log.info("Operation successfull after {} attempts, execution: {}", 
	                retryCount, executionId);
	    } 
	  
	    else 
	    {
	        log.error("Operation failed after {} attempts, execution: {}, error: {}", 
	                 retryCount, executionId, lastException.getMessage());
	    }
	}
	
	
	public ResponseEntity<String> generateMasterKeyFallback(User registrationFormData, String username, 
															Throwable t) 
	{
	    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
	    		body("Service is temporarily unavailable. Please try again later.");
	}
	
	@CircuitBreaker(name = "addWebsitePasswordCB", fallbackMethod = "addWebsitePasswordFallback")
	@KafkaListener(topics = "add-website-password-event", groupId = "user-group")
	public String addWebsitePassword(Password websiteDetailsFormData, HttpServletRequest request)
	{
		String username = (String) request.getSession().getAttribute("username");
		websiteDetailsFormData.setUsername(username);
		passwordService.addPassword(websiteDetailsFormData);
		return "mainPage";
	}
	
	public ResponseEntity<String> addWebsitePasswordFallback(Password websiteDetailsFormData, 
														   HttpServletRequest request, Throwable t) 
	{
	    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
	    		body("Service is temporarily unavailable. Please try again later.");
	}
	
	@PostMapping("/handlePasswordIdToEdit")
	public String handlePasswordIdToEdit(@RequestParam(name = "password_id") String Id, 
			                        HttpServletRequest request)
	{
		request.getSession().setAttribute("ID", Id);
		return "editPassword";
	}
	
	@PostMapping("/editWebsitePassword")
	@CircuitBreaker(name = "editWebsitePasswordCB", fallbackMethod = "editWebsitePasswordFallback")
	public String editWebsitePassword(@ModelAttribute Password passwordDetails, HttpServletRequest request)
	{
		String passwordId = (String) request.getSession().getAttribute("ID");
		String newPassword = passwordDetails.getPassword();
		passwordService.updatePassword(passwordId, newPassword);
		return "mainPage";
	}
	
	public ResponseEntity<String> editWebsitePasswordFallback(Password passwordDetails, 
			   						                         HttpServletRequest request, Throwable t) 
	{
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
				body("Service is temporarily unavailable. Please try again later.");
	}

	@PostMapping("/deletePassword")
	@CircuitBreaker(name = "deletePasswordCB", fallbackMethod = "deletePasswordFallback")
	public String deletePassword(@RequestParam(name = "password_id") String Id)
	{
		passwordService.deletePassword(Id);
		return "mainPage";
	}
	
	public ResponseEntity<String> deletePasswordFallback(String Id, Throwable t) 
	{
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
				body("Service is temporarily unavailable. Please try again later.");
	}
	
	@RequestMapping("/loginPasswordAutoChange")
	public void loginPasswordAutoChange(HttpServletRequest request)
	{
		String newPassword = RandomStringUtils.random(8, 
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?/");
		
		String username = (String) request.getSession().getAttribute("username");
		User user = userService.getUser(username);
		user.setPassword(newPassword);
		userService.addUser(user);
		
		String mailSubject = "Password change for PVault";
		String mailBody = "Your Password have been successfully updated\n"
				        + "Your new password: " + newPassword;
		
		try 
		{
			mailService.sendEmail(username, mailSubject, mailBody);
		} 
		catch(MessagingException e) 
		{
			e.printStackTrace();
		}	
	}
	
	@GetMapping("/viewSavedPasswords")
	@CircuitBreaker(name = "viewSavedPasswordsCB", fallbackMethod = "viewSavedPasswordsFallback")
	public String viewSavedPasswords(Model model, HttpServletRequest request)
	{
		String username = (String) request.getSession().getAttribute("username");
		List<Password> passwordList = passwordService.getAllPasswords(username);
		model.addAttribute("passwordList", passwordList);  // Add the list to the model
        return "viewPasswords";
	}
	
	public ResponseEntity<String> viewSavedPasswordsFallback(Model model, HttpServletRequest request, 
			                                                 Throwable t) 
	{
	    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
	    		body("Service is temporarily unavailable. Please try again later.");
	}
	
	@KafkaListener(topics = "view-deleted-passwords-event", groupId = "user-group")
	public String viewDeletedPasswords(Model model, HttpServletRequest request, String event)
	{
		String username = (String) request.getSession().getAttribute("username");
		List<Password> userDeletedPasswords = new ArrayList<Password>();
		
		try 
		{
			userDeletedPasswords = backupAndRestoreService.restoreUserSavedPasswords(username);
		} 
		catch (Exception e) { e.printStackTrace(); }
		
		model.addAttribute("deletedPasswordList", userDeletedPasswords);
		return "restoredPasswords";
	}
	
	@KafkaListener(topics = "restore-password-event", groupId = "user-group")
	public void restorePassword(Password userPassword)
	{		
		passwordService.addPassword(userPassword);
	}
	
	@CircuitBreaker(name = "sendOTPCB", fallbackMethod = "sendOTPFallback")
	@KafkaListener(topics = "send-otp-event", groupId = "user-group")
	public String emailOTP(ArrayList<String> emailDetails)
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
	                try 
	                {
	                    Thread.sleep(backoffTimes[retryCount - 1]);
	                    
	                } 
	                
	                catch(InterruptedException ie) 
	                {
	                    Thread.currentThread().interrupt();
	                }
	            }
	            
	            String toEmail = emailDetails.get(0);
	    		String userOTP = emailDetails.get(1);
	    		
	    		try 
	    		{
	    			otpService.sendOTPEmail(toEmail, userOTP);
	    		} 
	    		catch(MessagingException e) 
	    		{
	    			e.printStackTrace();
	    		}
	    		
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
	    
		return "homePage";
	}
	
	public ResponseEntity<String> sendOTPFallback(User registrationFormData, HttpServletRequest request, 
			                                      Throwable t) 
	{
	    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
	    		body("Service is temporarily unavailable. Please try again later.");
	}
	
	@CircuitBreaker(name = "sendUserLoginOTPCB", fallbackMethod = "sendUserLoginOTPFallback")
	@KafkaListener(topics = "send-user-login-otp-event", groupId = "user-group")
	public void sendUserLoginOTP(ArrayList<String> emailDetails)
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
	                try 
	                {
	                    Thread.sleep(backoffTimes[retryCount - 1]);
	                    
	                } 
	                
	                catch(InterruptedException ie) 
	                {
	                    Thread.currentThread().interrupt();
	                }
	            }	            

	    		String userOTP = emailDetails.get(0);
	    		String toEmail = emailDetails.get(1);
	    	
	    		try 
	    		{
	    			otpService.sendOTPEmail(toEmail, userOTP);
	    		} 
	    		catch(MessagingException e) 
	    		{
	    			e.printStackTrace();
	    		}
	                  
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
	        log.info("Successfull after {} attempts, execution: {}", 
	                retryCount, executionID);
	    } 
	    
	    else 
	    {
	        log.error("Failed after {} attempts, execution: {}, error: {}", 
	                 retryCount, executionID, lastException.getMessage());
	    }
	}
	
	public ResponseEntity<String> sendUserLoginOTPFallback(HttpServletRequest request, String username, Throwable t) 
	{
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
				body("Service is temporarily unavailable. Please try again later. :)");
	}
	
	@PostMapping("/userLogin")
	@CircuitBreaker(name = "userLoginCB", fallbackMethod = "userLoginFallback")
	public String userLogin(@ModelAttribute User userLoginFormData, HttpServletRequest request, 
			                @RequestParam("otp") String otp)
	{
		String userReq = userLoginFormData.getUsername();
		User u = userService.getUser(userReq);
		
		String inputPassword = userLoginFormData.getPassword();
		String storedPassword = u.getPassword();
		String sentOTP = (String) request.getSession().getAttribute("OTP");
		String enteredOTP = otp;
		
		if(authService.authenticate(inputPassword, storedPassword) && sentOTP.equals(enteredOTP))
		{
			request.getSession().setAttribute("username", u.getUsername());
			return "vaultOTP";
		}
		
		else
		{
			return "homePage";
		}
	}
	
	public ResponseEntity<String> userLoginFallback(User userLoginFormData, 
            										HttpServletRequest request, String otp, Throwable t) 
		{
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
					body("Service is temporarily unavailable. Please try again later. :)");  //Fallback method
		}
	
	@RequestMapping("/userLogout")
	public String userLogout(HttpServletRequest request)
	{
		HttpSession session = request.getSession(false); // Get existing session
		
	    if(session != null) 
	    {
	        session.invalidate(); // Invalidate the session
	    }
	    return "redirect:/homePage";		
	}
	
	@CircuitBreaker(name = "sendUserVaultOTPCB", fallbackMethod = "sendUserVaultOTPFallback") //Circuit breaker
	@KafkaListener(topics = "send-user-vault-otp-event", groupId = "user-group")
	public void sendUserVaultOTP(ArrayList<String> emailDetails)
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
	                }
	            }
	            
	            String userOTP = emailDetails.get(0);
	    		String toEmail = emailDetails.get(1);
	    	
	    		try 
	    		{
	    			otpService.sendOTPEmail(toEmail, userOTP);
	    		} 
	    		catch(MessagingException e) 
	    		{
	    			e.printStackTrace();
	    		}
	    		
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
	        log.info("Successfull after {} attempts, execution: {}", 
	                retryCount, executionID);
	    } 
	    
	    else 
	    {
	        log.error("Failed after {} attempts, execution: {}, error: {}", 
	                 retryCount, executionID, lastException.getMessage());
	    }	
	}
	
	public ResponseEntity<String> sendUserVaultOTPFallback(HttpServletRequest request, Throwable t) 
	{
	    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
	    		body("Service is temporarily unavailable. Please try again later.");
	}
	
	@PostMapping("/userVaultOTPAuth")
	public String userVaultOTPAuth(HttpServletRequest request, @RequestParam("vOTP") String otp)
			                       
	{
		String inputOTP = otp;
		String sentOTP = (String) request.getSession().getAttribute("VaultOTP");
		
		if(inputOTP.equals(sentOTP))
		{
			return "mainPage";
		}
		
		else
		{
			return "homePage";
		}
	}
}














