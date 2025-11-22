package com.Shubham.ai_chatbot_backend.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.io.*;

@Service
public class SalaryPredictionService {

    private String getModelsDirectory() {
        try {
            // Try multiple approaches to find the models directory
            File devModelsDir = new File("src/main/resources/models");
            if (devModelsDir.exists()) {
                System.out.println("📁 Found models in: " + devModelsDir.getAbsolutePath());
                return devModelsDir.getAbsolutePath();
            }

            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL resource = classLoader.getResource("models");
            if (resource != null) {
                String jarPath = new File(resource.toURI()).getAbsolutePath();
                System.out.println("📁 Found models in JAR: " + jarPath);
                return jarPath;
            }

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

    private Map<String, Object> callPythonMLModel(int experience, String jobTitle, String location,
                                                  String educationLevel, List<String> skills) {
        try {
            // Prepare input data for Python script
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("experience", experience);
            inputData.put("role", jobTitle);
            inputData.put("location", location);
            inputData.put("education", educationLevel);
            inputData.put("skills", skills);

            String inputJson = new com.google.gson.Gson().toJson(inputData);

            // Write JSON to a temporary file
            File tempFile = File.createTempFile("salary_input", ".json");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(inputJson);
            }

            System.out.println("📁 Created temp file: " + tempFile.getAbsolutePath());

            // Get the models directory path
            String modelsDir = getModelsDirectory();
            String pythonScript = modelsDir + "/ml_salary_predictor.py";

            System.out.println("📁 Python script path: " + pythonScript);

            // Check if Python script exists
            File scriptFile = new File(pythonScript);
            if (!scriptFile.exists()) {
                System.out.println("❌ Python script not found: " + pythonScript);
                tempFile.delete();
                return null;
            }

            // Create ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("python", pythonScript, tempFile.getAbsolutePath());
            processBuilder.directory(new File(modelsDir));
            processBuilder.redirectErrorStream(true);

            System.out.println("🐍 Starting Python process...");
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
                    System.out.println("✅ Found JSON output: " + jsonOutput);
                }
            }

            // Wait for process to complete
            int exitCode = process.waitFor();
            System.out.println("🐍 Python exit code: " + exitCode);

            // Clean up temp file
            tempFile.delete();

            if (exitCode != 0) {
                System.out.println("❌ Python script failed with exit code: " + exitCode);
                return null;
            }

            if (jsonOutput == null) {
                System.out.println("❌ No JSON found in Python output");
                return null;
            }

            try {
                Map<String, Object> result = new com.google.gson.Gson().fromJson(jsonOutput, Map.class);
                System.out.println("✅ Successfully parsed Python response");
                return result;
            } catch (Exception e) {
                System.out.println("❌ Failed to parse JSON: " + e.getMessage());
                return null;
            }

        } catch (Exception e) {
            System.out.println("❌ Error calling Python ML model: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> predictSalaryWithML(int experience, String jobTitle, String location,
                                                   String educationLevel, List<String> skills) {

        try {
            // Use Python ML model
            Map<String, Object> mlResult = callPythonMLModel(experience, jobTitle, location, educationLevel, skills);

            if (mlResult != null && mlResult.get("success") != null && (boolean) mlResult.get("success")) {
                System.out.println("🤖 Using ML model prediction");
                return mlResult;
            } else {
                System.out.println("❌ ML model failed - no fallback available");
                // Return simple error response since we removed fallback logic
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Salary prediction service temporarily unavailable");
                errorResponse.put("message", "Please try again later or contact support");
                return errorResponse;
            }

        } catch (Exception e) {
            System.out.println("❌ ML integration error: " + e.getMessage());
            // Return simple error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Salary prediction service error");
            errorResponse.put("message", "Please try again later");
            return errorResponse;
        }
    }
}