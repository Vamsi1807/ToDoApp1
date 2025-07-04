package com.todo.todoapp1.service;

import com.todo.todoapp1.model.TodoItem;
import com.todo.todoapp1.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private AISuggestionService aiSuggestionService;

    public List<TodoItem> getAllTodoItems() {
        return todoRepository.findAll();
    }

    public Optional<TodoItem> getTodoItemById(Long id) {
        return todoRepository.findById(id);
    }

    public TodoItem saveTodoItem(TodoItem todoItem) {
        return todoRepository.save(todoItem);
    }

    public void deleteTodoItem(Long id) {
        todoRepository.deleteById(id);
    }

    public String getAiSuggestionsForTodos() {
        List<String> existingDescriptions = todoRepository.findAll().stream()
                .map(TodoItem::getDescription)
                .collect(Collectors.toList());
        return aiSuggestionService.getSuggestions(existingDescriptions);
    }

    public List<TodoItem> getIncompleteTasksDueSoon(LocalDateTime start, LocalDateTime end) {
        return todoRepository.findByCompletedFalseAndDueDateBetween(start, end);
    }
}