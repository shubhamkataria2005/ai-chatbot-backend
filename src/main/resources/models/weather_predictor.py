import sys
import json
import joblib
import numpy as np
import os

def predict_weather(temperature, humidity, wind_speed, pressure, rainfall):
    try:
        print(f"🌤️ Weather Prediction for:")
        print(f"   Temperature: {temperature}°C")
        print(f"   Humidity: {humidity}%")
        print(f"   Wind Speed: {wind_speed} km/h")
        print(f"   Pressure: {pressure} hPa")
        print(f"   Rainfall: {rainfall} mm")

        # Check current directory and available files
        current_dir = os.getcwd()
        print(f"📁 Current directory: {current_dir}")
        print(f"📁 Files in directory: {[f for f in os.listdir('.') if f.endswith('.pkl')]}")

        # Try different possible model file locations
        possible_paths = [
            'weather_model.pkl',
            './weather_model.pkl',
            'models/weather_model.pkl',
            '../weather_model.pkl',
            'weather_prediction_model.pkl'
        ]

        model_path = None
        for path in possible_paths:
            if os.path.exists(path):
                model_path = path
                print(f"✅ Found model at: {path}")
                break

        if not model_path:
            print("❌ No model file found in any expected location")
            return use_fallback_logic(temperature, humidity, wind_speed, pressure, rainfall)

        print(f"✅ Using model: {model_path}")

        # Load model with joblib
        model_data = joblib.load(model_path)
        print("✅ Model loaded successfully!")

        # Debug: Print what's in the model data
        model_keys = list(model_data.keys())
        print(f"📦 Model contents: {model_keys}")

        # Check if we have the expected model structure
        if 'temperature_model' in model_data:
            model = model_data['temperature_model']
            print("✅ Temperature model found!")

            # Make prediction for temperature
            input_data = [[temperature, humidity, wind_speed, pressure, rainfall]]
            predicted_temp = round(model.predict(input_data)[0], 1)

            # Use simple heuristic for rainfall
            predicted_rain = max(0.0, rainfall * 0.8)

        else:
            print("⚠️ Unexpected model structure, using fallback")
            return use_fallback_logic(temperature, humidity, wind_speed, pressure, rainfall)

        # Determine weather condition
        condition = determine_weather_condition(predicted_temp, predicted_rain)

        print(f"🎯 ML Prediction Results:")
        print(f"   Temperature: {predicted_temp}°C")
        print(f"   Rainfall: {predicted_rain} mm")
        print(f"   Condition: {condition}")

        return {
            "success": True,
            "predictedTemperature": predicted_temp,
            "predictedRainfall": round(predicted_rain, 1),
            "weatherCondition": condition,
            "confidence": 85,
            "source": "ml_model"
        }

    except Exception as e:
        error_msg = f"ML prediction failed: {str(e)}"
        print(f"❌ {error_msg}")
        return use_fallback_logic(temperature, humidity, wind_speed, pressure, rainfall)

def use_fallback_logic(temperature, humidity, wind_speed, pressure, rainfall):
    """Fallback logic when ML model fails"""
    print("🔄 Using fallback prediction logic")

    # Simple rule-based fallback
    if rainfall > 5:
        predicted_rain = rainfall * 1.1
        condition = "Heavy Rain"
    elif rainfall > 1:
        predicted_rain = rainfall * 1.05
        condition = "Light Rain"
    else:
        predicted_rain = max(0.0, rainfall)

    # Temperature adjustment based on conditions
    temp_adjustment = 0
    if humidity > 80:
        temp_adjustment -= 2  # Feels cooler with high humidity
    if wind_speed > 20:
        temp_adjustment -= 3  # Wind chill effect
    if pressure < 1000:
        temp_adjustment -= 1  # Low pressure often means cooler weather
    elif pressure > 1020:
        temp_adjustment += 1  # High pressure often means warmer weather

    predicted_temp = temperature + temp_adjustment

    # Determine condition if not already set by rainfall
    if 'condition' not in locals():
        condition = determine_weather_condition(predicted_temp, predicted_rain)

    print(f"🎯 Fallback Prediction Results:")
    print(f"   Temperature: {predicted_temp}°C")
    print(f"   Rainfall: {predicted_rain} mm")
    print(f"   Condition: {condition}")

    return {
        "success": True,
        "predictedTemperature": round(predicted_temp, 1),
        "predictedRainfall": round(predicted_rain, 1),
        "weatherCondition": condition,
        "confidence": 65,
        "source": "fallback"
    }

def determine_weather_condition(temperature, rainfall):
    """Determine weather condition based on temperature and rainfall"""
    if rainfall > 5:
        return "Heavy Rain"
    elif rainfall > 1:
        return "Light Rain"
    elif temperature > 28:
        return "Hot"
    elif temperature > 22:
        return "Warm"
    elif temperature > 15:
        return "Mild"
    elif temperature > 5:
        return "Cool"
    else:
        return "Cold"

if __name__ == "__main__":
    try:
        # Check if running in test mode
        if len(sys.argv) > 1 and sys.argv[1] == "test":
            test_model_loading()
        else:
            input_json = sys.argv[1]
            data = json.loads(input_json)

            result = predict_weather(
                float(data['temperature']),
                float(data['humidity']),
                float(data['wind_speed']),
                float(data['pressure']),
                float(data['rainfall'])
            )

            print(json.dumps(result))

    except Exception as e:
        error_result = {
            "success": False,
            "error": str(e),
            "predictedTemperature": 0.0,
            "predictedRainfall": 0.0,
            "weatherCondition": "Unknown"
        }
        print(json.dumps(error_result))

def test_model_loading():
    """Test function to check model loading"""
    model_path = 'weather_model.pkl'

    print(f"🧪 Testing model loading...")
    print(f"Checking for model file: {model_path}")
    print(f"File exists: {os.path.exists(model_path)}")
    print(f"Current directory: {os.getcwd()}")
    print(f"Files in current directory: {os.listdir('.')}")

    if os.path.exists(model_path):
        try:
            model_data = joblib.load(model_path)
            print(f"✅ Model loaded successfully!")
            print(f"Model type: {type(model_data)}")
            print(f"Model keys: {list(model_data.keys())}")

            # Test prediction
            if 'temperature_model' in model_data:
                model = model_data['temperature_model']
                test_input = [[21.5, 75.0, 15.0, 1012.0, 0.5]]
                prediction = model.predict(test_input)
                print(f"✅ Test prediction: {prediction[0]}")
            else:
                print("❌ 'temperature_model' key not found in model data")

        except Exception as e:
            print(f"❌ Error loading model: {e}")
    else:
        print("❌ Model file not found")