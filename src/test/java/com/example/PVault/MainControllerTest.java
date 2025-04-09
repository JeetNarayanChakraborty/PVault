package com.example.PVault;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import com.example.PVault.entityClasses.Password;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.BackupAndRestoreService;
import com.example.PVault.service.MailService;
import com.example.PVault.service.UserService;
import com.example.PVault.service.passwordService;
import security.AuthenticationService;
import security.OTPService;



@ExtendWith(MockitoExtension.class)
public class MainControllerTest 
{

    @Mock private UserService userService;
    @Mock private passwordService passwordService;
    @Mock private AuthenticationService authService;
    @Mock private OTPService otpService;
    @Mock private MailService mailService;
    @Mock private BackupAndRestoreService backupAndRestoreService;
    @Mock private HttpServletRequest request;
    @Mock private HttpSession session;
    @Mock private Model model;

    @InjectMocks
    private MainController mainController;

    @BeforeEach
    public void setUp() 
    {
        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void testGetHomeAuthenticated() 
    {
        when(session.getAttribute("username")).thenReturn("user");
        String view = mainController.getHome(request);
        assertEquals("mainPage", view);
    }

    @Test
    public void testGetHomeNotAuthenticated() 
    {
        when(session.getAttribute("username")).thenReturn(null);
        String view = mainController.getHome(request);
        assertEquals("homePage", view);
    }

    @Test
    public void testGetRegistration() 
    {
        String view = mainController.getRegistration();
        assertEquals("registration", view);
    }

    @Test
    public void testGetAddPassword() 
    {
        String view = mainController.getAddPassword();
        assertEquals("addPassword", view);
    }

    @Test
    public void testGetMainPage() 
    {
        String view = mainController.getMainPage();
        assertEquals("mainPage", view);
    }

    @Test
    public void testUserRegistration() 
    {
        User user = new User();
        user.setPassword("password");
        when(authService.encrypt(anyString())).thenReturn("encryptedPassword");
        mainController.userRegistration(user, request);
        verify(userService, times(1)).addUser(user);
    }

    @Test
    public void testGenerateMasterKey() throws Exception 
    {
        when(request.getSession().getAttribute("username")).thenReturn("user");
        mainController.generateMasterKey(request, "user");
        verify(passwordService, times(1)).addMasterKey(anyString(), anyString());
    }

    @Test
    public void testAddWebsitePassword() 
    {
        Password password = new Password();
        when(session.getAttribute("username")).thenReturn("user");
        String view = mainController.addWebsitePassword(password, request);
        assertEquals("mainPage", view);
        verify(passwordService, times(1)).addPassword(password);
    }

    @Test
    public void testEditWebsitePassword() 
    {
        Password password = new Password();
        when(session.getAttribute("ID")).thenReturn("1");
        String view = mainController.editWebsitePassword(password, request);
        assertEquals("mainPage", view);
        verify(passwordService, times(1)).updatePassword("1", password.getPassword());
    }

    @Test
    public void testDeletePassword() 
    {
        String view = mainController.deletePassword("1");
        assertEquals("mainPage", view);
        verify(passwordService, times(1)).deletePassword("1");
    }

    @Test
    public void testViewSavedPasswords() 
    {
        when(session.getAttribute("username")).thenReturn("user");
        List<Password> passwords = new ArrayList<>();
        when(passwordService.getAllPasswords("user")).thenReturn(passwords);
        String view = mainController.viewSavedPasswords(model, request);
        assertEquals("viewPasswords", view);
        verify(model, times(1)).addAttribute("passwordList", passwords);
    }

    @Test
    public void testUserLogin() 
    {
        User user = new User();
        user.setUsername("user");
        user.setPassword("password");
        when(userService.getUser("user")).thenReturn(user);
        when(authService.authenticate(anyString(), anyString())).thenReturn(true);
        when(session.getAttribute("OTP")).thenReturn("123456");
        String view = mainController.userLogin(user, request, "123456");
        assertEquals("vaultOTP", view);
    }

    @Test
    public void testUserLogout() 
    {
        String view = mainController.userLogout(request);
        assertEquals("redirect:/homePage", view);
        verify(session, times(1)).invalidate();
    }

    @Test
    public void testHandlePasswordIdToEdit() 
    {
        String id = "1";
        String view = mainController.handlePasswordIdToEdit(id, request);
        assertEquals("editPassword", view);
        verify(session, times(1)).setAttribute("ID", id);
    }

    @Test
    public void testLoginPasswordAutoChange() 
    {
        when(session.getAttribute("username")).thenReturn("user");
        User user = new User();
        user.setUsername("user");
        when(userService.getUser("user")).thenReturn(user);
        mainController.loginPasswordAutoChange(request);
        verify(userService, times(1)).addUser(user);
        try 
        {
            verify(mailService, times(1)).sendEmail(eq("user"), anyString(), anyString());
        } 
        
        catch(MessagingException e) 
        {
            fail("Exception in mail service: " + e.getMessage());
        }
    }

    @Test
    public void testViewDeletedPasswords() 
    {
        when(session.getAttribute("username")).thenReturn("user");
        List<Password> passwords = new ArrayList<>();
        try 
        {
            when(backupAndRestoreService.restoreUserSavedPasswords("user")).thenReturn(passwords);
        } 
        
        catch(Exception e) 
        {
            fail("Exception during backup restore: " + e.getMessage());
        }
        String view = mainController.viewDeletedPasswords(model, request, "event");
        assertEquals("restoredPasswords", view);
        verify(model, times(1)).addAttribute("deletedPasswordList", passwords);
    }

    @Test
    public void testRestorePassword() 
    {
        Password password = new Password();
        mainController.restorePassword(password);
        verify(passwordService, times(1)).addPassword(password);
    }

    @Test
    public void testEmailOTP() 
    {
        ArrayList<String> emailDetails = new ArrayList<>();
        emailDetails.add("user@example.com");
        emailDetails.add("123456");
        String view = mainController.emailOTP(emailDetails);
        assertEquals("homePage", view);
        try 
        {
            verify(otpService, times(1)).sendOTPEmail("user@example.com", "123456");
        } catch (MessagingException e) 
        {
            fail("Exception in OTP service: " + e.getMessage());
        }
    }

    @Test
    public void testSendUserLoginOTP() 
    {
        ArrayList<String> emailDetails = new ArrayList<>();
        emailDetails.add("123456");
        emailDetails.add("user@example.com");
        mainController.sendUserLoginOTP(emailDetails);
        try 
        {
            verify(otpService, times(1)).sendOTPEmail("user@example.com", "123456");
        } 
        catch(MessagingException e) 
        {
            fail("Exception in OTP service: " + e.getMessage());
        }
    }

    @Test
    public void testUserVaultOTPAuth() 
    {
        when(session.getAttribute("VaultOTP")).thenReturn("123456");
        String view = mainController.userVaultOTPAuth(request, "123456");
        assertEquals("mainPage", view);
    }
}
