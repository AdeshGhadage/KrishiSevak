# config.py
import os
from dotenv import load_dotenv

load_dotenv()

# Weather API
WEATHER_API_KEY = os.getenv("WEATHER_API_KEY")
WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather"  # Example

# LangChain/LLM API
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
# You'll need to set up your LLM model here
# Example:
# from langchain_openai import ChatOpenAI
# llm = ChatOpenAI(openai_api_key=OPENAI_API_KEY)

# Vector DB
VECTOR_DB_PATH = "vector_db.faiss" # Path to your local FAISS vector store

# ViT Model
VIT_MODEL_URL = "http://your-vit-model-service.com/predict" # URL if hosted externally
# Or if running locally:
# import torch
# from transformers import ViTFeatureExtractor, ViTForImageClassification
# vit_model = ViTForImageClassification.from_pretrained('google/vit-base-patch16-224')
# feature_extractor = ViTFeatureExtractor.from_pretrained('google/vit-base-patch16-224')