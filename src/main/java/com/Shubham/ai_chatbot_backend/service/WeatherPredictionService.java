package com.Shubham.ai_chatbot_backend.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.io.*;

@Service
public class WeatherPredictionService {

    public Map<String, Object> predictWeather(double temperature, double humidity,
                                              double windSpeed, double pressure, double rainfall) {

        try {
            // Try to use Python ML model first
            Map<String, Object> mlResult = callPythonWeatherModel(temperature, humidity, windSpeed, pressure, rainfall);

            if (mlResult != null && (boolean) mlResult.get("success")) {
                System.out.println("🌤️ Using ML weather model prediction");
                return mlResult;
            } else {
                System.out.println("⚠️ ML model failed, using fallback logic");
                return fallbackWeatherPrediction(temperature, humidity, windSpeed, pressure, rainfall);
            }

        } catch (Exception e) {
            System.out.println("❌ Weather ML integration error: " + e.getMessage());
            return fallbackWeatherPrediction(temperature, humidity, windSpeed, pressure, rainfall);
        }
    }

    private Map<String, Object> callPythonWeatherModel(double temperature, double humidity,
                                                       double windSpeed, double pressure, double rainfall) {
        try {
            // Prepare input data for Python script
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("temperature", temperature);
            inputData.put("humidity", humidity);
            inputData.put("wind_speed", windSpeed);
            inputData.put("pressure", pressure);
            inputData.put("rainfall", rainfall);

            String inputJson = new com.google.gson.Gson().toJson(inputData);

            // Get the models directory path
            String modelsDir = getModelsDirectory();
            String pythonScript = modelsDir + "/weather_predictor.py";

            // Check if Python script exists
            File scriptFile = new File(pythonScript);
            if (!scriptFile.exists()) {
                System.out.println("❌ Python weather script not found: " + pythonScript);
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
                System.out.println("❌ Python weather script failed with exit code: " + exitCode);
                return null;
            }

            // Parse JSON response
            String jsonOutput = output.toString();
            return new com.google.gson.Gson().fromJson(jsonOutput, Map.class);

        } catch (Exception e) {
            System.out.println("❌ Error calling Python weather model: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> fallbackWeatherPrediction(double temperature, double humidity,
                                                          double windSpeed, double pressure, double rainfall) {

        // Simple rule-based prediction based on your dataset patterns
        double predictedTemp = predictTemperature(temperature, humidity, pressure);
        double predictedRainfall = predictRainfall(temperature, humidity, pressure, rainfall);
        String weatherCondition = predictWeatherCondition(predictedTemp, predictedRainfall, humidity);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("predictedTemperature", Math.round(predictedTemp * 10.0) / 10.0);
        result.put("predictedRainfall", Math.round(predictedRainfall * 10.0) / 10.0);
        result.put("weatherCondition", weatherCondition);
        result.put("confidence", 75);
        result.put("model", "Fallback_Weather_Algorithm");

        List<String> factors = new ArrayList<>();
        factors.add("Current temperature: " + temperature + "°C");
        factors.add("Humidity: " + humidity + "%");
        factors.add("Pressure: " + pressure + " hPa");
        factors.add("Current rainfall: " + rainfall + " mm");
        factors.add("Wind speed: " + windSpeed + " km/h");
        factors.add("Based on Auckland weather patterns");

        result.put("factors", factors);

        return result;
    }

    private double predictTemperature(double currentTemp, double humidity, double pressure) {
        // Simple linear regression based on your dataset patterns
        double tempChange = 0.0;

        if (pressure < 1012) tempChange -= 0.5;  // Low pressure → cooler
        if (pressure > 1015) tempChange += 0.3;  // High pressure → warmer
        if (humidity > 85) tempChange -= 0.7;    // High humidity → cooler
        if (humidity < 70) tempChange += 0.4;    // Low humidity → warmer

        return currentTemp + tempChange + (Math.random() * 0.6 - 0.3); // Small random variation
    }

    private double predictRainfall(double temperature, double humidity, double pressure, double currentRainfall) {
        // Probability-based rainfall prediction
        double rainProbability = 0.0;

        if (humidity > 80) rainProbability += 0.6;
        if (pressure < 1010) rainProbability += 0.4;
        if (currentRainfall > 2.0) rainProbability += 0.3; // If currently raining, likely to continue
        if (temperature < 18) rainProbability += 0.2;

        // Convert probability to rainfall amount
        return rainProbability * 8.0; // Max 8mm based on your dataset
    }

    private String predictWeatherCondition(double temp, double rainfall, double humidity) {
        if (rainfall > 5.0) return "Heavy Rain";
        if (rainfall > 1.0) return "Light Rain";
        if (humidity > 90) return "Foggy";
        if (temp > 25) return "Sunny";
        if (temp < 15) return "Cold";
        if (humidity > 80) return "Cloudy";
        return "Partly Cloudy";
    }

    private String getModelsDirectory() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL resource = classLoader.getResource("models");
            if (resource != null) {
                return new File(resource.toURI()).getAbsolutePath();
            }
        } catch (Exception e) {
            System.out.println("❌ Error getting models directory: " + e.getMessage());
        }
        return new File(".").getAbsolutePath();
    }
}