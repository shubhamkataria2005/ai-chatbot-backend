package com.Shubham.ai_chatbot_backend.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OpenAIService {

    public String generateResponse(String userMessage) {
        System.out.println("🤖 Processing message: " + userMessage);

        // First, check if it's one of your custom questions
        String customResponse = getCustomResponse(userMessage);
        if (customResponse != null) {
            System.out.println("✅ Using custom response");
            return customResponse;
        }

        // For other questions, use a simple response
        System.out.println("🔧 Using simple response");
        return getSimpleResponse(userMessage);
    }

    private String getCustomResponse(String userMessage) {
        String lower = userMessage.toLowerCase().trim();

        // === YOUR CUSTOM QUESTIONS & ANSWERS ===
        if (lower.contains("what is your name") || lower.contains("who are you")) {
            return "I'm Shubham's AI Assistant! You can call me Shubh. 🤖";
        }

        if (lower.contains("what are you studying") || lower.contains("your studies")) {
            return "I'm doing BIT from Otago Polytechnic";
        }

        if (lower.contains("what is your interests") || lower.contains("your interests")) {
            return "I'm really into AI right now.";
        }

        if (lower.contains("what projects have you worked on") || lower.contains("your projects")) {
            return "I have worked on several projects but if you are interested I can send my Portfolio link. You can have a look https://shubhamkataria2005.github.io/Shubham_Portfolio/";
        }

        if (lower.contains("portfolio") || lower.contains("website")) {
            return "Here's my portfolio: https://shubhamkataria2005.github.io/Shubham_Portfolio/";
        }

        // === BASIC GREETINGS ===
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

        // Return null if no custom response matches
        return null;
    }

    private String getSimpleResponse(String userMessage) {
        // Simple responses for common questions
        String lower = userMessage.toLowerCase();

        if (lower.contains("what") && lower.contains("ai")) {
            return "AI (Artificial Intelligence) refers to computer systems that can perform tasks that typically require human intelligence.";
        }

        if (lower.contains("machine learning")) {
            return "Machine learning is a subset of AI that enables computers to learn and improve from experience.";
        }

        if (lower.contains("weather")) {
            return "I don't have real-time weather data, but you can use our Weather Predictor tool for forecasts!";
        }

        if (lower.contains("salary") || lower.contains("income")) {
            return "Check out our Salary Predictor tool for accurate tech salary estimates based on your experience and location!";
        }

        // Default response for unknown questions
        return "That's an interesting question! I'm Shubham's AI assistant. For specific queries, try our specialized tools like Salary Predictor or Sentiment Analyzer. How else can I help you?";
    }

    public boolean isOpenAIAvailable() {
        return false;
    }
}