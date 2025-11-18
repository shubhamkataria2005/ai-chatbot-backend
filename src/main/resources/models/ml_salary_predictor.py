import sys
import json
import pickle
import pandas as pd
import numpy as np
import os

def predict_salary(experience, job_title, location, education_level, skills_list):
    """
    Predict salary using the trained ML model
    """
    try:
        # Get the directory where this script is located
        script_dir = os.path.dirname(os.path.abspath(__file__))
        models_dir = os.path.join(script_dir, 'models')

        # Load the model and preprocessing objects
        with open(os.path.join(models_dir, 'salary_predictor_model.pkl'), 'rb') as f:
            model = pickle.load(f)

        with open(os.path.join(models_dir, 'scaler.pkl'), 'rb') as f:
            scaler = pickle.load(f)

        with open(os.path.join(models_dir, 'label_encoders.pkl'), 'rb') as f:
            label_encoders = pickle.load(f)

        with open(os.path.join(models_dir, 'feature_columns.pkl'), 'rb') as f:
            feature_columns = pickle.load(f)

        with open(os.path.join(models_dir, 'exchange_rates.pkl'), 'rb') as f:
            exchange_rates = pickle.load(f)

        # Prepare input data
        input_data = {}

        # Encode categorical variables
        input_data['experience_years'] = experience
        input_data['job_title_encoded'] = label_encoders['job_title'].transform([job_title])[0]
        input_data['location_encoded'] = label_encoders['location'].transform([location])[0]
        input_data['education_level_encoded'] = label_encoders['education_level'].transform([education_level])[0]
        input_data['company_size_encoded'] = label_encoders['company_size'].transform(['Large'])[0]  # Default to Large

        # Skills features (from your Colab training)
        skills_columns = ['JavaScript', 'React', 'Node.js', 'Python', 'AWS', 'Docker',
                         'ML', 'AI', 'Cloud', 'DevOps', 'TypeScript', 'SQL']

        for skill in skills_columns:
            input_data[skill] = 1 if any(skill.lower() in s.lower() for s in skills_list) else 0

        # Create feature array in correct order
        features = [input_data[col] for col in feature_columns]
        features_scaled = scaler.transform([features])

        # Make prediction (this returns USD salary)
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
        local_salary = predicted_salary_usd / exchange_rate

        # Calculate confidence (you can make this smarter)
        confidence = min(85 + (experience * 1) + (len(skills_list) * 2), 95)

        # Prepare factors
        factors = [
            f"{experience} years of experience",
            f"{job_title} role",
            f"{location} location",
            f"{education_level} education level",
            f"{len(skills_list)} key skills selected",
            "ML model trained on real salary data"
        ]

        return {
            "success": True,
            "salaryUSD": round(predicted_salary_usd),
            "salary": round(local_salary),
            "currency": currency,
            "confidence": round(confidence),
            "factors": factors,
            "model": "RandomForest_Colab_Trained"
        }

    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "message": "ML prediction failed"
        }

if __name__ == "__main__":
    # Read input from command line
    input_data = json.loads(sys.argv[1])

    experience = input_data['experience']
    job_title = input_data['role']
    location = input_data['location']
    education_level = input_data['education']
    skills_list = input_data['skills']

    # Make prediction
    result = predict_salary(experience, job_title, location, education_level, skills_list)

    # Output result as JSON
    print(json.dumps(result))