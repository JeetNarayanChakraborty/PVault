package com.example.PVault.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.PVault.entityClasses.User;
import com.example.PVault.service.userRepository;


@Service
public class UserService 
{
    @Autowired
    private userRepository userRepository;


    public void addUser(User user) 
    {
        userRepository.save(user); // Save user
    }
    
    public User getUser(String username)
    {
    	return userRepository.findByUsername(username);  //Get user
    }
    
    public List<User> getAllUsers() 
	{
		return userRepository.findAll(); // Get all users
	}
    
    public User getUserById(String userID)
    {
    	return userRepository.findById(userID);
    }
    
    public void updateUser(String username, String UpdatedPassword)
    {
    	User temp = userRepository.findByUsername(username);  // Update User
    	temp.setPassword(UpdatedPassword);
    	userRepository.save(temp);
    }

    public void RemoveUser(String username) 
    {
        userRepository.deleteByUsername(username); // Delete user by ID
    }
    
    public void updatePasswordHistory(String userID)
    {
    	userRepository.updatePasswordHistory(userID); // Update password history
    }
}











