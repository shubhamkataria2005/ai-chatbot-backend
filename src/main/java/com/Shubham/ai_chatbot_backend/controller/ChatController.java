package com.Shubham.ai_chatbot_backend.controller;

import com.Shubham.ai_chatbot_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
// @CrossOrigin(origins = "http://localhost:3000") - Commented for Railway deployment
public class ChatController {

    @Autowired
    private UserService userService;

    // Conversation memory for context
    private Map<String, List<String>> conversationMemory = new HashMap<>();
    private int sessionCounter = 0;

    @PostMapping("/send")
    public Map<String, Object> receiveMessage(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String sessionToken) {

        // Check authentication
        if (sessionToken == null || !userService.validateSession(sessionToken)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication required");
            errorResponse.put("message", "Please login to chat");
            return errorResponse;
        }

        String userMessage = request.get("message");
        String sessionId = request.getOrDefault("sessionId", "default");

        // Get user info
        var user = userService.getUserFromSession(sessionToken);
        System.out.println("👤 User " + user.getUsername() + " (ID: " + user.getId() + "): " + userMessage);

        // Generate intelligent response
        String botResponse = generateSmartResponse(userMessage);

        System.out.println("🤖 AI: " + botResponse);

        Map<String, Object> response = new HashMap<>();
        response.put("response", botResponse);
        response.put("status", "success");
        response.put("sessionId", sessionId);
        response.put("timestamp", new Date().toString());
        response.put("model", "Smart_Java_AI_v1.0");

        return response;
    }

    private String generateSmartResponse(String userMessage) {
        String lower = userMessage.toLowerCase();

        // === ADD YOUR CUSTOM QUESTIONS & ANSWERS HERE ===

        // EXAMPLE: Only one Q&A - add more below this
        if (lower.contains("what is your name")) {
            return "I'm Shubham's AI Assistant! You can call me Shubh. 🤖";
        }

        if (lower.contains("what are you studying")) {
            return "I'm doing BIT from Otago Polytechnic";
        }

        if (lower.contains("what is your interests")) {
            return "I'm really into AI right now.";
        }

        if (lower.contains("what projects have you worked on")) {
            return "I have worked on several projects but if you are interested I can send my Portfolio link. You can have a look https://shubhamkataria2005.github.io/Shubham_Portfolio/";
        }

        // Default responses if no custom match
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello! Shubham here. How can I help you today?";
        }

        if (lower.contains("how are you")) {
            return "I'm doing great! Ready to help you with your questions.";
        }

        if (lower.contains("thank")) {
            return "You're welcome! Happy to help!";
        }

        if (lower.contains("bye") || lower.contains("goodbye")) {
            return "Goodbye! Come back anytime!";
        }

        // Default response for unknown questions
        return "I understand you're asking about: \"" + userMessage + "\". I can't help you right now but i will get back to you soon";
    }

    // Additional endpoints for chatbot management
    @GetMapping("/info")
    public Map<String, String> getBotInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Shubham's AI Chatbot");
        info.put("version", "1.0");
        info.put("description", "Intelligent Java-based AI Assistant");
        info.put("features", "Smart Responses, User Authentication, Custom Q&A");
        info.put("technology", "React + Java Spring Boot + MySQL");
        info.put("status", "Active and Ready!");
        return info;
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Chat API is working! MySQL backend connected.");
        response.put("status", "success");
        return response;
    }
}