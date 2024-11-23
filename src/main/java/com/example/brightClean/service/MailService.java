package com.example.brightClean.service;


public interface MailService {

    public void sendPlainText(String receivers, String subject, String content);
}
