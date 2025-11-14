package com.Shubham.ai_chatbot_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiChatbotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatbotBackendApplication.class, args);
        System.out.println("🚀 AI Chatbot Backend Started!");
        System.out.println("📍 http://localhost:8080");
        System.out.println("💬 API: http://localhost:8080/api/chat/send");
        System.out.println("🔐 MySQL Database: Connected");
    }
}