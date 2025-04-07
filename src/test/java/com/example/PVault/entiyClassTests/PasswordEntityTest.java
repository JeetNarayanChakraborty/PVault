package com.example.PVault.entiyClassTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.PVault.entityClasses.Password;



public class PasswordEntityTest 
{
    private Password password;

    @BeforeEach
    public void setUp() 
    {
        password = new Password();
    }

    @Test
    public void testPasswordConstructor() 
    {
        assertNotNull(password.getId(), "ID should be generated");
    }

    @Test
    public void testGetAndSetUsername() 
    {
        String username = "testUser";
        password.setUsername(username);
        assertEquals(username, password.getUsername(), "Username should be set correctly");
    }

    @Test
    public void testGetAndSetDname() 
    {
        String dname = "testDomain";
        password.setDname(dname);
        assertEquals(dname, password.getDname(), "Domain name should be set correctly");
    }

    @Test
    public void testGetAndSetPassword() 
    {
        String pwd = "testPassword";
        password.setPassword(pwd);
        assertEquals(pwd, password.getPassword(), "Password should be set correctly");
    }

    @Test
    public void testSetId() 
    {
        String id = "1234-5678-9101";
        password.setId(id);
        assertEquals(id, password.getId(), "ID should be set correctly");
    }
}
