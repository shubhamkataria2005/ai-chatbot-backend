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
            Map<String, Object> mlResult = callPythonSentimentML(text);

            if (mlResult != null && mlResult.get("success") != null &&
                    (boolean) mlResult.get("success")) {
                return mlResult;
            } else {
                System.out.println("🔄 ML sentiment model failed, using fallback");
                return fallbackSentimentAnalysis(text);
            }

        } catch (Exception e) {
            System.out.println("❌ ML sentiment error, using fallback: " + e.getMessage());
            return fallbackSentimentAnalysis(text);
        }
    }

    private Map<String, Object> fallbackSentimentAnalysis(String text) {
        try {
            System.out.println("😊 Using fallback sentiment analysis");

            // Simple rule-based sentiment analysis
            String lowerText = text.toLowerCase();

            // Positive words and phrases
            Set<String> positiveWords = Set.of(
                    "good", "great", "excellent", "amazing", "wonderful", "fantastic",
                    "love", "like", "awesome", "brilliant", "perfect", "happy",
                    "pleased", "satisfied", "outstanding", "superb", "nice", "best",
                    "beautiful", "thanks", "thank you", "appreciate", "recommend"
            );

            // Negative words and phrases
            Set<String> negativeWords = Set.of(
                    "bad", "terrible", "awful", "horrible", "hate", "dislike",
                    "worst", "poor", "disappointing", "annoying", "frustrating",
                    "angry", "upset", "sad", "unhappy", "disgusting", "rubbish",
                    "waste", "useless", "broken", "problem", "issue", "complaint"
            );

            // Neutral/negation words
            Set<String> negationWords = Set.of("not", "no", "never", "none", "nothing");

            // Count sentiment indicators
            int positiveCount = 0;
            int negativeCount = 0;
            boolean hasNegation = false;

            String[] words = lowerText.split("\\s+");
            for (int i = 0; i < words.length; i++) {
                String word = words[i].replaceAll("[^a-zA-Z]", "");

                if (positiveWords.contains(word)) {
                    // Check for negation before positive word
                    if (i > 0 && negationWords.contains(words[i-1].toLowerCase())) {
                        negativeCount++;
                        hasNegation = true;
                    } else {
                        positiveCount++;
                    }
                }

                if (negativeWords.contains(word)) {
                    // Check for negation before negative word (creates positive)
                    if (i > 0 && negationWords.contains(words[i-1].toLowerCase())) {
                        positiveCount++;
                        hasNegation = true;
                    } else {
                        negativeCount++;
                    }
                }
            }

            // Determine sentiment
            String sentiment;
            double confidence;

            if (positiveCount > negativeCount) {
                sentiment = "positive";
                confidence = 0.6 + (positiveCount * 0.1);
            } else if (negativeCount > positiveCount) {
                sentiment = "negative";
                confidence = 0.6 + (negativeCount * 0.1);
            } else {
                sentiment = "neutral";
                confidence = 0.5;
            }

            // Adjust confidence based on text length and clarity
            confidence = Math.min(confidence + (text.length() > 20 ? 0.1 : 0), 0.85);
            if (hasNegation) confidence += 0.05;

            // Text complexity analysis
            int wordCount = words.length;
            String complexity = wordCount < 10 ? "simple" : wordCount < 25 ? "moderate" : "complex";

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sentiment", sentiment);
            result.put("confidence", Math.round(confidence * 100));
            result.put("analysis", String.format(
                    "The text shows %s sentiment with %.0f%% confidence",
                    sentiment, confidence * 100
            ));
            result.put("textLength", text.length());
            result.put("wordCount", wordCount);
            result.put("positiveIndicators", positiveCount);
            result.put("negativeIndicators", negativeCount);
            result.put("complexity", complexity);
            result.put("model", "RuleBased_Sentiment_Fallback_v1.0");
            result.put("ml_model_status", "fallback_used");

            return result;

        } catch (Exception e) {
            System.out.println("❌ Fallback sentiment analysis failed: " + e.getMessage());
            return createSentimentErrorResponse("Sentiment analysis service temporarily unavailable");
        }
    }

    private Map<String, Object> createSentimentErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Analysis service unavailable");
        errorResponse.put("message", message);
        errorResponse.put("suggestion", "Please try again with different text");
        return errorResponse;
    }

    // Existing ML sentiment method (keep as is)
    private Map<String, Object> callPythonSentimentML(String text) {
        try {
            Map<String, Object> inputData = Map.of("text", text);
            String inputJson = gson.toJson(inputData);

            File tempFile = File.createTempFile("sentiment_input", ".json");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(inputJson);
            }

            String modelsDir = getModelsDirectory();
            String pythonScript = modelsDir + "/sentiment_predictor.py";

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
            System.out.println("❌ Error calling Python sentiment model: " + e.getMessage());
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