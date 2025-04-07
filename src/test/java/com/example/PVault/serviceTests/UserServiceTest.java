package com.example.PVault.serviceTests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.UserService;
import com.example.PVault.service.userRepository;




@ExtendWith(MockitoExtension.class)
public class UserServiceTest 
{

    @Mock
    private userRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() 
    {
        user = new User();
        user.setUsername("user");
        user.setPassword("password");
    }

    @Test
    public void testAddUser() 
    {
        userService.addUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testGetUser() 
    {
        when(userRepository.findByUsername("user")).thenReturn(user);
        User foundUser = userService.getUser("user");
        assertEquals(user, foundUser);
    }

    @Test
    public void testGetUserById() 
    {
        when(userRepository.findById("1")).thenReturn(user);
        User foundUser = userService.getUserById("1");
        assertEquals(user, foundUser);
    }

    @Test
    public void testUpdateUser() 
    {
        when(userRepository.findByUsername("user")).thenReturn(user);
        userService.updateUser("user", "newPassword");
        assertEquals("newPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testRemoveUser() 
    {
        userService.RemoveUser("user");
        verify(userRepository, times(1)).deleteByUsername("user");
    }
}