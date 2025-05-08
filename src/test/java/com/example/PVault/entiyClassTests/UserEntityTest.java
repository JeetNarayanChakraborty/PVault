package com.example.PVault.entiyClassTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.PVault.entityClasses.User;



public class UserEntityTest 
{
    private User user;

    @BeforeEach
    public void setUp() 
    {
        user = new User();
    }

    @Test
    public void testUserConstructor() 
    {
        assertNotNull(user.getId(), "ID should be generated");
    }

    @Test
    public void testGetAndSetUsername() 
    {
        String username = "testUser";
        user.setUsername(username);
        assertEquals(username, user.getUsername(), "Username should be set correctly");
    }

    @Test
    public void testGetAndSetPassword() 
    {
        String password = "testPassword";
        user.setPassword(password);
        assertEquals(password, user.getPassword(), "Password should be set correctly");
    }

    @Test
    public void testSetIdWithInt() 
    {
        String id = "123";
        user.setId(id);
        assertEquals(String.valueOf(id), user.getId(), "ID should be set correctly with int");
    }
}
