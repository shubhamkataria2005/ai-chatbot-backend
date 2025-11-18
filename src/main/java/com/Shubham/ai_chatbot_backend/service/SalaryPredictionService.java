package com.Shubham.ai_chatbot_backend.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.io.*;

@Service
public class SalaryPredictionService {

    public Map<String, Object> predictSalaryWithML(int experience, String jobTitle, String location,
                                                   String educationLevel, List<String> skills) {

        try {
            // Try to use Python ML model first
            Map<String, Object> mlResult = callPythonMLModel(experience, jobTitle, location, educationLevel, skills);

            if (mlResult != null && (boolean) mlResult.get("success")) {
                System.out.println("🤖 Using actual ML model prediction");
                return mlResult;
            } else {
                System.out.println("⚠️ ML model failed, using fallback logic");
                return fallbackPrediction(experience, jobTitle, location, educationLevel, skills);
            }

        } catch (Exception e) {
            System.out.println("❌ ML integration error: " + e.getMessage());
            return fallbackPrediction(experience, jobTitle, location, educationLevel, skills);
        }
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

            // Get the models directory path
            String modelsDir = getModelsDirectory();
            String pythonScript = modelsDir + "/ml_salary_predictor.py";

            // Check if Python script exists
            File scriptFile = new File(pythonScript);
            if (!scriptFile.exists()) {
                System.out.println("❌ Python script not found: " + pythonScript);
                return null;
            }

            // Execute Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python3", pythonScript, inputJson
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
            System.out.println("❌ Error calling Python ML model: " + e.getMessage());
            return null;
        }
    }

    private String getModelsDirectory() {
        try {
            // Get the directory where models are stored
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL resource = classLoader.getResource("models");
            if (resource != null) {
                return new File(resource.toURI()).getAbsolutePath();
            }
        } catch (Exception e) {
            System.out.println("❌ Error getting models directory: " + e.getMessage());
        }

        // Fallback to current directory
        return new File(".").getAbsolutePath();
    }

    private Map<String, Object> fallbackPrediction(int experience, String jobTitle, String location,
                                                   String educationLevel, List<String> skills) {
        // Your existing fallback logic
        Map<String, Double> roleBaseSalaries = Map.of(
                "Software Developer", 75000.0,
                "Data Scientist", 90000.0,
                "ML Engineer", 95000.0,
                "DevOps Engineer", 85000.0,
                "Frontend Developer", 70000.0,
                "Backend Developer", 80000.0,
                "Full Stack Developer", 78000.0
        );

        Map<String, Double> locationMultipliers = Map.of(
                "United States", 1.0,
                "New Zealand", 0.75,
                "India", 0.25,
                "United Kingdom", 0.85,
                "Germany", 0.90,
                "Canada", 0.80,
                "Australia", 0.82
        );

        Map<String, Double> educationMultipliers = Map.of(
                "Bachelor", 1.0,
                "Master", 1.15,
                "PhD", 1.3
        );

        double skillsBonus = 1.0 + (skills.size() * 0.02);
        double experienceMultiplier = 1.0 + (experience * 0.07) + (Math.pow(experience, 0.7) * 0.03);

        double baseSalary = roleBaseSalaries.getOrDefault(jobTitle, 70000.0);
        double locationMultiplier = locationMultipliers.getOrDefault(location, 0.8);
        double educationMultiplier = educationMultipliers.getOrDefault(educationLevel, 1.0);

        double predictedSalaryUSD = baseSalary * experienceMultiplier * locationMultiplier * educationMultiplier * skillsBonus;

        Map<String, Object> result = convertToLocalCurrency(predictedSalaryUSD, location);

        List<String> factors = new ArrayList<>();
        factors.add(experience + " years of experience");
        factors.add(jobTitle + " role");
        factors.add(location + " location");
        factors.add(educationLevel + " education level");
        factors.add(skills.size() + " key skills");
        factors.add("Fallback algorithm (ML model not available)");

        result.put("factors", factors);
        result.put("confidence", 75); // Lower confidence for fallback
        result.put("model", "Fallback_Algorithm");

        return result;
    }

    private Map<String, Object> convertToLocalCurrency(double salaryUSD, String location) {
        Map<String, String> currencies = Map.of(
                "United States", "USD",
                "New Zealand", "NZD",
                "India", "INR",
                "United Kingdom", "GBP",
                "Germany", "EUR",
                "Canada", "CAD",
                "Australia", "AUD"
        );

        Map<String, Double> exchangeRates = Map.of(
                "USD", 1.0,
                "NZD", 1.62,
                "INR", 83.0,
                "GBP", 0.79,
                "EUR", 0.92,
                "CAD", 1.35,
                "AUD", 1.52
        );

        String currency = currencies.getOrDefault(location, "USD");
        double exchangeRate = exchangeRates.getOrDefault(currency, 1.0);
        double localSalary = salaryUSD * exchangeRate;

        Map<String, Object> result = new HashMap<>();
        result.put("salary", (int) localSalary);
        result.put("currency", currency);
        result.put("salaryUSD", (int) salaryUSD);

        return result;
    }
}