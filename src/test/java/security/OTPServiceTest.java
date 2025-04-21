package security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Multipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;


public class OTPServiceTest 
{

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private OTPService otpService;

    private ArgumentCaptor<MimeMessage> messageCaptor;

    @BeforeEach
    public void setUp() 
    {
        MockitoAnnotations.openMocks(this);
        messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
    }
    
    private String extractTextFromMessage(MimeMessage message) throws Exception 
    {
        Object content = message.getContent();
        StringBuilder textContent = new StringBuilder();
        
        if(content instanceof String) 
        {
            textContent.append((String) content);
        } 
        
        else if(content instanceof Multipart) 
        {
            Multipart multipart = (Multipart) content;
            
            for(int i=0; i<multipart.getCount(); i++) 
            {
                jakarta.mail.BodyPart part = multipart.getBodyPart(i);
                Object partContent = part.getContent();
                
                if(partContent instanceof String) 
                {
                    textContent.append((String) partContent);
                } 
                
                else if(partContent instanceof Multipart) 
                {
                    MimeMessage tempMessage = new MimeMessage((jakarta.mail.Session) null);
                    tempMessage.setContent(partContent, part.getContentType());
                    textContent.append(extractTextFromMessage(tempMessage));
                }
            }
        }
        
        return textContent.toString();
    }

    @Test
    public void testGenerateOTP() 
    {
        String otp = otpService.generateOTP();
        assertEquals(6, otp.length());

        for(char c : otp.toCharArray()) 
        {
            assertTrue(Character.isDigit(c));
        }
    }

    @Test
    public void testSendOTPEmail() throws Exception 
    {
        String toEmail = "test@example.com";
        String otp = "123456";
        
        jakarta.mail.Session session = jakarta.mail.Session.getInstance(new java.util.Properties(), null);
        MimeMessage mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        otpService.sendOTPEmail(toEmail, otp);
        
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentMessage = messageCaptor.getValue();
        
        assertEquals(toEmail, sentMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("OTP Code for PVault", sentMessage.getSubject());

        String messageContent = extractTextFromMessage(sentMessage);
        assertTrue(messageContent.contains(otp), "Email should contain the OTP");
        assertTrue(messageContent.contains("Your OTP code is"), "Email should contain intro text");
        assertTrue(messageContent.contains("This verification code will only be valid"), "Email should contain validity statement");
        assertTrue(messageContent.contains("If you didn't sign up for PVault"), "Email should contain disclaimer");
    }
}




