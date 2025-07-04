package com.todo.todoapp1.service;

import com.todo.todoapp1.model.TodoItem;
import com.todo.todoapp1.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TodoReminderService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private EmailService emailService;

    // Runs every minute (60000 milliseconds)
    @Scheduled(fixedRate = 60000)
    public void checkUpcomingTodos() {
        System.out.println("Checking for upcoming todos at: " + LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);

        // Using the query method from your repository
        List<TodoItem> upcomingTodos = todoRepository.findUpcomingTodos(now, oneHourFromNow);

        System.out.println("Found " + upcomingTodos.size() + " upcoming todos");

        for (TodoItem todo : upcomingTodos) {
            try {
                if (todo.getUser() != null && todo.getUser().getEmail() != null) {
                    System.out.println("Sending reminder for todo: " + todo.getDescription());
                    emailService.sendReminderEmail(todo.getUser().getEmail(), todo.getDescription());

                    // Mark reminder as sent
                    todo.setReminderSent(true);
                    todoRepository.save(todo);

                    System.out.println("Reminder sent successfully for todo: " + todo.getId());
                }
            } catch (Exception e) {
                System.err.println("Failed to send reminder for todo " + todo.getId() + ": " + e.getMessage());
            }
        }
    }
}