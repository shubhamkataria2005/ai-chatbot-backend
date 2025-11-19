package com.Shubham.ai_chatbot_backend.controller;

import com.Shubham.ai_chatbot_backend.service.UserService;
import com.Shubham.ai_chatbot_backend.service.SalaryPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.io.*;

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

            // Call Python ML model for sentiment analysis
            Map<String, Object> sentimentResult = callPythonSentimentModel(text);

            if (sentimentResult != null && (boolean) sentimentResult.get("success")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("sentiment", sentimentResult.get("sentiment"));
                response.put("confidence", sentimentResult.get("confidence"));
                response.put("analysis", sentimentResult.get("analysis"));
                response.put("textLength", sentimentResult.get("textLength"));
                response.put("wordCount", sentimentResult.get("wordCount"));
                response.put("model", sentimentResult.get("model"));
                return response;
            } else {
                // Fallback to Java-based sentiment analysis
                return fallbackSentimentAnalysis(text);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Analysis failed");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    private Map<String, Object> callPythonSentimentModel(String text) {
        try {
            // Prepare input data
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("text", text);

            String inputJson = new com.google.gson.Gson().toJson(inputData);

            // Get models directory
            String modelsDir = getModelsDirectory();
            String pythonScript = modelsDir + "/sentiment_predictor.py";

            // Check if Python script exists
            File scriptFile = new File(pythonScript);
            if (!scriptFile.exists()) {
                System.out.println("❌ Python script not found: " + pythonScript);
                return null;
            }

            // Execute Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", pythonScript, inputJson
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("❌ Python script failed with exit code: " + exitCode);
                return null;
            }

            // Parse JSON response
            String jsonOutput = output.toString();
            return new com.google.gson.Gson().fromJson(jsonOutput, Map.class);

        } catch (Exception e) {
            System.out.println("❌ Error calling Python sentiment model: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> fallbackSentimentAnalysis(String text) {
        // Simple fallback sentiment analysis
        String lowerText = text.toLowerCase();

        String[] positiveWords = {"good", "great", "excellent", "amazing", "wonderful", "happy", "love", "like", "awesome", "fantastic", "best", "perfect"};
        String[] negativeWords = {"bad", "terrible", "awful", "hate", "dislike", "worst", "horrible", "angry", "sad", "disappointing", "poor"};

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            if (lowerText.contains(word)) positiveCount++;
        }

        for (String word : negativeWords) {
            if (lowerText.contains(word)) negativeCount++;
        }

        String sentiment;
        double confidence;
        String analysis;

        if (positiveCount > negativeCount) {
            sentiment = "POSITIVE";
            confidence = 70 + (positiveCount * 5);
            analysis = "The text shows positive sentiment with " + positiveCount + " positive indicators.";
        } else if (negativeCount > positiveCount) {
            sentiment = "NEGATIVE";
            confidence = 70 + (negativeCount * 5);
            analysis = "The text shows negative sentiment with " + negativeCount + " negative indicators.";
        } else if (positiveCount == negativeCount && positiveCount > 0) {
            sentiment = "MIXED";
            confidence = 65;
            analysis = "The text shows mixed emotions with both positive and negative elements.";
        } else {
            sentiment = "NEUTRAL";
            confidence = 80;
            analysis = "The text appears to be neutral or factual without strong emotional indicators.";
        }

        // Cap confidence at 95%
        confidence = Math.min(confidence, 95);

        Map<String, Object> result = new HashMap<>();
        result.put("sentiment", sentiment);
        result.put("confidence", confidence);
        result.put("analysis", analysis);
        result.put("textLength", text.length());
        result.put("wordCount", text.split("\\s+").length);
        result.put("model", "Fallback_Algorithm");

        return result;
    }

    private String getModelsDirectory() {
        try {
            // Try multiple approaches to find the models directory

            // Approach 1: Check if running from IDE (development)
            File devModelsDir = new File("src/main/resources/models");
            if (devModelsDir.exists()) {
                System.out.println("📁 Found models in: " + devModelsDir.getAbsolutePath());
                return devModelsDir.getAbsolutePath();
            }

            // Approach 2: Check if running from JAR (production)
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL resource = classLoader.getResource("models");
            if (resource != null) {
                String jarPath = new File(resource.toURI()).getAbsolutePath();
                System.out.println("📁 Found models in JAR: " + jarPath);
                return jarPath;
            }

            // Approach 3: Current directory
            File currentDir = new File("models");
            if (currentDir.exists()) {
                System.out.println("📁 Found models in current directory: " + currentDir.getAbsolutePath());
                return currentDir.getAbsolutePath();
            }

        } catch (Exception e) {
            System.out.println("❌ Error finding models directory: " + e.getMessage());
        }

        // Last resort
        String fallbackPath = new File(".").getAbsolutePath();
        System.out.println("⚠️ Using fallback path: " + fallbackPath);
        return fallbackPath;
    }

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
}