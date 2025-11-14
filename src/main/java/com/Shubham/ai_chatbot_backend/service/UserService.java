package com.Shubham.ai_chatbot_backend.service;

import com.Shubham.ai_chatbot_backend.model.User;
import com.Shubham.ai_chatbot_backend.model.Session;
import com.Shubham.ai_chatbot_backend.repository.UserRepository;
import com.Shubham.ai_chatbot_backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;  // FIXED IMPORT
import java.util.*;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    public Map<String, Object> registerUser(String username, String email, String password) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.existsByEmail(email)) {
            response.put("success", false);
            response.put("message", "User with this email already exists");
            return response;
        }

        if (userRepository.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "Username already taken");
            return response;
        }

        try {
            User newUser = new User(username, email, password);
            User savedUser = userRepository.save(newUser);

            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("userId", savedUser.getId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> loginUser(String email, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOpt = userRepository.findByEmailAndPassword(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastLogin(java.time.LocalDateTime.now());
                userRepository.save(user);

                String sessionToken = UUID.randomUUID().toString();
                Session session = new Session(sessionToken, user);
                sessionRepository.save(session);

                sessionRepository.deleteExpiredSessions();

                response.put("success", true);
                response.put("message", "Login successful");
                response.put("sessionToken", sessionToken);
                response.put("user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "avatar", user.getAvatar()
                ));
            } else {
                response.put("success", false);
                response.put("message", "Invalid email or password");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
        }

        return response;
    }

    public boolean validateSession(String sessionToken) {
        try {
            Optional<Session> sessionOpt = sessionRepository.findBySessionToken(sessionToken);
            return sessionOpt.isPresent() && !sessionOpt.get().isExpired();
        } catch (Exception e) {
            return false;
        }
    }

    public User getUserFromSession(String sessionToken) {
        try {
            Optional<Session> sessionOpt = sessionRepository.findBySessionToken(sessionToken);
            return sessionOpt.map(Session::getUser).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean logout(String sessionToken) {
        try {
            sessionRepository.deleteBySessionToken(sessionToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}