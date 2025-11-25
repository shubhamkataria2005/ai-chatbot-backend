package com.Shubham.ai_chatbot_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiChatbotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatbotBackendApplication.class, args);
        System.out.println("🚀 AI Chatbot Backend Started on Railway!");
        System.out.println("📍 Port: " + System.getenv("PORT"));
        System.out.println("💬 API Endpoints: /api/chat, /api/auth, /api/ai-tools");
        System.out.println("🔐 MySQL Database: Connected via Railway");
        System.out.println("🌐 CORS: Configured for production");
    }
}