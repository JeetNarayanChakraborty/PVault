package com.example.PVault;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.PVault.entityClasses.Password;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.MailService;
import com.example.PVault.service.UserService;
import com.example.PVault.service.passwordService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import security.AuthenticationService;
import security.OTPService;



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
	
	
	
	@GetMapping("/getHome")
	public String getHome(HttpServletRequest request)
	{
		if(request.getSession().getAttribute("username") != null)
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
	
	@PostMapping("/userRegistration")
	public String userRegistration(@ModelAttribute User registrationFormData, HttpServletRequest request)
	{
		//Take password input
		String inputPassword = registrationFormData.getPassword();
		
		//Encrypt password
		String hashedPassword = authService.encrypt(inputPassword);
		registrationFormData.setPassword(hashedPassword);
		
		request.getSession().setAttribute("username", registrationFormData.getUsername());
		request.getSession().setAttribute("UserID", registrationFormData.getId());
			
		//Add to Database
		userService.addUser(registrationFormData);
		return "mainPage";
	}
	
	@RequestMapping("/generateMasterKey")
	public void generateMasterKey(HttpServletRequest request, @RequestParam("hiddenField") String username)
	{
		request.getSession().setAttribute("username", username);
		String masterKey = UUID.randomUUID().toString().substring(0, 16);
	
		
		KeyGenerator keyGen = null;
		try 
		{
			keyGen = KeyGenerator.getInstance("AES");
		} 
		
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		
	    keyGen.init(256); 
	    SecretKey secretKey = keyGen.generateKey();
		
		
	    Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(masterKey.getBytes());
        String encryptedMasterKey = Base64.getEncoder().encodeToString(encryptedBytes);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		String toEmail = username;
		String mailSubject = "Master Key for password backup";
		String mailBody = "Hi " + username
				          + "/n/n"
				          + "Master key is generate and securely stored";
				         
		try 
		{
			mailService.sendEmail(toEmail, mailSubject, mailBody);
		} 
		catch(MessagingException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	@PostMapping("/addWebsitePassword")
	public String addWebsitePassword(@ModelAttribute Password websiteDetailsFormData, HttpServletRequest request)
	{
		String username = (String) request.getSession().getAttribute("username");
		websiteDetailsFormData.setUsername(username);
		passwordService.addPassword(websiteDetailsFormData);
		return "mainPage";
	}
	
	@PostMapping("/handlePasswordIdToEdit")
	public String handlePasswordIdToEdit(@RequestParam(name = "password_id") String Id, 
			                        HttpServletRequest request)
	{
		request.getSession().setAttribute("ID", Id);
		return "editPassword";
	}
	
	@PostMapping("/editWebsitePassword")
	public String editWebsitePassword(@ModelAttribute Password passwordDetails, HttpServletRequest request)
	{
		String passwordId = (String) request.getSession().getAttribute("ID");
		String newPassword = passwordDetails.getPassword();
		passwordService.updatePassword(passwordId, newPassword);
		return "mainPage";
	}
	
	@PostMapping("/deletePassword")
	public String deletePassword(@RequestParam(name = "password_id") String Id, 
			                        HttpServletRequest request)
	{
		passwordService.deletePassword(Id);
		return "mainPage";
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
	public String viewSavedPasswords(Model model, HttpServletRequest request)
	{
		String username = (String) request.getSession().getAttribute("username");
		List<Password> passwordList = passwordService.getAllPasswords(username);
		model.addAttribute("passwordList", passwordList);  // Add the list to the model
        return "viewPasswords";
	}
	
	@RequestMapping("/sendOTP")
	public String sendOTP(HttpServletRequest request)
	{
		String userOTP = otpService.generateOTP();
		request.getSession().setAttribute("OTP", userOTP);
		String toEmail = (String) request.getSession().getAttribute("username");
		
		try 
		{
			otpService.sendOTPEmail(toEmail, userOTP);
		} 
		catch(MessagingException e) 
		{
			e.printStackTrace();
		}
		
		return "homePage";
	}
	
	@RequestMapping("/sendUserLoginOTP")
	public String sendUserLoginOTP(HttpServletRequest request, @RequestParam("hiddenField") String username)
	{
		String userOTP = otpService.generateOTP();
		request.getSession().setAttribute("OTP", userOTP);
		String toEmail = username;
	
		try 
		{
			otpService.sendOTPEmail(toEmail, userOTP);
		} 
		catch(MessagingException e) 
		{
			e.printStackTrace();
		}
		
		return "homePage";
	}	
	
	@PostMapping("/userLogin")
	public String userLogin(@ModelAttribute User userLoginFormData, HttpServletRequest request, 
			                @RequestParam("otp") String otp)
	{
		String userReq = userLoginFormData.getUsername();
		User u = userService.getUser(userReq);
		
		String inputPassword = userLoginFormData.getPassword();
		String storedPassword = u.getPassword();
		request.getSession().setAttribute("username", u.getUsername());
		String sentOTP = (String) request.getSession().getAttribute("OTP");
		String enteredOTP = otp;
		
		if(authService.authenticate(inputPassword, storedPassword) && sentOTP.equals(enteredOTP))
		{
			return "vaultOTP";
		}
		
		else
		{
			return "homePage";
		}
	}
	
	@RequestMapping("/sendUserVaultOTP")
	public String sendUserVaultOTP(HttpServletRequest request)
	{
		String userOTP = otpService.generateOTP();
		request.getSession().setAttribute("VaultOTP", userOTP);
		String toEmail = (String) request.getSession().getAttribute("username");
	
		try 
		{
			otpService.sendOTPEmail(toEmail, userOTP);
		} 
		catch(MessagingException e) 
		{
			e.printStackTrace();
		}
		
		return "vaultOTP";
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














