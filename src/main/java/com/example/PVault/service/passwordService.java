package com.example.PVault.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.PVault.entityClasses.Password;


@Service
public class passwordService 
{
	@Autowired
	passwordRepository PasswordRepository;
	
	public void addPassword(Password password)
	{
		PasswordRepository.save(password); // Save password
    }
    
    public List<Password> getAllPasswords(String username)
    {
    	return PasswordRepository.findAllPasswordsByUsername(username);  //Get user passwords
    }
    
    public void updatePassword(String ID, String UpdatedPassword)
    {
    	Password intendedWebsitePassword = PasswordRepository.findPasswordDetailsById(ID);  // Update Website password
    	intendedWebsitePassword.setPassword(UpdatedPassword);
    	PasswordRepository.save(intendedWebsitePassword);
    }

    public void deletePassword(String ID) 
    {
    	PasswordRepository.deleteById(ID); // Delete website's password by ID
    }
    
    public List<Object[]> getWebsiteAndPassword(String username)
    {
    	return PasswordRepository.findWebsiteAndPasswordByUsername(username);
    }
    
    public void addUserBackup(String userID, String backup)
    {
    	PasswordRepository.addBackup(userID, backup);
    }
    
    public void addMasterKey(String username, String masterKey)
    {
    	PasswordRepository.addMasterKey(username, masterKey);
    }
    
    public String getMasterKey(String username)
    {
    	return PasswordRepository.getMasterKeyByUsername(username);
    }
}







