package com.example.PVault.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class MailService 
{
	private final JavaMailSender mailSender;
	
	@Autowired
    public MailService(JavaMailSender mailSender)
    {
    	this.mailSender = mailSender;
    }
	
	public void sendEmail(String toEmail, String mailSubject, String mailBody) throws MessagingException 
    {
        String subject = mailSubject;
        String body = mailBody;
   
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body);
        mailSender.send(message);
    }
}






