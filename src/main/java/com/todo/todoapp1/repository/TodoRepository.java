package com.todo.todoapp1.repository;

import com.todo.todoapp1.model.TodoItem;
import com.todo.todoapp1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Repository
public interface TodoRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByUser(User user);
    List<TodoItem> findByUserAndCompleted(User user, boolean completed);
    @Query("SELECT t FROM TodoItem t WHERE t.dueDate BETWEEN :now AND :oneHourFromNow " +
            "AND t.completed = false AND t.reminderSent = false")
    List<TodoItem> findUpcomingTodos(LocalDateTime now, LocalDateTime oneHourFromNow);
    List<TodoItem> findByDueDateBetweenAndCompletedFalse(LocalDateTime start, LocalDateTime end);


}