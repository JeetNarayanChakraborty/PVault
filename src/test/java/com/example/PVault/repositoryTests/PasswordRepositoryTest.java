package com.example.PVault.repositoryTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.PVault.entityClasses.Password;
import com.example.PVault.service.passwordRepository;


@SpringBootTest
@ActiveProfiles("test")
public class PasswordRepositoryTest 
{
    @Mock
    private passwordRepository passwordRepository;

    @InjectMocks
    private PasswordRepositoryTest passwordRepositoryTest;

    private Password password;

    @BeforeEach
    public void setUp() 
    {
        MockitoAnnotations.openMocks(this);
        password = new Password();
        password.setId("1");
        password.setUsername("testUser");
        password.setDname("testDomain");
        password.setPassword("testPassword");
    }

    @Test
    public void testFindPasswordDetailsById() 
    {
        when(passwordRepository.findPasswordDetailsById("1")).thenReturn(password);
        Password foundPassword = passwordRepository.findPasswordDetailsById("1");
        assertNotNull(foundPassword);
        assertEquals("testUser", foundPassword.getUsername());
    }

    @Test
    public void testFindAllPasswordsByUsername() 
    {
        List<Password> passwords = Arrays.asList(password);
        when(passwordRepository.findAllPasswordsByUsername("testUser")).thenReturn(passwords);
        List<Password> foundPasswords = passwordRepository.findAllPasswordsByUsername("testUser");
        assertNotNull(foundPasswords);
        assertEquals(1, foundPasswords.size());
        assertEquals("testUser", foundPasswords.get(0).getUsername());
    }

    @Test
    public void testFindWebsiteAndPasswordByUsername() 
    {
        List<Object[]> data = Arrays.asList(new Object[][] {{"1", "testDomain", "testPassword"}});
        when(passwordRepository.findWebsiteAndPasswordByUsername("testUser")).thenReturn(data);
        List<Object[]> foundData = passwordRepository.findWebsiteAndPasswordByUsername("testUser");
        assertNotNull(foundData);
        assertEquals(1, foundData.size());
        assertEquals("testDomain", foundData.get(0)[1]);
    }

    @Test
    public void testAddBackup() 
    {
        passwordRepository.addBackup("1", "backupData");
        verify(passwordRepository).addBackup("1", "backupData");
    }

    @Test
    public void testGetBackup() 
    {
        List<String> backups = Arrays.asList("backupData");
        when(passwordRepository.getBackup("1")).thenReturn(backups);
        List<String> foundBackups = passwordRepository.getBackup("1");
        assertNotNull(foundBackups);
        assertEquals(1, foundBackups.size());
        assertEquals("backupData", foundBackups.get(0));
    }

    @Test
    public void testAddMasterKey() 
    {
        passwordRepository.addMasterKey("testUser", "masterKey");
        verify(passwordRepository).addMasterKey("testUser", "masterKey");
    }

    @Test
    public void testGetMasterKeyByUsername() 
    {
        when(passwordRepository.getMasterKeyByUsername("testUser")).thenReturn("masterKey");
        String masterKey = passwordRepository.getMasterKeyByUsername("testUser");
        assertNotNull(masterKey);
        assertEquals("masterKey", masterKey);
    }

    @Test
    @Transactional
    public void testDeleteById() 
    {
        passwordRepository.deleteById("1");
        verify(passwordRepository).deleteById("1");
    }
}
