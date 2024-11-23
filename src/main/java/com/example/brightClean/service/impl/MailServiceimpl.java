package com.example.brightClean.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.brightClean.service.MailService;

@Service
public class MailServiceimpl implements MailService{

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendPlainText(String receivers, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(receivers);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom("a0983835990@gmail.com");

        mailSender.send(message);
    }
    
}
