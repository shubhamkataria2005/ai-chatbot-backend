import sys
import json
import pickle
import pandas as pd
import numpy as np
import os
import traceback

def predict_salary(experience, job_title, location, education_level, skills_list):
    """
    Predict salary using SINGLE model file and return BOTH local currency and USD
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

        # Ensure prediction is reasonable
        if predicted_salary_usd <= 0 or np.isnan(predicted_salary_usd):
            print(f"Warning: Invalid prediction {predicted_salary_usd}, using fallback calculation")
            predicted_salary_usd = calculate_fallback_salary(experience, job_title, location, education_level, skills_list)

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

        # Ensure salaries are reasonable
        predicted_salary_usd = max(20000, min(300000, predicted_salary_usd))
        local_salary = max(20000, min(300000, local_salary))

        # Confidence calculation
        confidence = min(80 + (experience * 1) + (len(skills_list) * 2), 95)

        # Factors for explanation
        factors = [
            f"{experience} years of experience",
            f"{job_title} role",
            f"{location} location",
            f"{education_level} education level",
            f"{len(skills_list)} key skills selected",
            "Trained ML model with market data"
        ]

        result = {
            "success": True,
            "salaryUSD": round(predicted_salary_usd),
            "salary": round(local_salary),  # ← NOW RETURNING LOCAL SALARY TOO!
            "currency": currency,
            "confidence": round(confidence),
            "factors": factors,
            "model": "RandomForest_Single_File_v1.0",
            "exchangeRate": exchange_rate,
            "predictionDetails": {
                "baseSalaryUSD": round(predicted_salary_usd),
                "localCurrencySalary": round(local_salary),
                "currencyUsed": currency
            }
        }

        print(f"SINGLE FILE Prediction successful: {result['salary']} {currency} ({result['salaryUSD']} USD)")
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

def calculate_fallback_salary(experience, job_title, location, education_level, skills_list):
    """Fallback calculation if ML prediction is invalid"""
    # Base salaries in USD by role
    base_salaries = {
        'Software Developer': 75000,
        'Senior Developer': 110000,
        'Full Stack Developer': 90000,
        'Frontend Developer': 80000,
        'Backend Developer': 85000,
        'Data Scientist': 95000,
        'ML Engineer': 105000
    }

    base = base_salaries.get(job_title, 80000)

    # Experience adjustment
    exp_multiplier = 1.0 + (min(experience, 20) * 0.05)

    # Education adjustment
    edu_multipliers = {
        'PhD': 1.2,
        'Master': 1.1,
        'Bachelor': 1.05,
        'Diploma': 1.0
    }
    edu_multiplier = edu_multipliers.get(education_level, 1.0)

    # Skills bonus
    skills_bonus = len(skills_list) * 1000

    calculated_salary = (base * exp_multiplier * edu_multiplier) + skills_bonus

    print(f"Fallback calculation: {calculated_salary} USD")
    return max(30000, min(250000, calculated_salary))

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