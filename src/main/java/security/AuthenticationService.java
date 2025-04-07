package security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthenticationService 
{
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public String encrypt(String password) 
	{
        return passwordEncoder.encode(password);
    }
	
	public boolean authenticate(String inputPassword, String storedHashedPassword)
	{
		return passwordEncoder.matches(inputPassword, storedHashedPassword);
	}
}
