package com.Shubham.ai_chatbot_backend.controller;

import com.Shubham.ai_chatbot_backend.service.UserService;
import com.Shubham.ai_chatbot_backend.service.SalaryPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/ai-tools")
@CrossOrigin(origins = "http://localhost:3000")
public class AIToolsController {

    @Autowired
    private UserService userService;

    @Autowired
    private SalaryPredictionService salaryPredictionService;

    // Enhanced Salary Prediction Endpoint with ML
    @PostMapping("/salary-prediction")
    public Map<String, Object> predictSalary(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String sessionToken) {

        // Check authentication
        if (!userService.validateSession(sessionToken)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication required");
            errorResponse.put("message", "Please login to use this feature");
            return errorResponse;
        }

        try {
            int experience = (int) request.get("experience");
            String role = (String) request.get("role");
            String location = (String) request.get("location");
            String education = (String) request.get("education");
            List<String> skills = (List<String>) request.get("skills");

            System.out.println("🤖 ML Salary Prediction Request:");
            System.out.println("   Experience: " + experience + " years");
            System.out.println("   Role: " + role);
            System.out.println("   Location: " + location);
            System.out.println("   Education: " + education);
            System.out.println("   Skills: " + skills);

            // Use ML service for prediction
            Map<String, Object> prediction = salaryPredictionService.predictSalaryWithML(
                    experience, role, location, education, skills
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("predictedSalary", prediction.get("salary"));
            response.put("currency", prediction.get("currency"));
            response.put("salaryUSD", prediction.get("salaryUSD"));
            response.put("confidence", prediction.get("confidence"));
            response.put("factors", prediction.get("factors"));
            response.put("model", "ML_Model_v1.0");

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Prediction failed");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    // Test endpoint
    @GetMapping("/test-ml")
    public Map<String, String> testML() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "ML Salary Prediction API is working!");
        response.put("status", "success");
        response.put("model", "Trained on tech_salaries.csv");
        return response;
    }
}