package com.Shubham.ai_chatbot_backend.service;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key:}")
    private String apiKey;

    private OpenAiService openAiService;

    @PostConstruct
    public void init() {
        initializeOpenAI();
    }

    private void initializeOpenAI() {
        System.out.println("🔑 Initializing OpenAI Service...");

        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-test-key-here")) {
            try {
                System.out.println("🔑 API Key length: " + apiKey.length());

                this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
                System.out.println("✅ OpenAI Service initialized successfully");

            } catch (Exception e) {
                System.out.println("❌ OpenAI initialization failed: " + e.getMessage());
                throw new RuntimeException("OpenAI initialization failed", e);
            }
        } else {
            System.out.println("❌ OpenAI API key not configured");
            throw new RuntimeException("OpenAI API key not found in environment variables");
        }
    }

    public String generateResponse(String userMessage) {
        // First, check if it's one of your custom questions
        String customResponse = getCustomResponse(userMessage);
        if (customResponse != null) {
            System.out.println("✅ Using custom response");
            return customResponse;
        }

        // Use OpenAI for all other questions
        System.out.println("🚀 Using OpenAI API");
        return callOpenAI(userMessage);
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
            return "Hello! How can I help you today?";
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

    private String callOpenAI(String userMessage) {
        try {
            // Simple, clean prompt - just answer the question directly
            String prompt = "You are a helpful AI assistant. Answer the user's question directly and concisely.\n\n" +
                    "User: " + userMessage + "\n" +
                    "Assistant:";

            CompletionRequest completionRequest = CompletionRequest.builder()
                    .model("gpt-3.5-turbo-instruct")
                    .prompt(prompt)
                    .maxTokens(300)
                    .temperature(0.7)
                    .build();

            String response = openAiService.createCompletion(completionRequest)
                    .getChoices()
                    .get(0)
                    .getText()
                    .trim();

            System.out.println("🤖 OpenAI Response: " + response);
            return response;

        } catch (Exception e) {
            System.out.println("❌ OpenAI API call failed: " + e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    public boolean isOpenAIAvailable() {
        return openAiService != null;
    }

    public Map<String, Object> getOpenAIStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("serviceAvailable", openAiService != null);
        status.put("apiKeyPresent", apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-test-key-here"));
        status.put("apiKeyLength", apiKey != null ? apiKey.length() : 0);
        return status;
    }
}