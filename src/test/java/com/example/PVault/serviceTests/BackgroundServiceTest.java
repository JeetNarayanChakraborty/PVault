package com.example.PVault.serviceTests;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.BackgroundService;
import com.example.PVault.service.BackupAndRestoreService;
import com.example.PVault.service.MailService;
import com.example.PVault.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpSession;



@ExtendWith(MockitoExtension.class)
public class BackgroundServiceTest 
{
    @Mock
    private EntityManager entityManager;

    @Mock
    private HttpSession session;

    @Mock
    private MailService mailService;

    @Mock
    private UserService userService;

    @Mock
    private BackupAndRestoreService backupAndRestoreService;

    @InjectMocks
    private BackgroundService backgroundService;

    private User user;
    private String userID;
    private String username;

    @BeforeEach
    public void setUp() 
    {
        userID = "1";
        username = "testUser";
        user = new User();
        user.setId(Long.parseLong(userID));
        user.setUsername(username);

        when(session.getAttribute("userID")).thenReturn(userID);
        when(userService.getUserById(userID)).thenReturn(user);
    }

    @Test
    public void testFetchData() 
    {
        String queryStr = "SELECT u.username FROM users u JOIN password_history ph ON u.password = ph.Password WHERE ph.created_Time <= :timeThreshold";
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(queryStr)).thenReturn(query);
        when(query.setParameter("timeThreshold", LocalDateTime.now().minusSeconds(2592000))).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList("user1", "user2"));

        List<String> result = backgroundService.fetchData();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0));
        assertEquals("user2", result.get(1));
    }

    @Test
    public void testSendPasswordChangeReminder() throws MessagingException 
    {
        List<String> usernames = Arrays.asList("user1", "user2");
        when(backgroundService.fetchData()).thenReturn(usernames);

        backgroundService.sendPasswordChangeReminder();

        for(String username : usernames) 
        {
            verify(mailService, times(1)).sendEmail(eq(username), anyString(), anyString());
        }
    }

    @Test
    public void testSendPasswordChangeReminder_ExceptionHandling() throws MessagingException 
    {
        List<String> usernames = Arrays.asList("user1", "user2");
        when(backgroundService.fetchData()).thenReturn(usernames);
        doThrow(new MessagingException()).when(mailService).sendEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> backgroundService.sendPasswordChangeReminder());
    }

    @Test
    public void testScheduleBackup() throws Exception 
    {
        backgroundService.scheduleBackup();

        verify(backupAndRestoreService, times(1)).backupUserSavedPasswords(username);
    }

    @Test
    public void testScheduleBackup_ExceptionHandling() throws Exception 
    {
        doThrow(new InvalidKeyException()).when(backupAndRestoreService).backupUserSavedPasswords(username);

        assertDoesNotThrow(() -> backgroundService.scheduleBackup());
    }
}

