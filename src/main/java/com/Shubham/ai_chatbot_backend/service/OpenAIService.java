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
    private boolean openAiInitialized = false;

    @PostConstruct
    public void init() {
        initializeOpenAI();
    }

    private void initializeOpenAI() {
        System.out.println("🔑 Initializing OpenAI Service...");
        System.out.println("🔑 API Key present: " + (apiKey != null && !apiKey.isEmpty()));

        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-test-key-here")) {
            try {
                System.out.println("🔑 API Key starts with: " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
                System.out.println("🔑 API Key length: " + apiKey.length());

                this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));

                // Test the connection with a simple request
                try {
                    CompletionRequest testRequest = CompletionRequest.builder()
                            .model("gpt-3.5-turbo-instruct")
                            .prompt("Say 'Hello'")
                            .maxTokens(5)
                            .build();

                    String testResponse = openAiService.createCompletion(testRequest)
                            .getChoices()
                            .get(0)
                            .getText()
                            .trim();

                    System.out.println("✅ OpenAI Service initialized successfully");
                    System.out.println("✅ Test response: " + testResponse);
                    this.openAiInitialized = true;

                } catch (Exception testError) {
                    System.out.println("❌ OpenAI test failed: " + testError.getMessage());
                    this.openAiService = null;
                    this.openAiInitialized = false;
                }

            } catch (Exception e) {
                System.out.println("❌ OpenAI initialization failed: " + e.getMessage());
                this.openAiService = null;
                this.openAiInitialized = false;
            }
        } else {
            System.out.println("⚠️ OpenAI API key not set or using default, using fallback mode");
            System.out.println("🔑 API Key: " + (apiKey == null ? "null" : "empty or default"));
            this.openAiService = null;
            this.openAiInitialized = false;
        }
    }

    public String generateResponse(String userMessage) {
        // First, check if it's one of your custom questions
        String customResponse = getCustomResponse(userMessage);
        if (customResponse != null) {
            System.out.println("✅ Using custom response");
            return customResponse;
        }

        // If OpenAI is available and initialized, use it
        if (openAiService != null && openAiInitialized) {
            System.out.println("🚀 Using OpenAI API");
            try {
                return callOpenAI(userMessage);
            } catch (Exception e) {
                System.out.println("❌ OpenAI API error: " + e.getMessage());
                // Fallback to simple response if OpenAI fails
                return getSimpleResponse(userMessage);
            }
        }

        // If OpenAI not available, use simple responses
        System.out.println("🔧 Using simple response (OpenAI not configured or failed)");
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

    private String callOpenAI(String userMessage) {
        try {
            String prompt = "You are Shubham's AI Assistant, a helpful and friendly AI. " +
                    "Keep responses concise and friendly (1-2 paragraphs max). " +
                    "If someone asks about Shubham, mention he's a BIT student at Otago Polytechnic interested in AI.\n\n" +
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
        return openAiService != null && openAiInitialized;
    }

    // New method to get OpenAI status details
    public Map<String, Object> getOpenAIStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", openAiInitialized);
        status.put("serviceAvailable", openAiService != null);
        status.put("apiKeyPresent", apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-test-key-here"));
        status.put("apiKeyLength", apiKey != null ? apiKey.length() : 0);
        status.put("apiKeyPrefix", apiKey != null && apiKey.length() > 7 ? apiKey.substring(0, 7) : "N/A");
        return status;
    }
}