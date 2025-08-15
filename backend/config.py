"""
Configuration settings for KrishiSevak Backend
"""

import os
from typing import List
from pydantic_settings import BaseSettings
from pydantic import Field


class Settings(BaseSettings):
    """Application settings"""
    
    # Server Configuration
    HOST: str = Field(default="0.0.0.0", description="Server host")
    PORT: int = Field(default=8000, description="Server port")
    DEBUG: bool = Field(default=True, description="Debug mode")
    ENVIRONMENT: str = Field(default="development", description="Environment")
    
    # Security
    SECRET_KEY: str = Field(default="your-secret-key-here", description="Secret key for JWT")
    ALLOWED_ORIGINS: List[str] = Field(
        default=["http://localhost:3000", "http://127.0.0.1:3000"],
        description="Allowed CORS origins"
    )
    
    # Database Configuration
    DATABASE_URL: str = Field(
        default="sqlite:///./krishisevak.db",
        description="Database URL"
    )
    
    # AI/ML Model Configuration
    VISION_MODEL_PATH: str = Field(
        default="./models/vit_disease_detection",
        description="Path to ViT disease detection model"
    )
    
    # NLP Configuration
    GEMINI_API_KEY: str = Field(default="", description="Gemini API key")
    OLLAMA_BASE_URL: str = Field(
        default="http://localhost:11434",
        description="Ollama base URL"
    )
    OLLAMA_MODEL: str = Field(
        default="llama2",
        description="Ollama model name"
    )
    
    # Voice Processing
    WHISPER_MODEL_PATH: str = Field(
        default="./models/whisper",
        description="Path to Whisper model"
    )
    ESPEAK_PATH: str = Field(
        default="/usr/bin/espeak",
        description="Path to eSpeak binary"
    )
    
    # RAG Configuration
    VECTOR_DB_TYPE: str = Field(
        default="faiss",
        description="Vector database type (faiss|pinecone)"
    )
    PINECONE_API_KEY: str = Field(default="", description="Pinecone API key")
    PINECONE_ENVIRONMENT: str = Field(default="", description="Pinecone environment")
    FAISS_INDEX_PATH: str = Field(
        default="./data/faiss_index",
        description="FAISS index file path"
    )
    
    # External APIs
    OPENWEATHER_API_KEY: str = Field(default="", description="OpenWeatherMap API key")
    GOVERNMENT_DATA_BASE_URL: str = Field(
        default="https://api.data.gov.in",
        description="Government data API base URL"
    )
    
    # File Storage
    UPLOAD_DIR: str = Field(
        default="./uploads",
        description="Upload directory for images"
    )
    MAX_FILE_SIZE: int = Field(
        default=10 * 1024 * 1024,  # 10MB
        description="Maximum file size in bytes"
    )
    
    # Supported Languages
    SUPPORTED_LANGUAGES: List[str] = Field(
        default=[
            "en", "hi", "bn", "te", "mr", "ta", "gu", "kn", "ml", "pa", "or", "as"
        ],
        description="Supported language codes"
    )
    
    # Cache Configuration
    REDIS_URL: str = Field(
        default="redis://localhost:6379",
        description="Redis URL for caching"
    )
    CACHE_TTL: int = Field(
        default=3600,  # 1 hour
        description="Cache TTL in seconds"
    )
    
    # Logging
    LOG_LEVEL: str = Field(default="INFO", description="Logging level")
    LOG_FILE: str = Field(
        default="./logs/krishisevak.log",
        description="Log file path"
    )
    
    # Rate Limiting
    RATE_LIMIT_REQUESTS: int = Field(
        default=100,
        description="Rate limit requests per minute"
    )
    
    # Disease Detection Configuration
    DISEASE_CONFIDENCE_THRESHOLD: float = Field(
        default=0.7,
        description="Minimum confidence threshold for disease detection"
    )
    
    # Weather Configuration
    WEATHER_CACHE_DURATION: int = Field(
        default=1800,  # 30 minutes
        description="Weather data cache duration in seconds"
    )
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = True

# Create settings instance
settings = Settings()

# Create necessary directories
os.makedirs(settings.UPLOAD_DIR, exist_ok=True)
os.makedirs(os.path.dirname(settings.LOG_FILE), exist_ok=True)
os.makedirs(os.path.dirname(settings.FAISS_INDEX_PATH), exist_ok=True)
os.makedirs(os.path.dirname(settings.VISION_MODEL_PATH), exist_ok=True)
