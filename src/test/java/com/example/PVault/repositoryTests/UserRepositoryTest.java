package com.example.PVault.repositoryTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.userRepository;



@DataJpaTest
public class UserRepositoryTest 
{
    @Mock
    private userRepository userRepository;

    @InjectMocks
    private UserRepositoryTest userRepositoryTest;

    private User user;

    @BeforeEach
    public void setUp() 
    {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("testPassword");
    }

    @Test
    public void testFindByUsername() 
    {
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        User foundUser = userRepository.findByUsername("testUser");
        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    public void testFindById() 
    {
        when(userRepository.findById("1")).thenReturn(user);
        User foundUser = userRepository.findById("1");
        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    public void testDeleteByUsername() 
    {
        userRepository.deleteByUsername("testUser");
        verify(userRepository).deleteByUsername("testUser");
    }

    @Test
    public void testSaveUser() 
    {
        when(userRepository.save(user)).thenReturn(user);
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser);
        assertEquals("testUser", savedUser.getUsername());
    }
}
