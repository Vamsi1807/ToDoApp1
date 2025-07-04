
package com.todo.todoapp1.service;

import com.todo.todoapp1.model.TodoItem;
import com.todo.todoapp1.model.User;
import com.todo.todoapp1.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    // Get all todos for a specific user
    public List<TodoItem> getTodosByUser(User user) {
        return todoRepository.findByUser(user);
    }

    // Add a new todo item
    public TodoItem addTodoItem(TodoItem todo, User user) {
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    // Delete a todo item if it belongs to the user
    public void deleteTodoItem(Long id, User user) {
        TodoItem todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));

        if (!todo.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to delete this todo");
        }

        todoRepository.deleteById(id);
    }

    // Update a todo item if it belongs to the user
    public TodoItem updateTodoItem(Long id, boolean completed, User user) {
        TodoItem todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));

        if (!todo.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to update this todo");
        }

        todo.setCompleted(completed);
        return todoRepository.save(todo);
    }

    // Check if a todo item belongs to a user
    public boolean isTodoOwnedByUser(Long todoId, User user) {
        TodoItem todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + todoId));
        return todo.getUser().getId().equals(user.getId());
    }

    // Delete all completed todos for a specific user
    public void deleteCompletedTodos(User user) {
        List<TodoItem> completedTodos = todoRepository.findByUserAndCompleted(user, true);
        todoRepository.deleteAll(completedTodos);
    }
}