package com.Shubham.ai_chatbot_backend.controller;

import com.Shubham.ai_chatbot_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserService userService;

    // User Registration
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        return userService.registerUser(username, email, password);
    }

    // User Login
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        return userService.loginUser(email, password);
    }

    // User Logout
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader("Authorization") String sessionToken) {
        boolean success = userService.logout(sessionToken);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Logout successful" : "Invalid session");
        return response;
    }

    // Validate Session
    @GetMapping("/validate")
    public Map<String, Object> validateSession(@RequestHeader("Authorization") String sessionToken) {
        boolean isValid = userService.validateSession(sessionToken);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        if (isValid) {
            var user = userService.getUserFromSession(sessionToken);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("avatar", user.getAvatar());
            response.put("user", userMap);
        }
        return response;
    }

    // Get all users (for testing)
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        List<Map<String, Object>> userList = new ArrayList<>();

        for (var user : userService.getAllUsers()) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("avatar", user.getAvatar());
            userMap.put("createdAt", user.getCreatedAt());
            userMap.put("lastLogin", user.getLastLogin());
            userMap.put("messageCount", user.getMessageCount());
            userList.add(userMap);
        }

        return userList;
    }

    // Test database connection
    @GetMapping("/test-db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            long userCount = userService.getAllUsers().size();

            response.put("status", "success");
            response.put("message", "Database connection successful!");
            response.put("userCount", userCount);
            response.put("database", "MySQL");

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Database connection failed: " + e.getMessage());
        }

        return response;
    }
}