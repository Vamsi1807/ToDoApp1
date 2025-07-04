package com.todo.todoapp1.controller;

import com.todo.todoapp1.service.AISuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private AISuggestionService aiSuggestionService;

    @PostMapping
    public String chatWithAI(@RequestBody String userMessage) {
        return aiSuggestionService.getChatResponse(userMessage);
    }
}