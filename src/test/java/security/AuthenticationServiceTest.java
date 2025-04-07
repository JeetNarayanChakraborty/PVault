package security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;



public class AuthenticationServiceTest 
{
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() 
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testEncrypt() 
    {
        String password = "myPassword";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        String result = authenticationService.encrypt(password);
        assertTrue(result.equals(encodedPassword));
    }

    @Test
    public void testAuthenticateSuccess() 
    {
        String inputPassword = "myPassword";
        String storedHashedPassword = "storedHashedPassword";

        when(passwordEncoder.matches(inputPassword, storedHashedPassword)).thenReturn(true);
        boolean result = authenticationService.authenticate(inputPassword, storedHashedPassword);
        assertTrue(result);
    }

    @Test
    public void testAuthenticateFailure() 
    {
        String inputPassword = "myPassword";
        String storedHashedPassword = "storedHashedPassword";

        when(passwordEncoder.matches(inputPassword, storedHashedPassword)).thenReturn(false);
        boolean result = authenticationService.authenticate(inputPassword, storedHashedPassword);
        assertFalse(result);
    }
}
