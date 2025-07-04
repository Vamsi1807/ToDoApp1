package com.todo.todoapp1.controller;

import com.todo.todoapp1.model.TodoItem;
import com.todo.todoapp1.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @GetMapping
    public List<TodoItem> getAllTodos() {
        return todoService.getAllTodoItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoItem> getTodoById(@PathVariable Long id) {
        return todoService.getTodoItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TodoItem createTodo(@RequestBody TodoItem todoItem) {
        return todoService.saveTodoItem(todoItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoItem> updateTodo(@PathVariable Long id, @RequestBody TodoItem todoItemDetails) {
        return todoService.getTodoItemById(id)
                .map(todoItem -> {
                    todoItem.setDescription(todoItemDetails.getDescription());
                    todoItem.setDueDate(todoItemDetails.getDueDate());
                    todoItem.setCompleted(todoItemDetails.isCompleted());
                    TodoItem updatedTodo = todoService.saveTodoItem(todoItem);
                    return ResponseEntity.ok(updatedTodo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        return todoService.getTodoItemById(id)
                .map(todoItem -> {
                    todoService.deleteTodoItem(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/suggestions")
    public String getAiSuggestions() {
        return todoService.getAiSuggestionsForTodos();
    }

    @PostMapping("/with-suggestions")
    public ResponseEntity<?> createTodoWithSuggestions(@RequestBody TodoItem todoItem) {
        TodoItem savedItem = todoService.saveTodoItem(todoItem);
        String suggestions = todoService.getAiSuggestionsForTodos();
        return ResponseEntity.ok(Map.of(
                "todo", savedItem,
                "suggestions", suggestions
        ));
    }
}