package com.example.PVault.repositoryTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.PVault.entityClasses.User;
import com.example.PVault.service.userRepository;



@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest 
{
    @Autowired
    private userRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() 
    {
        user = new User();
        user.setId("12345");
        user.setUsername("testUser");
        user.setPassword("testPassword");
        userRepository.save(user);
    }

    @Test
    public void testFindByUsername() 
    {
        User foundUser = userRepository.findByUsername("testUser");
        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    public void testFindById() 
    {
        Optional<User> foundUser = userRepository.findById(1L);
        assertNotNull(foundUser.orElse(null));
        assertEquals("testUser", foundUser.get().getUsername());
    }

    @Test
    @Transactional
    public void testDeleteByUsername() 
    {
        userRepository.deleteByUsername("testUser");
        User deleted = userRepository.findByUsername("testUser");
        assertEquals(null, deleted);
    }

    @Test
    public void testSaveUser() 
    {
        User newUser = new User();
        newUser.setId("123456");
        newUser.setUsername("newUser");
        newUser.setPassword("newPassword");

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser);
        assertEquals("newUser", savedUser.getUsername());
    }
}
