package com.example.PVault.serviceTests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.example.PVault.entityClasses.Password;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.BackupAndRestoreService;
import com.example.PVault.service.KeyManagementService;
import com.example.PVault.service.MailService;
import com.example.PVault.service.UserService;
import com.example.PVault.service.passwordService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;



@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BackupAndRestoreServiceTest 
{
    @Mock
    private passwordService passwordService;

    @Mock
    private MailService mailService;

    @Mock
    private UserService userService;

    @Mock
    private KeyManagementService keyManagementService;

    private BackupAndRestoreService backupAndRestoreService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String username;
    private User user;
    private List<Object[]> passwordList;
    private List<String> encryptedBackups;
    private List<Password> currentPasswords;

    private final String validMasterKey = "masterKey1234567"; // 16-char AES key

    @BeforeEach
    public void setUp() throws Exception 
    {
        username = "testUser";
        user = new User();
        user.setId("123456");
        user.setUsername(username);

        passwordList = new ArrayList<>();
        passwordList.add(new Object[]{"site1", "user1", "pass1"});
        passwordList.add(new Object[]{"site2", "user2", "pass2"});

        backupAndRestoreService = spy(new BackupAndRestoreService(passwordService, mailService, userService, keyManagementService));

        HashMap<String, pwd> mockPwdMap = new HashMap<>();
        mockPwdMap.put("site1", new pwd("user1", "pass1"));
        mockPwdMap.put("site2", new pwd("user2", "pass2"));
        String jsonData = objectMapper.writeValueAsString(mockPwdMap);
        String encrypted = encryptAES(jsonData, validMasterKey);

        encryptedBackups = new ArrayList<>();
        encryptedBackups.add(encrypted); 

        currentPasswords = new ArrayList<>();
        currentPasswords.add(new Password("1", "user1", "site1", "pass1"));
        currentPasswords.add(new Password("2", "user2", "site2", "pass2"));

        when(userService.getUser(username)).thenReturn(user);
        when(passwordService.getWebsiteAndPassword(username)).thenReturn(passwordList);
        when(passwordService.getUserBackUp(user.getId())).thenReturn(encryptedBackups);
        when(passwordService.getAllPasswords(username)).thenReturn(currentPasswords);
    }

    @Test
    public void testBackupUserSavedPasswords() throws Exception 
    {
        when(keyManagementService.getUserMasterKey(username)).thenReturn(validMasterKey);

        backupAndRestoreService.backupUserSavedPasswords(username);

        verify(passwordService, times(1)).addUserBackup(eq(user.getId()), anyString());
        verify(mailService, times(1)).sendEmail(eq(username), anyString(), anyString());
    }

    @Test
    public void testBackupUserSavedPasswords_ExceptionHandling() throws Exception 
    {
        when(keyManagementService.getUserMasterKey(username)).thenReturn(validMasterKey);
        doThrow(new MessagingException()).when(mailService).sendEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> backupAndRestoreService.backupUserSavedPasswords(username));
    }

    @Test
    public void testRestoreUserSavedPasswords() throws Exception 
    {
        String masterKey = "masterKey1234567"; // 16 characters
        when(keyManagementService.getUserMasterKey(username)).thenReturn(masterKey);

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(masterKey.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        HashMap<String, pwd> pwdMap = new HashMap<>();
        pwdMap.put("id1", new pwd("gmail.com", "pass123"));
        pwdMap.put("id2", new pwd("yahoo.com", "pass456"));

        // Serialize to JSON string first
        String jsonData = objectMapper.writeValueAsString(pwdMap);

        // Then encrypt the JSON string
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        String encrypted = Base64.getEncoder().encodeToString(
            cipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8))
        );

        // Mock user
        User mockUser = new User();
        mockUser.setId("1234567");
        when(userService.getUser(username)).thenReturn(mockUser);

        // Mock current passwords so that they get removed during comparison
        when(passwordService.getAllPasswords(username)).thenReturn(Collections.emptyList());

        // Mock getPasswordByID
        when(passwordService.getPasswordByID("id1")).thenReturn(new Password());
        when(passwordService.getPasswordByID("id2")).thenReturn(new Password());

        // Run actual test
        List<Password> deleted = backupAndRestoreService.restoreUserSavedPasswords(username);

        assertNotNull(deleted);
        assertEquals(2, deleted.size()); // since nothing was removed from backup
    }


    @Test
    public void testRestoreUserSavedPasswords_ExceptionHandling() throws Exception 
    {
        when(keyManagementService.getUserMasterKey(username)).thenReturn(validMasterKey);
        doThrow(new RuntimeException(new IllegalBlockSizeException()))
            .when(passwordService).getUserBackUp(user.getId());

        assertThrows(Exception.class, () -> backupAndRestoreService.restoreUserSavedPasswords(username));
    }

    private String encryptAES(String plainText, String key) throws Exception 
    {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}



class pwd
{
	String websiteName;
	String Password;
	
	public pwd() 
	{
        // Empty default constructor needed for Jackson deserialization
    }
	
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


