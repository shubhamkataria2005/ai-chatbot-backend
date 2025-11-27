package com.Shubham.ai_chatbot_backend.controller;

import com.Shubham.ai_chatbot_backend.service.UserService;
import com.Shubham.ai_chatbot_backend.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private UserService userService;

    @Autowired
    private OpenAIService openAIService;

    // Conversation memory for context
    private Map<String, List<String>> conversationMemory = new HashMap<>();

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

        // Generate intelligent response using hybrid approach
        String botResponse = openAIService.generateResponse(userMessage);

        System.out.println("🤖 Response: " + botResponse);

        Map<String, Object> response = new HashMap<>();
        response.put("response", botResponse);
        response.put("status", "success");
        response.put("sessionId", sessionId);
        response.put("timestamp", new Date().toString());

        // Add source info for debugging
        if (botResponse.contains("Shubham") || botResponse.contains("portfolio") ||
                botResponse.contains("Otago Polytechnic") || botResponse.contains("BIT")) {
            response.put("model", "Custom_Response_v1.0");
        } else if (openAIService.isOpenAIAvailable()) {
            response.put("model", "OpenAI_GPT-3.5");
        } else {
            response.put("model", "Fallback_Response");
        }

        return response;
    }

    // Clear conversation memory
    @PostMapping("/clear")
    public Map<String, Object> clearConversation(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default");
        conversationMemory.remove(sessionId);

        return Map.of(
                "success", true,
                "message", "Conversation cleared successfully"
        );
    }

    // Get bot info
    @GetMapping("/info")
    public Map<String, String> getBotInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Shubham's AI Chatbot");
        info.put("version", "2.0");
        info.put("description", "Hybrid AI-powered chatbot with custom responses and OpenAI integration");
        info.put("features", "Custom Q&A, OpenAI AI Responses, Smart Fallbacks");
        info.put("technology", "React + Java Spring Boot + OpenAI API");
        info.put("status", "AI Powered and Ready!");
        info.put("response_mode", "Hybrid (Custom + OpenAI)");
        info.put("openai_available", String.valueOf(openAIService.isOpenAIAvailable()));
        return info;
    }

    // OpenAI Status Endpoint
    @GetMapping("/openai-status")
    public Map<String, Object> getOpenAIStatus() {
        Map<String, Object> status = openAIService.getOpenAIStatus();
        status.put("timestamp", new Date().toString());
        return status;
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Chat API is working! Hybrid AI system ready.");
        response.put("status", "success");
        response.put("openai_available", String.valueOf(openAIService.isOpenAIAvailable()));
        return response;
    }

    // Test the OpenAI service
    @GetMapping("/test-openai")
    public Map<String, Object> testOpenAI() {
        Map<String, Object> result = new HashMap<>();

        try {
            String testResponse = openAIService.generateResponse("Hello, who are you?");
            result.put("success", true);
            result.put("response", testResponse);
            result.put("openai_available", openAIService.isOpenAIAvailable());
            result.put("message", "OpenAI service test completed");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("openai_available", false);
            result.put("message", "OpenAI service test failed");
        }

        return result;
    }

    // Health check for chat service
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "chat");
        health.put("status", "healthy");
        health.put("timestamp", new Date().toString());
        health.put("openai_available", openAIService.isOpenAIAvailable());
        health.put("features", Arrays.asList(
                "authentication",
                "custom_responses",
                "openai_integration",
                "hybrid_response_system"
        ));
        return health;
    }
}