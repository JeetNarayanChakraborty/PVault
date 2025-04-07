package com.example.PVault.serviceTests;



import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.PVault.entityClasses.Password;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.BackupAndRestoreService;
import com.example.PVault.service.KeyManagementService;
import com.example.PVault.service.MailService;
import com.example.PVault.service.UserService;
import com.example.PVault.service.passwordService;
import jakarta.mail.MessagingException;



@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private BackupAndRestoreService backupAndRestoreService;

    private String username;
    private User user;
    private SecretKey secretKey;
    private List<Object[]> passwordList;
    private List<String> encryptedBackups;
    private List<Password> currentPasswords;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException 
    {
        username = "testUser";
        user = new User();
        user.setId(1);
        user.setUsername(username);

        passwordList = new ArrayList<>();
        passwordList.add(new Object[]{"site1", "user1", "pass1"});
        passwordList.add(new Object[]{"site2", "user2", "pass2"});

        encryptedBackups = new ArrayList<>();
        encryptedBackups.add("encryptedData1");
        encryptedBackups.add("encryptedData2");

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
        when(KeyManagementService.getUserMasterKey(username)).thenReturn("masterKey");

        backupAndRestoreService.backupUserSavedPasswords(username);

        verify(passwordService, times(1)).addUserBackup(eq(user.getId()), anyString());
        verify(mailService, times(1)).sendEmail(eq(username), anyString(), anyString());
    }

    @Test
    public void testBackupUserSavedPasswords_ExceptionHandling() throws Exception 
    {
        when(KeyManagementService.getUserMasterKey(username)).thenReturn("masterKey");
        doThrow(new MessagingException()).when(mailService).sendEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> backupAndRestoreService.backupUserSavedPasswords(username));
    }

    @Test
    public void testRestoreUserSavedPasswords() throws Exception 
    {
        when(KeyManagementService.getUserMasterKey(username)).thenReturn("masterKey");

        List<Password> deletedPasswords = backupAndRestoreService.restoreUserSavedPasswords(username);

        assertNotNull(deletedPasswords);
        assertTrue(deletedPasswords.isEmpty());
    }

    @Test
    public void testRestoreUserSavedPasswords_ExceptionHandling() throws Exception 
    {
        when(KeyManagementService.getUserMasterKey(username)).thenReturn("masterKey");
        doThrow(new IllegalBlockSizeException()).when(passwordService).getUserBackUp(user.getId());

        assertThrows(Exception.class, () -> backupAndRestoreService.restoreUserSavedPasswords(username));
    }
}
