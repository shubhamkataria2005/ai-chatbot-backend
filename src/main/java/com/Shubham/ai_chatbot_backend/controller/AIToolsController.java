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
// @CrossOrigin(origins = "http://localhost:3000") - Commented for Railway deployment
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

            // Approach 4: Check target directory (Maven build)
            File targetModelsDir = new File("target/classes/models");
            if (targetModelsDir.exists()) {
                System.out.println("📁 Found models in target: " + targetModelsDir.getAbsolutePath());
                return targetModelsDir.getAbsolutePath();
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
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", new Date().toString());
        health.put("services", Map.of(
                "salary_prediction", "active",
                "sentiment_analysis", "active",
                "weather_prediction", "active",
                "car_recognition", "active",
                "authentication", "active"
        ));
        health.put("version", "1.0.0");
        return health;
    }

    // Get available AI tools
    @GetMapping("/tools")
    public Map<String, Object> getAvailableTools() {
        Map<String, Object> tools = new HashMap<>();

        List<Map<String, String>> toolList = new ArrayList<>();

        // Salary Prediction Tool
        Map<String, String> salaryTool = new HashMap<>();
        salaryTool.put("name", "Salary Prediction");
        salaryTool.put("endpoint", "/api/ai-tools/salary-prediction");
        salaryTool.put("description", "Predict tech salaries based on experience, role, location, and skills");
        salaryTool.put("method", "POST");
        salaryTool.put("status", "active");
        toolList.add(salaryTool);

        // Sentiment Analysis Tool
        Map<String, String> sentimentTool = new HashMap<>();
        sentimentTool.put("name", "Sentiment Analysis");
        sentimentTool.put("endpoint", "/api/ai-tools/sentiment-analysis");
        sentimentTool.put("description", "Analyze text sentiment using ML model");
        sentimentTool.put("method", "POST");
        sentimentTool.put("status", "active");
        toolList.add(sentimentTool);

        // Weather Prediction Tool
        Map<String, String> weatherTool = new HashMap<>();
        weatherTool.put("name", "Weather Prediction");
        weatherTool.put("endpoint", "/api/ai-tools/weather-prediction");
        weatherTool.put("description", "Predict weather conditions based on current parameters");
        weatherTool.put("method", "POST");
        weatherTool.put("status", "active");
        toolList.add(weatherTool);

        // Car Recognition Tool
        Map<String, String> carTool = new HashMap<>();
        carTool.put("name", "Car Recognition");
        carTool.put("endpoint", "/api/ai-tools/car-recognition");
        carTool.put("description", "Identify car brands from images using TensorFlow CNN");
        carTool.put("method", "POST");
        carTool.put("status", "active");
        toolList.add(carTool);

        // Image Analysis Tool
        Map<String, String> imageTool = new HashMap<>();
        imageTool.put("name", "Image Analysis");
        imageTool.put("endpoint", "/api/ai-tools/image-analysis");
        imageTool.put("description", "Basic image analysis and information extraction");
        imageTool.put("method", "POST");
        imageTool.put("status", "active");
        toolList.add(imageTool);

        tools.put("tools", toolList);
        tools.put("total_tools", toolList.size());
        tools.put("last_updated", new Date().toString());

        return tools;
    }

    // Root endpoint for Railway health checks
    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "AI Chatbot Backend is running on Railway!");
        response.put("status", "OK");
        response.put("timestamp", new Date().toString());
        response.put("version", "1.0.0");
        return response;
    }
}