package com.Shubham.ai_chatbot_backend.service;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.io.*;

@Service
public class CarRecognitionService {

    private final Gson gson = new Gson();

    public Map<String, Object> recognizeCar(MultipartFile imageFile) {
        try {
            return callPythonCarModel(imageFile);
        } catch (Exception e) {
            System.out.println("Car recognition error: " + e.getMessage());
            return Map.of(
                    "success", false,
                    "error", "Car recognition failed: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> callPythonCarModel(MultipartFile imageFile) throws Exception {
        // Save image temporarily
        File tempFile = File.createTempFile("car_image", ".jpg");
        imageFile.transferTo(tempFile);

        // Get models directory
        String modelsDir = getModelsDirectory();
        String pythonScript = modelsDir + "/car_recognition.py";

        // Check if files exist
        File scriptFile = new File(pythonScript);
        File modelFile = new File(modelsDir + "/car_model.h5");

        if (!scriptFile.exists() || !modelFile.exists()) {
            tempFile.delete();
            return Map.of("success", false, "error", "Model files not found");
        }

        // Prepare input data
        Map<String, Object> inputData = Map.of("image_path", tempFile.getAbsolutePath());
        String inputJson = gson.toJson(inputData);

        // Write JSON to temporary file
        File inputJsonFile = File.createTempFile("car_input", ".json");
        try (FileWriter writer = new FileWriter(inputJsonFile)) {
            writer.write(inputJson);
        }

        // Execute Python script
        ProcessBuilder processBuilder = new ProcessBuilder("/opt/venv/bin/python", pythonScript, inputJsonFile.getAbsolutePath());
        processBuilder.directory(new File(modelsDir));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Read output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String jsonOutput = null;
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("{")) {
                jsonOutput = line.trim();
                break;
            }
        }

        // Clean up
        tempFile.delete();
        inputJsonFile.delete();

        int exitCode = process.waitFor();
        if (exitCode != 0 || jsonOutput == null) {
            return Map.of("success", false, "error", "Python script execution failed");
        }

        return gson.fromJson(jsonOutput, Map.class);
    }

    private String getModelsDirectory() {
        String[] possiblePaths = {
                "src/main/resources/models",
                "target/classes/models",
                "models",
                "."
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists()) {
                return dir.getAbsolutePath();
            }
        }
        return new File(".").getAbsolutePath();
    }
}