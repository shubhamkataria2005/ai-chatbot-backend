package com.Shubham.ai_chatbot_backend.service;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Service
@Configuration
public class OpenAIService {

    @Value("${openai.api.key:}")
    private String apiKey;

    private OpenAiService openAiService;

    // Initialize when the bean is created
    public OpenAIService() {
        // Initialization will happen after properties are set
    }

    // This method will be called after properties are injected
    private void initializeOpenAI() {
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-test-key-here")) {
            try {
                this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
                System.out.println("✅ OpenAI Service initialized successfully");
            } catch (Exception e) {
                System.out.println("❌ OpenAI initialization failed: " + e.getMessage());
                this.openAiService = null;
            }
        } else {
            System.out.println("⚠️ OpenAI API key not set, using fallback mode");
            this.openAiService = null;
        }
    }

    public String generateResponse(String userMessage) {
        // Initialize OpenAI on first use (lazy initialization)
        if (openAiService == null && apiKey != null && !apiKey.isEmpty()) {
            initializeOpenAI();
        }

        // First, check if it's one of your custom questions
        String customResponse = getCustomResponse(userMessage);
        if (customResponse != null) {
            System.out.println("✅ Using custom response");
            return customResponse;
        }

        // If OpenAI is available, use it
        if (openAiService != null) {
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
        System.out.println("🔧 Using simple response (OpenAI not configured)");
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
        return openAiService != null;
    }
}