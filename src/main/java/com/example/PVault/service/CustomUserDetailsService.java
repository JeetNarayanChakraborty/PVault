package com.example.PVault.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;



@Service
public class CustomUserDetailsService implements UserDetailsService 
{
    private final UserService userService;

    public CustomUserDetailsService(UserService userService) 
    {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException 
    {
    	com.example.PVault.entityClasses.User user = userService.getUser(username);

        if (user == null) 
        {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) 
                .build();
    }
}








