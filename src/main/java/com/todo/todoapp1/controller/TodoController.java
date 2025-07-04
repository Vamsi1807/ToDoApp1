
package com.todo.todoapp1.controller;

import com.todo.todoapp1.model.TodoItem;
import com.todo.todoapp1.model.User;
import com.todo.todoapp1.service.TodoService;
import com.todo.todoapp1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private UserService userService;

    // Get all todos for the current user
    @GetMapping
    public ResponseEntity<List<TodoItem>> getAllTodos(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(todoService.getTodosByUser(user));
    }

    // Add a new todo with AI suggestions
    @PostMapping("/with-suggestions")
    public ResponseEntity<?> addTodoWithSuggestions(
            @RequestBody TodoItem todo,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            TodoItem savedTodo = todoService.addTodoItem(todo, user);

            // You can add AI suggestions logic here if needed
            return ResponseEntity.ok(Map.of(
                    "todo", savedTodo,
                    "suggestions", "[]" // Replace with actual suggestions if implementing AI features
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Delete a specific todo
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            todoService.deleteTodoItem(id, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Update a todo's completion status
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> updates,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            TodoItem updated = todoService.updateTodoItem(id, updates.get("completed"), user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Delete all completed todos for the current user
    @DeleteMapping("/completed")
    public ResponseEntity<?> deleteCompleted(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            todoService.deleteCompletedTodos(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}