package security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Properties;

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
import jakarta.mail.Session;
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
    public void testSendOTPEmail() throws Exception {
        String toEmail = "test@example.com";
        String otp = "123456";

        // Create a real MimeMessage with a session
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        
        // Mock JavaMailSender to return our real message
        when(mailSender.createMimeMessage()).thenReturn(message);

        // Call the method under test
        otpService.sendOTPEmail(toEmail, otp);

        // Send triggers any finalization
        verify(mailSender, times(1)).send(message);

        // Read and assert message properties
        assertEquals(toEmail, message.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("OTP Code for PVault", message.getSubject());

        // Extract content
        Object content = message.getContent();
        String actualText = "";

        if (content instanceof jakarta.mail.Multipart multipart) {
            var bodyPart = multipart.getBodyPart(0);
            Object partContent = bodyPart.getContent();
            if (partContent instanceof String str) {
                actualText = str.trim();
            }
        } else if (content instanceof String str) {
            actualText = str.trim();
        }

        String expectedText = ("Your OTP code is: " + otp 
                              + "\n\n\n"
                              + "This verification code will only be valid for the next 5 mins.\n"
                              + "If you didn't sign up for PVault please ignore this mail.").trim();

        assertEquals(expectedText, actualText);
    }

}





