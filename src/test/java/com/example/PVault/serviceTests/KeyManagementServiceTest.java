package com.example.PVault.serviceTests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.HttpSession;
import com.example.PVault.service.KeyManagementService;
import com.example.PVault.service.passwordService;



@ExtendWith(MockitoExtension.class)
public class KeyManagementServiceTest 
{

    @Mock
    private HttpSession session;

    @Mock
    private passwordService passwordService;

    @InjectMocks
    private KeyManagementService keyManagementService;

    @BeforeEach
    public void setUp() 
    {
        keyManagementService = new KeyManagementService(session);
    }

    @Test
    public void testGetUserMasterKey() throws Exception 
    {
        String username = "user";
        String keytoDecryptMasterKey = Base64.getEncoder().encodeToString("testKey".getBytes());
        String encryptedMasterKey = Base64.getEncoder().encodeToString("masterKey".getBytes());

        when(session.getAttribute("AESEncyptionKeyForMasterKey")).thenReturn(keytoDecryptMasterKey);
        when(passwordService.getMasterKey(username)).thenReturn(encryptedMasterKey);

        String masterKey = KeyManagementService.getUserMasterKey(username);

        assertEquals("masterKey", masterKey);
    }
}
