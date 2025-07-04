package com.todo.todoapp1.scheduler;

import com.todo.todoapp1.model.TodoItem;
import com.todo.todoapp1.service.EmailService;
import com.todo.todoapp1.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ScheduledTasks {

    @Autowired
    private TodoService todoService;

    @Autowired
    private EmailService emailService;

    @Value("${todo.alert.email}")
    private String alertEmail;

    @Scheduled(fixedRate = 60000)
    public void checkAndSendAlerts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<TodoItem> tasksDueSoon = todoService.getIncompleteTasksDueSoon(now, oneHourLater);

        if (!tasksDueSoon.isEmpty()) {
            System.out.println("Found tasks due soon: " + tasksDueSoon.size());
            for (TodoItem task : tasksDueSoon) {
                String subject = "Reminder: To-Do Task Due Soon!";
                String body = String.format(
                        "Hi,\n\nJust a friendly reminder that your task '%s' is due at %s.\n\nDon't forget to complete it!",
                        task.getDescription(),
                        task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                );
                emailService.sendSimpleEmail(alertEmail, subject, body);
            }
        }
    }
}