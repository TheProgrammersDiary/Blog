package com.evalvis.blog;

public class FakeSmtpEmail implements Email {
    @Override
    public void sendEmail(String toEmail, String subject, String body) {
    }
}
