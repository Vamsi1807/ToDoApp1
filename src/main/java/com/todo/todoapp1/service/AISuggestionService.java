package com.todo.todoapp1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections; // For List.of in older Java versions, or use Arrays.asList
import java.util.List;

@Service
public class AISuggestionService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    // Changed to a more free-tier friendly and efficient model
    private String currentGeminiModel = "gemini-2.5-flash"; // MODIFIED LINE

    public AISuggestionService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String getSuggestions(List<String> existingTodos) {
        if (existingTodos == null || existingTodos.isEmpty()) {
            return "Please provide at least one existing todo item";
        }

        try {
            String prompt = buildPrompt(existingTodos);
            String response = queryGeminiModel(prompt);
            return cleanGeminiResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return handleError(e);
        }
    }

    private String buildPrompt(List<String> todos) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Based on the following existing to-do items, ");
        promptBuilder.append("suggest 3-5 new, relevant, and helpful to-do items. ");
        promptBuilder.append("Provide only the suggestions, one per line, without numbering or bullet points:\n\n");

        todos.forEach(desc ->
                promptBuilder.append("- ").append(desc).append("\n"));

        promptBuilder.append("\nSuggestions:");
        return promptBuilder.toString();
    }

    private String queryGeminiModel(String prompt) throws Exception {
        GeminiRequest requestPayload = new GeminiRequest(prompt);
        String requestBody = objectMapper.writeValueAsString(requestPayload);

        String fullUri = "https://generativelanguage.googleapis.com/v1beta/models/" + currentGeminiModel + ":generateContent?key=" + geminiApiKey;

        // --- DEBUGGING PRINT STATEMENTS ---
        System.out.println("Gemini API URL: " + fullUri);
        System.out.println("Gemini Request Body: " + requestBody);
        // --- END DEBUGGING ---

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // --- DEBUGGING PRINT STATEMENTS ---
        System.out.println("Gemini Response Status Code: " + response.statusCode());
        System.out.println("Gemini Response Body: " + response.body());
        // --- END DEBUGGING ---

        if (response.statusCode() != 200) {
            throw new Exception("API Error: " + response.body());
        }
        return response.body();
    }

    private String cleanGeminiResponse(String jsonResponse) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode textNode = rootNode.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isTextual()) {
            return textNode.asText().trim();
        }
        return "Failed to parse AI suggestions. Raw response: " + jsonResponse;
    }

    private String handleError(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("API Error")) {
            return "AI service unavailable. Error: " + e.getMessage();
        }
        return "Failed to get suggestions: " + e.getMessage();
    }

    public String getChatResponse(String userMessage) {
        try {
            String prompt = "You are a helpful todo assistant. Respond to this message with concise, actionable advice:\n\n" +
                    userMessage;
            String response = queryGeminiModel(prompt);
            return cleanGeminiResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't process your request. Error: " + e.getMessage();
        }
    }

    private static class GeminiRequest {
        public List<Content> contents;

        public GeminiRequest(String prompt) {
            this.contents = Collections.singletonList(new Content(Collections.singletonList(new Part(prompt))));
        }
    }

    private static class Content {
        public String role = "user";
        public List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    private static class Part {
        public String text;

        public Part(String text) {
            this.text = text;
        }
    }

    public void setModel(String modelName) {
        this.currentGeminiModel = modelName;
    }
}