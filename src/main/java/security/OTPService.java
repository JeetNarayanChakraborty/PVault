package security;

import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class OTPService 
{
	private final JavaMailSender mailSender;
    private static final String OTP_CHARS = "0123456789";
    private static final int OTP_LENGTH = 6;
    
    @Autowired
    public OTPService(JavaMailSender mailSender)
    {
    	this.mailSender = mailSender;
    }

    public String generateOTP() 
    {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for(int i=0; i<OTP_LENGTH; i++) 
        {
            otp.append(OTP_CHARS.charAt(random.nextInt(OTP_CHARS.length())));
        }
        
        return otp.toString();
    }
    
    public void sendOTPEmail(String toEmail, String OTP) throws MessagingException 
    {
        String subject = "OTP Code for PVault";
        String body = "Your OTP code is: " + OTP 
        		      + "\n\n\n"
        		      + "This verification code will only be valid for the next 5 mins."
        		      + "\n"
        		      + "If you didn't sign up for PVault please ignore this mail.";
   

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body);
        mailSender.send(message);
    }
}
















