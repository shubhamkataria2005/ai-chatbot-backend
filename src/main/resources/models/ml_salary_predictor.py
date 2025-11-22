import sys
import json
import pickle
import pandas as pd
import numpy as np
import os
import traceback

def predict_salary(experience, job_title, location, education_level, skills_list):
    """
    Predict salary using SINGLE model file
    """
    try:
        print("Starting salary prediction (SINGLE FILE)...")
        print(f"   Experience: {experience}, Role: {job_title}, Location: {location}")

        # Get the directory where this script is located
        script_dir = os.path.dirname(os.path.abspath(__file__))
        model_file = os.path.join(script_dir, 'salary_predictor_single.pkl')

        print(f"Loading single model file: {model_file}")

        if not os.path.exists(model_file):
            return {
                "success": False,
                "error": "Single model file not found",
                "message": "Please make sure salary_predictor_single.pkl exists"
            }

        # Load SINGLE model package
        with open(model_file, 'rb') as f:
            model_package = pickle.load(f)

        print("Single model file loaded successfully")

        # Extract components
        model = model_package['model']
        scaler = model_package['scaler']
        label_encoders = model_package['label_encoders']
        feature_columns = model_package['feature_columns']
        exchange_rates = model_package['exchange_rates']
        skills_columns = model_package['skills_columns']

        # Prepare input data
        input_data = {}

        # Basic features
        input_data['experience_years'] = experience

        # Encode categorical variables
        try:
            if 'job_title' in label_encoders:
                input_data['job_title_encoded'] = label_encoders['job_title'].transform([job_title])[0]
            if 'location' in label_encoders:
                input_data['location_encoded'] = label_encoders['location'].transform([location])[0]
            if 'education_level' in label_encoders:
                input_data['education_level_encoded'] = label_encoders['education_level'].transform([education_level])[0]
            if 'company_size' in label_encoders:
                input_data['company_size_encoded'] = label_encoders['company_size'].transform(['Medium'])[0]
        except Exception as e:
            return {
                "success": False,
                "error": f"Category not found in training data: {str(e)}",
                "message": "Please use job titles, locations, and education levels that exist in the training data"
            }

        # Skills features
        for skill in skills_columns:
            input_data[skill] = 1 if any(skill.lower() in s.lower() for s in skills_list) else 0

        # Create feature array
        features = [input_data.get(col, 0) for col in feature_columns]
        features_scaled = scaler.transform([features])

        # Make prediction (USD)
        predicted_salary_usd = model.predict(features_scaled)[0]

        # Convert to local currency
        currency_map = {
            'United States': 'USD',
            'New Zealand': 'NZD',
            'India': 'INR',
            'United Kingdom': 'GBP',
            'Germany': 'EUR',
            'Canada': 'CAD',
            'Australia': 'AUD'
        }

        currency = currency_map.get(location, 'USD')
        exchange_rate = exchange_rates.get(currency, 1.0)
        local_salary = predicted_salary_usd * exchange_rate

        # Confidence
        confidence = min(80 + (experience * 1) + (len(skills_list) * 2), 95)

        # Factors
        factors = [
            f"{experience} years of experience",
            f"{job_title} role",
            f"{location} location",
            f"{education_level} education level",
            f"{len(skills_list)} key skills selected",
            "Single file ML model"
        ]

        result = {
            "success": True,
            "salaryUSD": round(predicted_salary_usd),
            "salary": round(local_salary),
            "currency": currency,
            "confidence": round(confidence),
            "factors": factors,
            "model": "RandomForest_Single_File_v1.0"
        }

        print(f"SINGLE FILE Prediction successful: {result['salary']} {currency}")
        return result

    except Exception as e:
        error_msg = f"Single file prediction failed: {str(e)}"
        print(f"ERROR: {error_msg}")
        print("Full traceback:")
        traceback.print_exc()
        return {
            "success": False,
            "error": error_msg,
            "message": "Unexpected error during prediction"
        }

if __name__ == "__main__":
    try:
        print("Python script started successfully")

        # Read input from file (passed as command line argument)
        if len(sys.argv) > 1:
            input_file = sys.argv[1]
            print(f"Reading input from file: {input_file}")

            with open(input_file, 'r') as f:
                input_data = json.load(f)
        else:
            print("No input file provided, using test data")
            input_data = {
                'experience': 3,
                'role': 'Software Developer',
                'location': 'New Zealand',
                'education': 'Bachelor',
                'skills': ['JavaScript', 'React']
            }

        experience = input_data['experience']
        job_title = input_data['role']
        location = input_data['location']
        education_level = input_data['education']
        skills_list = input_data['skills']

        print(f"Input data: {experience}yrs, {job_title}, {location}, {education_level}, skills: {skills_list}")

        # Make prediction
        result = predict_salary(experience, job_title, location, education_level, skills_list)

        # Output result as JSON - THIS MUST BE THE LAST LINE!
        print(json.dumps(result))

    except Exception as e:
        error_msg = f"Script execution failed: {str(e)}"
        print(f"ERROR: {error_msg}")
        print("Full traceback:")
        traceback.print_exc()

        error_result = {
            "success": False,
            "error": error_msg
        }
        print(json.dumps(error_result))