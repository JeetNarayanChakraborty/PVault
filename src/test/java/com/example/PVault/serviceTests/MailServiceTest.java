package com.example.PVault.serviceTests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.example.PVault.service.MailService;



@ExtendWith(MockitoExtension.class)
public class MailServiceTest 
{

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    public void setUp() 
    {
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    public void testSendEmail() throws MessagingException 
    {
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        doNothing().when(mailSender).send(mimeMessage);

        mailService.sendEmail("user@example.com", "Test Subject", "Test Body");

        verify(mailSender, times(1)).send(mimeMessage);
    }
}
