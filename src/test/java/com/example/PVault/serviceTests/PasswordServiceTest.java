package com.example.PVault.serviceTests;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.PVault.entityClasses.Password;
import com.example.PVault.service.passwordRepository;
import com.example.PVault.service.passwordService;



@ExtendWith(MockitoExtension.class)
public class PasswordServiceTest 
{
    @Mock
    private passwordRepository PasswordRepository;

    @InjectMocks
    private passwordService passwordService;

    private Password password;

    @BeforeEach
    public void setUp() 
    {
        password = new Password();
        password.setPassword("password");
    }

    @Test
    public void testAddPassword() throws Exception 
    {
        doNothing().when(PasswordRepository).save(password);
        CompletableFuture<Void> future = passwordService.addPassword(password);
        future.join();
        verify(PasswordRepository, times(1)).save(password);
    }

    @Test
    public void testGetPasswordByID() 
    {
        when(PasswordRepository.findPasswordDetailsById("1")).thenReturn(password);
        Password foundPassword = passwordService.getPasswordByID("1");
        assertEquals(password, foundPassword);
    }

    @Test
    public void testGetAllPasswords() 
    {
        List<Password> passwords = new ArrayList<>();
        when(PasswordRepository.findAllPasswordsByUsername("user")).thenReturn(passwords);
        List<Password> foundPasswords = passwordService.getAllPasswords("user");
        assertEquals(passwords, foundPasswords);
    }

    @Test
    public void testUpdatePassword() 
    {
        when(PasswordRepository.findPasswordDetailsById("1")).thenReturn(password);
        passwordService.updatePassword("1", "newPassword");
        assertEquals("newPassword", password.getPassword());
        verify(PasswordRepository, times(1)).save(password);
    }

    @Test
    public void testDeletePassword() 
    {
        doNothing().when(PasswordRepository).deleteById("1");
        passwordService.deletePassword("1");
        verify(PasswordRepository, times(1)).deleteById("1");
    }

    @Test
    public void testGetWebsiteAndPassword() 
    {
        List<Object[]> websiteAndPasswords = new ArrayList<>();
        when(PasswordRepository.findWebsiteAndPasswordByUsername("user")).thenReturn(websiteAndPasswords);
        List<Object[]> foundWebsiteAndPasswords = passwordService.getWebsiteAndPassword("user");
        assertEquals(websiteAndPasswords, foundWebsiteAndPasswords);
    }

    @Test
    public void testAddUserBackup()
    {
        doNothing().when(PasswordRepository).addBackup("userID", "backup");
        passwordService.addUserBackup("userID", "backup");
        verify(PasswordRepository, times(1)).addBackup("userID", "backup");
    }

    @Test
    public void testGetUserBackUp() 
    {
        List<String> backups = new ArrayList<>();
        when(PasswordRepository.getBackup("userID")).thenReturn(backups);
        List<String> foundBackups = passwordService.getUserBackUp("userID");
        assertEquals(backups, foundBackups);
    }

    @Test
    public void testAddMasterKey() 
    {
        doNothing().when(PasswordRepository).addMasterKey("username", "masterKey");
        passwordService.addMasterKey("username", "masterKey");
        verify(PasswordRepository, times(1)).addMasterKey("username", "masterKey");
    }

    @Test
    public void testGetMasterKey() 
    {
        when(PasswordRepository.getMasterKeyByUsername("username")).thenReturn("masterKey");
        String masterKey = passwordService.getMasterKey("username");
        assertEquals("masterKey", masterKey);
    }
}

