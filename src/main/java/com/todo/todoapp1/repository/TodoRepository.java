package com.todo.todoapp1.repository;

import com.todo.todoapp1.model.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<TodoItem, Long> {

    // Custom query to find incomplete tasks due within a specific time range
    List<TodoItem> findByCompletedFalseAndDueDateBetween(LocalDateTime start, LocalDateTime end);
}