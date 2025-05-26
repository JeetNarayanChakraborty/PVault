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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.PVault.entityClasses.Password;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.AIService;
import com.example.PVault.service.BackupAndRestoreService;
import com.example.PVault.service.CustomUserDetailsService;
import com.example.PVault.service.Encryptor;
import com.example.PVault.service.MailService;
import com.example.PVault.service.UserService;
import com.example.PVault.service.passwordService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import messageQueue.GenerateMasterKeyEventProducer;
import com.example.PVault.entityClasses.formDetails;
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
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Autowired
	AIService aiService;
	
	@Autowired
	Encryptor encryptor;
	
	@Autowired
	CustomUserDetailsService userDetailsService;
	
	@Autowired
	private PersistentTokenBasedRememberMeServices rememberMeServices;
	
	private final GenerateMasterKeyEventProducer generateMasterKeyEventProducer;
	
	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	
	
	
	public MainController(GenerateMasterKeyEventProducer generateMasterKeyEventProducer) 
	{
        this.generateMasterKeyEventProducer = generateMasterKeyEventProducer;
    }
	
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
	
	@GetMapping("/getHomeAfterSessionInvalidation")
	public String getHomeAfterSessionInvalidation()
	{
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
	
	@GetMapping("/getContact")
	public String getContact()
	{
		return "contact";
	}
	
	@GetMapping("/getAboutDeveloper")
	public String getAboutDeveloper()
	{
		return "aboutMe";
	}
	
	@GetMapping("/getPasswordChangeForm")
	public String getPasswordChangeForm()
	{
		return "passwordChangeForm";
	}
	
	@GetMapping("/takeUsername")
	public String takeUsername()
	{
		return "takeUsername";
	}
	
	@PostMapping("/decryptPassword")
	@ResponseBody
	public String decryptPassword(@RequestBody String encryptedPassword) 
	{
	    return encryptor.decrypt(encryptedPassword.trim());
	}
	
	@CircuitBreaker(name = "userRegistrationCB", fallbackMethod = "userRegistrationFallback")
	@KafkaListener(topics = "user-registration-event", groupId = "user-group")
	public void userRegistration(formDetails formDetails)
	{
		User registrationFormData = formDetails.getRegistrationFormData();
		ArrayList<String> masterKeyList = formDetails.getMasterKeyList();		
		
		//Take password input
		String inputPassword = registrationFormData.getPassword();
		
		//Encrypt password
		String hashedPassword = authService.encrypt(inputPassword);
		registrationFormData.setPassword(hashedPassword);
			
		//Add user to Database
		userService.addUser(registrationFormData);
		
		passwordService.addAESEncryptionKeyForMasterKey(registrationFormData.getUsername(), masterKeyList.get(2)); //Save the AES key for master key in DB
		
		generateMasterKeyEventProducer.publishGenerateMasterKeyEvent(masterKeyList); //Publish event to generate master key
	}
	
	public ResponseEntity<String> userRegistrationFallback(formDetails formDetails, Throwable t) 
	{
	    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
	    		body("Service is temporarily unavailable. Please try again later. :)");
	}
	
	@CircuitBreaker(name = "generateMasterKeyCB", fallbackMethod = "generateMasterKeyFallback")
	@KafkaListener(topics = "generate-master-key-event", groupId = "user-group")
	public void generateMasterKey(ArrayList<String> masterKeyList) throws NoSuchAlgorithmException, 
	                                                                                  NoSuchPaddingException, InvalidKeyException, 
	                                                                                  IllegalBlockSizeException, BadPaddingException
	{
		String username = masterKeyList.get(0);
		String masterKey = masterKeyList.get(1);
		String AESEncyptionKeyForMasterKey = masterKeyList.get(2);
		
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
	    		
	            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(AESEncyptionKeyForMasterKey), "AES");
	            Cipher cipher = Cipher.getInstance("AES");
	            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);              // Encrypt Master key using AES 
	            byte[] encryptedBytes = cipher.doFinal(masterKey.getBytes());
	            String encryptedMasterKey = Base64.getEncoder().encodeToString(encryptedBytes);
	    		
	    		passwordService.addMasterKey(username, encryptedMasterKey);   // Save it to DB
	    		
	    		String toEmail = username;
	    		String mailSubject = "Registration for PVault";
	    		String mailBody = "Hi " + username
	    				          + "\n\n"
	    				          + "You have successfully registered for PVault. "
	    						  + "A master key has been generated and safely kept for you. "
	    				          + "(The master key will be used to restore the backed-up passwords)";
	    						  	    						  
	    		try 
	    		{
	    			System.out.println("Sending email to: " + toEmail);
	    			mailService.sendEmail(toEmail, mailSubject, mailBody);
	    		} 
	    		catch(Exception e) 
	    		{
	    			
	    			log.warn("Failed to send email to {}", toEmail);
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
	        log.info("\n\nOperation successfull after {} attempts, execution: {}", 
	                retryCount, executionId);
	    } 
	  
	    else 
	    {
	        log.error("\n\nOperation failed after {} attempts, execution: {}, error: {}", 
	                 retryCount, executionId, lastException.getMessage());
	    }
	}
	
	
	public ResponseEntity<String> generateMasterKeyFallback(ArrayList<String> masterKeyList, Throwable t) 
	{
	    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
	    		body("Service is temporarily unavailable. Please try again later.");
	}
	
	@CircuitBreaker(name = "addWebsitePasswordCB", fallbackMethod = "addWebsitePasswordFallback")
	@KafkaListener(topics = "add-website-password-event", groupId = "user-group")
	public void addWebsitePassword(Password websiteDetailsFormData)
	{
		websiteDetailsFormData.setPassword(encryptor.encrypt(websiteDetailsFormData.getPassword()));
		passwordService.addPassword(websiteDetailsFormData);
	}
	
	public ResponseEntity<String> addWebsitePasswordFallback(Password websiteDetailsFormData, 
														     Throwable t) 
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
		String encryptedPassword = encryptor.encrypt(newPassword);
		passwordService.updatePassword(passwordId, encryptedPassword);
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
	public String deletePassword(Model model, @RequestParam(name = "password_id") String Id, HttpServletRequest request)
	{
		passwordService.deletePassword(Id);
		return viewSavedPasswords(model, request);
	}
	
	public ResponseEntity<String> deletePasswordFallback(Model model, String Id, HttpServletRequest request, Throwable t) 
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
		String encryptedNewPassword = authService.encrypt(newPassword);
		user.setPassword(encryptedNewPassword);
		userService.addUser(user);
		
		userService.updatePasswordHistory(user.getId());
		
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
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		List<Password> encryptedPasswordList = passwordService.getAllPasswords(username);
		List<Password> passwordList = new ArrayList<>();
		
		for(Password p : encryptedPasswordList)
		{
			String decryptedPassword = encryptor.decrypt(p.getPassword());
			p.setPassword(decryptedPassword);
			passwordList.add(p);
		}
		
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
	public void viewDeletedPasswords(String user)
	{
		String username = user;
		List<Password> userDeletedPasswords = new ArrayList<Password>();
	}
		
	@KafkaListener(topics = "restore-password-event", groupId = "user-group")
	public void restorePassword(Password userPassword)
	{	
		try
		{
			passwordService.addPassword(userPassword);
		}
		
		catch(Exception e)
		{
			System.out.println("Error in restoring password: " + e.getMessage());
		}
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
	
	public ResponseEntity<String> sendOTPFallback(ArrayList<String> emailDetails, Throwable t) 
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
	
	public ResponseEntity<String> sendUserLoginOTPFallback(ArrayList<String> emailDetails, Throwable t) 
	{
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
				body("Service is temporarily unavailable. Please try again later. :)");
	}
	
	@PostMapping("/userLogin")
	@CircuitBreaker(name = "userLoginCB", fallbackMethod = "userLoginFallback")
	public String userLogin(@ModelAttribute User userLoginFormData, HttpServletRequest request, 
			                HttpServletResponse response, @RequestParam("otp") String otp)
	{
		String userReq = userLoginFormData.getUsername();
		User u = userService.getUser(userReq);
		
		String inputPassword = userLoginFormData.getPassword();
		String storedPassword = u.getPassword();
		String sentOTP = (String) request.getSession().getAttribute("OTP");
		String enteredOTP = otp;
		
		if(authService.authenticate(inputPassword, storedPassword) && sentOTP.equals(enteredOTP))
		{
			request.getSession().setAttribute("username", userReq);
			
			UserDetails userDetails = userDetailsService.loadUserByUsername(userReq);
	        UsernamePasswordAuthenticationToken auth =
	            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

	        // Set security context
	        SecurityContextHolder.getContext().setAuthentication(auth);

	        // Trigger remember-me manually
	        rememberMeServices.loginSuccess(request, response, auth);
			return "vaultOTP";
		}
		
		else
		{
			return "homePage";
		}
	}
	
	public ResponseEntity<String> userLoginFallback(User userLoginFormData, 
            										HttpServletRequest request, 
            										HttpServletResponse response, String otp, Throwable t) 
	{
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).
			   body("Service is temporarily unavailable. Please try again later. :)");  //Fallback method
	}
	
	@PostMapping("/forgotPassword")
	public String forgotPassword(@RequestParam("username") String username, HttpServletRequest request)
	{
		request.getSession().setAttribute("username", username);
		
		String mailSubject = "Reset Your PVault Password";
		String resetLink = "http://localhost:8080/main/getPasswordChangeForm?username=" + username;
		String mailBody =
					    "Hi,\n\n" +
					    "We received a request to reset your PVault password. Please click the link below to reset it:\n\n" +
					    resetLink + "\n\n" +
					    "If you didn't request a password reset, you can safely ignore this email — your password will remain unchanged.\n\n" +
					    "Thanks,\nThe PVault Team";
		
		try 
		{
			mailService.sendEmail(username, mailSubject, mailBody);
		} 
		catch(MessagingException e)
		{
			e.printStackTrace();
		}
		
		return "passwordResetUserNotifier";
	}
	
	@PostMapping("/passwordChange")
	public String passwordChange(@RequestParam("newPassword") String newPassword, HttpServletRequest request, Model model)
	{
		String username = (String) request.getSession().getAttribute("username");
		User user = userService.getUser(username);
		String oldPassword = user.getPassword();
		
		if(!newPassword.isEmpty() && !authenticationService.authenticate(newPassword, oldPassword))
		{
			String hashedPassword = authenticationService.encrypt(newPassword);
			user.setPassword(hashedPassword);
			userService.addUser(user);
			
			userService.updatePasswordHistory(user.getId());
			
			
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
			
			return "homePage";
		}
		
		else
		{
			 model.addAttribute("errorMessage", "Password change failed: new password is same as the old password.");
		     return "passwordChangeForm";
		}
	}

	@RequestMapping("/userLogout")
	public String userLogout(HttpServletRequest request)
	{
		HttpSession session = request.getSession(false); // Get existing session
		
	    if(session != null) 
	    {
	        session.invalidate(); // Invalidate the session
	    }
	    
	    return "homePage";		
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
	
	public ResponseEntity<String> sendUserVaultOTPFallback(ArrayList<String> emailDetails, Throwable t) 
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
	
	@GetMapping("/getAIPasswordInsights")
	public String getAIPasswordInsights(@RequestParam("stored_Password") String password, Model model)
	{
		boolean isPasswordFound= false;
		String passwordAnalysis = null;
		
		try 
		{
			isPasswordFound = aiService.isPasswordPwned(password);
			passwordAnalysis = aiService.analyzePasswordWithAI(password);
		} 
		catch(Exception e) {e.printStackTrace();}
		
		if(isPasswordFound)
		{
			String s = "Warning: This password has been exposed in one or more data breaches."
					 + "It is recommended to choose a different, more secure password.";
			
			String fullAnalysis = s + "<br><br>" + passwordAnalysis;
			model.addAttribute("passwordAnalysis", fullAnalysis);
			return "showPasswordInsight";
		}
		
		else
		{
			String s = "This password is not found in any data breaches. However, please ensure it meets security best practices.";
			String fullAnalysis = s + "<br><br>" + passwordAnalysis;
			model.addAttribute("passwordAnalysis", fullAnalysis);
			return "showPasswordInsight";
		}
	}
}














