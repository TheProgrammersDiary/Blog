package com.evalvis.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmail implements Email {
    @Value("${spring.mail.username}")
    private String sourceEmail;
    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sourceEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }
}
