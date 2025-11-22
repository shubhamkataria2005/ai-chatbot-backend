package com.Shubham.ai_chatbot_backend.controller;

import com.Shubham.ai_chatbot_backend.service.UserService;
import com.Shubham.ai_chatbot_backend.service.SalaryPredictionService;
import com.Shubham.ai_chatbot_backend.service.WeatherPredictionService;
import com.Shubham.ai_chatbot_backend.service.CarRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private WeatherPredictionService weatherPredictionService;

    @Autowired
    private CarRecognitionService carRecognitionService;

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

            if (sentimentResult != null && sentimentResult.get("success") != null && (boolean) sentimentResult.get("success")) {
                return sentimentResult;
            } else {
                // Return error response if ML model fails
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Sentiment analysis service temporarily unavailable");
                errorResponse.put("message", "Please try again later");
                return errorResponse;
            }

        } catch (Exception e) {
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

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("predictedTemperature", prediction.get("predictedTemperature"));
            response.put("predictedRainfall", prediction.get("predictedRainfall"));
            response.put("weatherCondition", prediction.get("weatherCondition"));
            response.put("confidence", prediction.get("confidence"));
            response.put("factors", prediction.get("factors"));
            response.put("model", prediction.get("model"));
            response.put("location", "Auckland, NZ");

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Weather prediction failed");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

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
            return carRecognitionService.recognizeCar(image);

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Car recognition failed"
            );
        }
    }

    private Map<String, Object> callPythonSentimentModel(String text) {
        try {
            // Prepare input data
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("text", text);

            String inputJson = new com.google.gson.Gson().toJson(inputData);

            // Write JSON to a temporary file
            File tempFile = File.createTempFile("sentiment_input", ".json");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(inputJson);
            }

            System.out.println("📁 Created temp file: " + tempFile.getAbsolutePath());

            // Get models directory
            String modelsDir = getModelsDirectory();
            String pythonScript = modelsDir + "/sentiment_predictor.py";

            System.out.println("📁 Python script path: " + pythonScript);

            // Check if Python script exists
            File scriptFile = new File(pythonScript);
            if (!scriptFile.exists()) {
                System.out.println("❌ Python script not found: " + pythonScript);
                tempFile.delete();
                return null;
            }

            // Execute Python script with file input
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("python", pythonScript, tempFile.getAbsolutePath());
            processBuilder.directory(new File(modelsDir));
            processBuilder.redirectErrorStream(true);

            System.out.println("🐍 Starting Python sentiment process...");
            Process process = processBuilder.start();

            // Read output and look for JSON line
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String jsonOutput = null;
            String line;

            System.out.println("🐍 Python output:");
            while ((line = reader.readLine()) != null) {
                System.out.println("   " + line);
                // Look for lines that start with { (JSON)
                if (line.trim().startsWith("{")) {
                    jsonOutput = line.trim();
                    System.out.println("✅ Found sentiment JSON output: " + jsonOutput);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("🐍 Python exit code: " + exitCode);

            // Clean up temp file
            tempFile.delete();

            if (exitCode != 0) {
                System.out.println("❌ Python sentiment script failed with exit code: " + exitCode);
                return null;
            }

            if (jsonOutput == null) {
                System.out.println("❌ No JSON found in sentiment Python output");
                return null;
            }

            try {
                Map<String, Object> result = new com.google.gson.Gson().fromJson(jsonOutput, Map.class);
                System.out.println("✅ Successfully parsed sentiment Python response");
                return result;
            } catch (Exception e) {
                System.out.println("❌ Failed to parse JSON from Python: " + e.getMessage());
                System.out.println("🐍 Raw output was: " + jsonOutput);
                return null;
            }

        } catch (Exception e) {
            System.out.println("❌ Error calling Python sentiment model: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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

    @GetMapping("/test-weather")
    public Map<String, String> testWeather() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Weather Prediction API is working!");
        response.put("status", "success");
        response.put("model", "Trained on auckland_weather.csv");
        return response;
    }
}