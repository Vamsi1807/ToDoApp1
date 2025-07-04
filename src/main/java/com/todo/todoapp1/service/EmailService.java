package com.todo.todoapp1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendReminderEmail(String toEmail, String todoDescription) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Todo Reminder: Task Due in 1 Hour");
        message.setText("Reminder: Your todo item '" + todoDescription + "' is due in 1 hour!");
        System.out.println("email sent to "+toEmail+" !!");
        emailSender.send(message);
    }
}