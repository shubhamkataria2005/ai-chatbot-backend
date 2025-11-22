import sys
import json
import joblib
import re
import nltk
from nltk.corpus import stopwords
from nltk.stem.porter import PorterStemmer
import traceback
import os

def predict_sentiment(text):
    """
    Predict sentiment using your trained model
    """
    try:
        print(f"Sentiment Analysis for: {text}")

        # Get the directory where this script is located
        script_dir = os.path.dirname(os.path.abspath(__file__))
        model_path = os.path.join(script_dir, 'sentiment_model.pkl')

        print(f"Loading model from: {model_path}")

        # Check if model file exists
        if not os.path.exists(model_path):
            error_msg = f"Model file not found: {model_path}"
            print(f"ERROR: {error_msg}")
            return {"success": False, "error": error_msg}

        # Load model components
        components = joblib.load(model_path)
        classifier = components['classifier']
        cv = components['count_vectorizer']
        le = components['label_encoder']
        ps = components['stemmer']

        print("Model loaded successfully")

        # Preprocess text (same as your training)
        review = re.sub('[^a-zA-Z]', ' ', text)
        review = review.lower()
        review = review.split()
        all_stopwords = stopwords.words('english')
        all_stopwords.remove('not')
        review = [ps.stem(word) for word in review if not word in set(all_stopwords)]
        review = ' '.join(review)

        print(f"Processed text: {review}")

        # Transform and predict
        X_new = cv.transform([review]).toarray()
        prediction_encoded = classifier.predict(X_new)[0]
        sentiment = le.inverse_transform([prediction_encoded])[0]

        # Get confidence score
        probabilities = classifier.predict_proba(X_new)[0]
        confidence = max(probabilities) * 100

        print(f"Prediction: {sentiment} (Confidence: {confidence:.2f}%)")

        # Prepare result
        result = {
            "success": True,
            "sentiment": sentiment,
            "confidence": round(confidence, 2),
            "analysis": f"The text shows {sentiment.lower()} sentiment with {confidence:.1f}% confidence",
            "textLength": len(text),
            "wordCount": len(text.split()),
            "model": "NaiveBayes_Sentiment_v1.0"
        }

        return result

    except Exception as e:
        error_msg = f"Prediction failed: {str(e)}"
        print(f"ERROR: {error_msg}")
        traceback.print_exc()
        return {"success": False, "error": error_msg}

if __name__ == "__main__":
    try:
        print("Python sentiment script started")

        # Read input from file (passed as command line argument)
        if len(sys.argv) > 1:
            input_file = sys.argv[1]
            print(f"Reading input from file: {input_file}")

            with open(input_file, 'r') as f:
                input_data = json.load(f)
        else:
            print("No input provided, using test data")
            input_data = {"text": "I love this product!"}

        text = input_data['text']

        # Make prediction
        result = predict_sentiment(text)

        # Output result as JSON - THIS MUST BE THE LAST LINE!
        print(json.dumps(result))

    except Exception as e:
        error_msg = f"Script execution failed: {str(e)}"
        print(f"ERROR: {error_msg}")
        traceback.print_exc()

        error_result = {
            "success": False,
            "error": error_msg
        }
        print(json.dumps(error_result))