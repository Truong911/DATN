package com.phamtruong.rookbooks.service;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
}
