package com.Shubham.ai_chatbot_backend.service;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import java.util.*;
import java.io.*;

@Service
public class AIToolsService {

    private final Gson gson = new Gson();

    public Map<String, Object> callPythonSentimentModel(String text) {
        try {
            System.out.println("🔍 Attempting ML sentiment analysis...");
            Map<String, Object> mlResult = callPythonSentimentML(text);

            if (isValidSentimentResult(mlResult)) {
                System.out.println("✅ Using ML sentiment model");
                return mlResult;
            } else {
                System.out.println("❌ ML sentiment model failed");
                return createSentimentErrorResponse("ML sentiment analysis unavailable");
            }

        } catch (Exception e) {
            System.out.println("❌ ML sentiment error: " + e.getMessage());
            return createSentimentErrorResponse("Sentiment analysis service error");
        }
    }

    private boolean isValidSentimentResult(Map<String, Object> mlResult) {
        if (mlResult == null) {
            System.out.println("❌ Sentiment ML result is null");
            return false;
        }

        if (mlResult.get("success") == null || !(boolean) mlResult.get("success")) {
            System.out.println("❌ Sentiment ML result indicates failure");
            return false;
        }

        // Check for required sentiment fields
        Object sentimentObj = mlResult.get("sentiment");
        Object confidenceObj = mlResult.get("confidence");

        if (sentimentObj == null || confidenceObj == null) {
            System.out.println("❌ Sentiment or confidence values are null");
            return false;
        }

        try {
            String sentiment = (String) sentimentObj;
            double confidence = ((Number) confidenceObj).doubleValue();

            // Validate sentiment values
            Set<String> validSentiments = Set.of("positive", "negative", "neutral");
            if (!validSentiments.contains(sentiment.toLowerCase())) {
                System.out.println("❌ Invalid sentiment value: " + sentiment);
                return false;
            }

            if (confidence < 0 || confidence > 100 || Double.isNaN(confidence)) {
                System.out.println("❌ Invalid confidence value: " + confidence);
                return false;
            }

            System.out.println("✅ Valid ML sentiment result: " + sentiment + " (" + confidence + "%)");
            return true;

        } catch (ClassCastException e) {
            System.out.println("❌ Sentiment values are invalid types: " + e.getMessage());
            return false;
        }
    }

    private Map<String, Object> createSentimentErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "ML sentiment analysis unavailable");
        errorResponse.put("message", message);
        errorResponse.put("suggestion", "Please ensure sentiment_model.pkl and required Python dependencies are installed");
        return errorResponse;
    }

    private Map<String, Object> callPythonSentimentML(String text) {
        try {
            Map<String, Object> inputData = Map.of("text", text);
            String inputJson = gson.toJson(inputData);

            File tempFile = File.createTempFile("sentiment_input", ".json");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(inputJson);
            }

            System.out.println("📁 Created temp file: " + tempFile.getAbsolutePath());

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

            // Check if sentiment model file exists
            File modelFile = new File(modelsDir + "/sentiment_model.pkl");
            if (!modelFile.exists()) {
                System.out.println("❌ Sentiment model file not found: " + modelFile.getAbsolutePath());
                tempFile.delete();
                return null;
            }

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("/opt/venv/bin/python", pythonScript, tempFile.getAbsolutePath());
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

            if (exitCode != 0 || jsonOutput == null) {
                System.out.println("❌ Python sentiment script failed with exit code: " + exitCode);
                return null;
            }

            try {
                Map<String, Object> result = gson.fromJson(jsonOutput, Map.class);
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

    // Rest of the existing methods remain the same...
    public Map<String, Object> getAvailableTools() {
        Map<String, Object> tools = new HashMap<>();
        List<Map<String, String>> toolList = new ArrayList<>();

        toolList.add(createToolInfo(
                "Salary Prediction",
                "/api/ai-tools/salary-prediction",
                "Predict tech salaries based on experience, role, location, and skills",
                "POST"
        ));

        toolList.add(createToolInfo(
                "Sentiment Analysis",
                "/api/ai-tools/sentiment-analysis",
                "Analyze text sentiment using ML model",
                "POST"
        ));

        toolList.add(createToolInfo(
                "Weather Prediction",
                "/api/ai-tools/weather-prediction",
                "Predict weather conditions based on current parameters",
                "POST"
        ));

        toolList.add(createToolInfo(
                "Car Recognition",
                "/api/ai-tools/car-recognition",
                "Identify car brands from images using TensorFlow CNN",
                "POST"
        ));

        tools.put("tools", toolList);
        tools.put("total_tools", toolList.size());
        tools.put("last_updated", new Date().toString());

        return tools;
    }

    public Map<String, Object> getHealthStatus() {
        return Map.of(
                "status", "healthy",
                "timestamp", new Date().toString(),
                "services", Map.of(
                        "salary_prediction", "active",
                        "sentiment_analysis", "active",
                        "weather_prediction", "active",
                        "car_recognition", "active",
                        "authentication", "active"
                ),
                "version", "1.0.0"
        );
    }

    public Map<String, String> getHomeInfo() {
        return Map.of(
                "message", "AI Chatbot Backend is running!",
                "status", "OK",
                "timestamp", new Date().toString(),
                "version", "1.0.0"
        );
    }

    private Map<String, String> createToolInfo(String name, String endpoint, String description, String method) {
        Map<String, String> tool = new HashMap<>();
        tool.put("name", name);
        tool.put("endpoint", endpoint);
        tool.put("description", description);
        tool.put("method", method);
        tool.put("status", "active");
        return tool;
    }

    private String getModelsDirectory() {
        try {
            File devModelsDir = new File("src/main/resources/models");
            if (devModelsDir.exists()) {
                return devModelsDir.getAbsolutePath();
            }

            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL resource = classLoader.getResource("models");
            if (resource != null) {
                return new File(resource.toURI()).getAbsolutePath();
            }

            File currentDir = new File("models");
            if (currentDir.exists()) {
                return currentDir.getAbsolutePath();
            }

        } catch (Exception e) {
            System.out.println("❌ Error finding models directory: " + e.getMessage());
        }

        return new File(".").getAbsolutePath();
    }
}