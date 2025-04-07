package security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.security.SecureRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;



public class OTPServiceTest 
{
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private OTPService otpService;

    @BeforeEach
    public void setUp() 
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGenerateOTP() 
    {
        String otp = otpService.generateOTP();
        assertEquals(6, otp.length());
        
        for (char c : otp.toCharArray()) 
        {
            assertTrue(Character.isDigit(c));
        }
    }

    @Test
    public void testSendOTPEmail() throws MessagingException 
    {
        String toEmail = "test@example.com";
        String otp = "123456";

        MimeMessage message = new MimeMessage((jakarta.mail.Session) null);

        when(mailSender.createMimeMessage()).thenReturn(message);

        otpService.sendOTPEmail(toEmail, otp);

        verify(mailSender, times(1)).send(message);
        assertEquals(toEmail, message.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("OTP Code for PVault", message.getSubject());
        try 
        {
			assertEquals("Your OTP code is: " + otp 
			             + "\n\n\n"
			             + "This verification code will only be valid for the next 5 mins."
			             + "\n"
			             + "If you didn't sign up for PVault please ignore this mail.", 
			             message.getContent().toString());
		} 
        
        catch(IOException e) {e.printStackTrace();} 
        catch(MessagingException e) {e.printStackTrace();}
    }
}
