import tensorflow as tf
import numpy as np
from PIL import Image
import json
import sys
import os

# Load your trained model
model = tf.keras.models.load_model('car_model.h5')
car_brands = ['BMW', 'Mercedes', 'Audi']

def predict_car(image_path):
    # Load and prepare image
    img = Image.open(image_path)
    img = img.resize((150, 150))
    img_array = np.array(img) / 255.0
    img_array = np.expand_dims(img_array, axis=0)

    # Make prediction
    prediction = model.predict(img_array)[0]
    top_index = np.argmax(prediction)

    return {
        "success": True,
        "predicted_brand": car_brands[top_index],
        "confidence": float(prediction[top_index]) * 100,
        "model": "Car_Recognizer_v1.0"
    }

# Main execution
if __name__ == "__main__":
    input_data = json.loads(sys.argv[1])
    image_path = input_data['image_path']

    result = predict_car(image_path)
    print(json.dumps(result))