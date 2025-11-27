package com.Shubham.ai_chatbot_backend.controller;

import com.Shubham.ai_chatbot_backend.service.UserService;
import com.Shubham.ai_chatbot_backend.service.SalaryPredictionService;
import com.Shubham.ai_chatbot_backend.service.WeatherPredictionService;
import com.Shubham.ai_chatbot_backend.service.CarRecognitionService;
import com.Shubham.ai_chatbot_backend.service.AIToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/ai-tools")
public class AIToolsController {

    @Autowired
    private UserService userService;

    @Autowired
    private SalaryPredictionService salaryPredictionService;

    @Autowired
    private WeatherPredictionService weatherPredictionService;

    @Autowired
    private CarRecognitionService carRecognitionService;

    @Autowired
    private AIToolsService aiToolsService;

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

            // Return the prediction directly from ML service
            return prediction;

        } catch (Exception e) {
            System.out.println("❌ Salary prediction error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Prediction failed");
            errorResponse.put("message", "ML service unavailable. Please try again later.");
            return errorResponse;
        }
    }

    // Sentiment Analysis Endpoint with ML
    @PostMapping("/sentiment-analysis")
    public Map<String, Object> analyzeSentiment(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String sessionToken) {

        // Check authentication
        if (!userService.validateSession(sessionToken)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication required");
            errorResponse.put("message", "Please login to use this feature");
            return errorResponse;
        }

        try {
            String text = request.get("text");
            System.out.println("📊 Sentiment Analysis Request: " + text);

            // Call Python ML model for sentiment analysis using service
            Map<String, Object> sentimentResult = aiToolsService.callPythonSentimentModel(text);

            if (sentimentResult != null && sentimentResult.get("success") != null && (boolean) sentimentResult.get("success")) {
                return sentimentResult;
            } else {
                // Return error if ML model fails
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Sentiment analysis service temporarily unavailable");
                errorResponse.put("message", "Please try again later");
                return errorResponse;
            }

        } catch (Exception e) {
            System.out.println("❌ Sentiment analysis error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Analysis failed");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    // Weather Prediction Endpoint with ML
    @PostMapping("/weather-prediction")
    public Map<String, Object> predictWeather(
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
            double temperature = ((Number) request.get("temperature")).doubleValue();
            double humidity = ((Number) request.get("humidity")).doubleValue();
            double windSpeed = ((Number) request.get("windSpeed")).doubleValue();
            double pressure = ((Number) request.get("pressure")).doubleValue();
            double rainfall = ((Number) request.get("rainfall")).doubleValue();

            System.out.println("🌤️ Weather Prediction Request:");
            System.out.println("   Temperature: " + temperature + "°C");
            System.out.println("   Humidity: " + humidity + "%");
            System.out.println("   Wind Speed: " + windSpeed + " km/h");
            System.out.println("   Pressure: " + pressure + " hPa");
            System.out.println("   Rainfall: " + rainfall + " mm");

            // Use weather prediction service
            Map<String, Object> prediction = weatherPredictionService.predictWeather(
                    temperature, humidity, windSpeed, pressure, rainfall
            );

            // Return the prediction directly from ML service
            return prediction;

        } catch (Exception e) {
            System.out.println("❌ Weather prediction error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Weather prediction failed");
            errorResponse.put("message", "Please check your input parameters");
            return errorResponse;
        }
    }

    // Enhanced Car Recognition Endpoint with ML
    @PostMapping("/car-recognition")
    public Map<String, Object> recognizeCar(
            @RequestParam("image") MultipartFile image,
            @RequestHeader("Authorization") String sessionToken) {

        // Check authentication
        if (!userService.validateSession(sessionToken)) {
            return Map.of("error", "Authentication required");
        }

        try {
            System.out.println("🚗 Car Recognition Request");
            System.out.println("📁 File name: " + image.getOriginalFilename());
            System.out.println("📁 File size: " + image.getSize() + " bytes");
            System.out.println("📁 Content type: " + image.getContentType());

            Map<String, Object> result = carRecognitionService.recognizeCar(image);
            System.out.println("🎯 Final result: " + result);

            return result;

        } catch (Exception e) {
            System.out.println("❌ Controller error: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Car recognition failed: " + e.getMessage()
            );
        }
    }

    // Image Analysis Endpoint (Generic)
    @PostMapping("/image-analysis")
    public Map<String, Object> analyzeImage(
            @RequestParam("image") MultipartFile image,
            @RequestHeader("Authorization") String sessionToken) {

        // Check authentication
        if (!userService.validateSession(sessionToken)) {
            return Map.of("error", "Authentication required");
        }

        try {
            System.out.println("🖼️ Image Analysis Request");
            System.out.println("📁 File name: " + image.getOriginalFilename());
            System.out.println("📁 File size: " + image.getSize() + " bytes");

            // For now, return basic image info
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image received successfully");
            response.put("fileName", image.getOriginalFilename());
            response.put("fileSize", image.getSize());
            response.put("contentType", image.getContentType());
            response.put("analysisType", "basic_info");

            return response;

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Image analysis failed"
            );
        }
    }

    // ================= EXISTING ENDPOINTS =================

    // Test endpoints
    @GetMapping("/test-ml")
    public Map<String, String> testML() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "ML Salary Prediction API is working!");
        response.put("status", "success");
        response.put("model", "Trained on tech_salaries.csv");
        return response;
    }

    @GetMapping("/test-sentiment")
    public Map<String, String> testSentiment() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sentiment Analysis API is working!");
        response.put("status", "success");
        response.put("model", "NaiveBayes with 82% accuracy");
        return response;
    }

    @GetMapping("/test-weather")
    public Map<String, String> testWeather() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Weather Prediction API is working!");
        response.put("status", "success");
        response.put("model", "Trained on auckland_weather.csv");
        return response;
    }

    @GetMapping("/test-car")
    public Map<String, String> testCar() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Car Recognition API is working!");
        response.put("status", "success");
        response.put("model", "TensorFlow CNN Model");
        response.put("supported_brands", "BMW, Mercedes, Audi, Toyota, Honda, Ford");
        return response;
    }

    // Health check endpoint for all AI tools
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return aiToolsService.getHealthStatus();
    }

    // Get available AI tools
    @GetMapping("/tools")
    public Map<String, Object> getAvailableTools() {
        return aiToolsService.getAvailableTools();
    }

    // Root endpoint for Railway health checks
    @GetMapping("/")
    public Map<String, String> home() {
        return aiToolsService.getHomeInfo();
    }
}