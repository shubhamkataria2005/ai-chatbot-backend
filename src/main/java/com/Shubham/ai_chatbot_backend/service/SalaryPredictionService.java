package com.Shubham.ai_chatbot_backend.service;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import java.util.*;
import java.io.*;

@Service
public class SalaryPredictionService {

    private final Gson gson = new Gson();

    public Map<String, Object> predictSalaryWithML(int experience, String jobTitle, String location,
                                                   String educationLevel, List<String> skills) {

        try {
            Map<String, Object> mlResult = callPythonMLModel(experience, jobTitle, location, educationLevel, skills);

            if (mlResult != null && mlResult.get("success") != null && (boolean) mlResult.get("success")) {
                System.out.println("🤖 Using ML model prediction");
                return mlResult;
            } else {
                System.out.println("🔄 ML model failed, using fallback prediction");
                return fallbackSalaryPrediction(experience, jobTitle, location, educationLevel, skills);
            }

        } catch (Exception e) {
            System.out.println("❌ ML integration error, using fallback: " + e.getMessage());
            return fallbackSalaryPrediction(experience, jobTitle, location, educationLevel, skills);
        }
    }

    private Map<String, Object> fallbackSalaryPrediction(int experience, String jobTitle, String location,
                                                         String educationLevel, List<String> skills) {
        try {
            System.out.println("💰 Using fallback salary prediction");

            // Base salary calculation with intelligent rules
            double baseSalary = calculateBaseSalary(jobTitle, location);

            // Experience multiplier (5-15% per year)
            double experienceBonus = baseSalary * (experience * 0.08);

            // Education multiplier
            double educationMultiplier = getEducationMultiplier(educationLevel);

            // Skills bonus
            double skillsBonus = calculateSkillsBonus(skills, baseSalary);

            // Location adjustment
            double locationAdjustment = getLocationAdjustment(location);

            // Calculate final salary
            double predictedSalary = (baseSalary + experienceBonus) * educationMultiplier + skillsBonus;
            predictedSalary *= locationAdjustment;

            // Add some randomness (±5%)
            Random random = new Random();
            double variation = 0.95 + (random.nextDouble() * 0.1); // 95% to 105%
            predictedSalary *= variation;

            // Currency conversion
            String currency = getCurrencyForLocation(location);
            double salaryUSD = predictedSalary / getExchangeRate(currency);

            // Confidence based on input quality
            int confidence = calculateConfidence(experience, skills.size());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("salary", Math.round(predictedSalary));
            result.put("salaryUSD", Math.round(salaryUSD));
            result.put("currency", currency);
            result.put("confidence", confidence);
            result.put("model", "Intelligent_Fallback_v2.0");
            result.put("ml_model_status", "fallback_used");

            List<String> factors = new ArrayList<>();
            factors.add("Base role: " + jobTitle + " (" + Math.round(baseSalary) + " " + currency + ")");
            factors.add("Experience: " + experience + " years (+" + Math.round(experienceBonus) + " " + currency + ")");
            factors.add("Education: " + educationLevel + " (x" + educationMultiplier + ")");
            factors.add("Skills: " + skills.size() + " relevant technologies (+" + Math.round(skillsBonus) + " " + currency + ")");
            factors.add("Location adjustment: " + (locationAdjustment > 1 ? "High" : "Normal") + " cost area");
            factors.add("Market trends and industry standards");

            result.put("factors", factors);
            result.put("note", "Based on industry averages and market research");

            return result;

        } catch (Exception e) {
            System.out.println("❌ Fallback salary prediction failed: " + e.getMessage());
            return createErrorResponse("Salary prediction service temporarily unavailable");
        }
    }

    private double calculateBaseSalary(String jobTitle, String location) {
        // Base salaries by role (in local currency)
        Map<String, Double> baseSalaries = Map.of(
                "Software Developer", 70000.0,
                "Senior Developer", 95000.0,
                "Full Stack Developer", 85000.0,
                "Frontend Developer", 75000.0,
                "Backend Developer", 80000.0,
                "Data Scientist", 90000.0,
                "ML Engineer", 95000.0,
                "DevOps Engineer", 85000.0,
                "Product Manager", 100000.0
        );

        double base = baseSalaries.getOrDefault(jobTitle, 75000.0);

        // Location adjustments
        Map<String, Double> locationMultipliers = Map.of(
                "United States", 1.2,
                "United Kingdom", 1.1,
                "Germany", 1.0,
                "Canada", 1.0,
                "Australia", 1.0,
                "New Zealand", 0.9,
                "India", 0.4
        );

        return base * locationMultipliers.getOrDefault(location, 0.8);
    }

    private double getEducationMultiplier(String educationLevel) {
        Map<String, Double> multipliers = Map.of(
                "PhD", 1.3,
                "Master", 1.2,
                "Bachelor", 1.1,
                "Diploma", 1.0,
                "High School", 0.9
        );
        return multipliers.getOrDefault(educationLevel, 1.0);
    }

    private double calculateSkillsBonus(List<String> skills, double baseSalary) {
        // High-value skills
        Set<String> highValueSkills = Set.of(
                "machine learning", "ai", "tensorflow", "pytorch", "aws", "azure",
                "docker", "kubernetes", "react", "node.js", "python", "java"
        );

        long highValueCount = skills.stream()
                .filter(skill -> highValueSkills.contains(skill.toLowerCase()))
                .count();

        return baseSalary * 0.02 * highValueCount; // 2% per high-value skill
    }

    private double getLocationAdjustment(String location) {
        Map<String, Double> adjustments = Map.of(
                "San Francisco", 1.4,
                "New York", 1.3,
                "London", 1.2,
                "Sydney", 1.1,
                "Auckland", 1.0,
                "Berlin", 1.0,
                "Bangalore", 0.8
        );
        return adjustments.getOrDefault(location, 1.0);
    }

    private String getCurrencyForLocation(String location) {
        Map<String, String> currencies = Map.of(
                "United States", "USD",
                "New Zealand", "NZD",
                "India", "INR",
                "United Kingdom", "GBP",
                "Germany", "EUR",
                "Canada", "CAD",
                "Australia", "AUD"
        );
        return currencies.getOrDefault(location, "USD");
    }

    private double getExchangeRate(String currency) {
        Map<String, Double> rates = Map.of(
                "USD", 1.0,
                "NZD", 0.62,
                "INR", 0.012,
                "GBP", 1.27,
                "EUR", 1.09,
                "CAD", 0.74,
                "AUD", 0.66
        );
        return rates.getOrDefault(currency, 1.0);
    }

    private int calculateConfidence(int experience, int skillsCount) {
        int baseConfidence = 70;
        int expBonus = Math.min(experience * 2, 15); // max 15% for experience
        int skillsBonus = Math.min(skillsCount * 3, 10); // max 10% for skills
        return Math.min(baseConfidence + expBonus + skillsBonus, 90);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Service temporarily unavailable");
        errorResponse.put("message", message);
        errorResponse.put("suggestion", "Please try again in a few moments");
        return errorResponse;
    }

    // Existing ML model calling method (keep as is)
    private Map<String, Object> callPythonMLModel(int experience, String jobTitle, String location,
                                                  String educationLevel, List<String> skills) {
        try {
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("experience", experience);
            inputData.put("role", jobTitle);
            inputData.put("location", location);
            inputData.put("education", educationLevel);
            inputData.put("skills", skills);

            String inputJson = gson.toJson(inputData);

            File tempFile = File.createTempFile("salary_input", ".json");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(inputJson);
            }

            String modelsDir = getModelsDirectory();
            String pythonScript = modelsDir + "/ml_salary_predictor.py";

            File scriptFile = new File(pythonScript);
            if (!scriptFile.exists()) {
                tempFile.delete();
                return null;
            }

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("/opt/venv/bin/python", pythonScript, tempFile.getAbsolutePath());
            processBuilder.directory(new File(modelsDir));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String jsonOutput = null;
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("{")) {
                    jsonOutput = line.trim();
                    break;
                }
            }

            int exitCode = process.waitFor();
            tempFile.delete();

            if (exitCode != 0 || jsonOutput == null) {
                return null;
            }

            return gson.fromJson(jsonOutput, Map.class);

        } catch (Exception e) {
            System.out.println("❌ Error calling Python ML model: " + e.getMessage());
            return null;
        }
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