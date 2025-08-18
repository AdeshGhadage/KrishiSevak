from pydantic_settings import BaseSettings, SettingsConfigDict
import os

class Settings(BaseSettings):
    # LLM
    USE_LLM: str = os.getenv("USE_LLM", "gemini")
    GEMINI_API_KEY: str | None = os.getenv("GEMINI_API_KEY")
    GEMINI_MODEL: str = os.getenv("GEMINI_MODEL", "gemini-1.5-flash")
    OLLAMA_BASE_URL: str = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
    OLLAMA_MODEL: str = os.getenv("OLLAMA_MODEL", "llama3.1")

    # Pinecone
    PINECONE_API_KEY: str | None = os.getenv("PINECONE_API_KEY")
    PINECONE_INDEX: str = os.getenv("PINECONE_INDEX", "android_agent_rag")
    PINECONE_NAMESPACE: str = os.getenv("PINECONE_NAMESPACE", "default")
    PINECONE_CLOUD: str = os.getenv("PINECONE_CLOUD", "aws")  # aws|gcp|azure
    PINECONE_REGION: str = os.getenv("PINECONE_REGION", "us-east-1")

    # Embeddings
    EMBEDDING_MODEL: str = os.getenv("EMBEDDING_MODEL", "intfloat/multilingual-e5-base")

    # Weather
    WEATHER_PROVIDER: str = os.getenv("WEATHER_PROVIDER", "open-meteo")

    # Speech
    WHISPER_CPP_BIN: str = os.getenv("WHISPER_CPP_BIN", "/opt/whisper.cpp/build/bin/whisper-cli")
    WHISPER_MODEL_PATH: str = os.getenv("WHISPER_MODEL_PATH", "/opt/whisper.cpp/models/ggml-base.en.bin")
    FFMPEG_BIN: str = os.getenv("FFMPEG_BIN", "ffmpeg")
    ESPEAK_BIN: str = os.getenv("ESPEAK_BIN", "espeak-ng")
    TTS_VOICE: str = os.getenv("TTS_VOICE", "en-us")
    TTS_SPEED_WPM: int = int(os.getenv("TTS_SPEED_WPM", "170"))

    # Online STT/TTS providers
    STT_PROVIDER: str = os.getenv("STT_PROVIDER", "local")  # local|openai
    OPENAI_API_KEY: str | None = os.getenv("OPENAI_API_KEY")
    OPENAI_BASE_URL: str = os.getenv("OPENAI_BASE_URL", "https://api.openai.com")
    OPENAI_WHISPER_MODEL: str = os.getenv("OPENAI_WHISPER_MODEL", "whisper-1")

    TTS_PROVIDER: str = os.getenv("TTS_PROVIDER", "espeak")  # espeak|elevenlabs
    ELEVENLABS_API_KEY: str | None = os.getenv("ELEVENLABS_API_KEY")
    ELEVENLABS_VOICE_ID: str = os.getenv("ELEVENLABS_VOICE_ID", "21m00Tcm4TlvDq8ikWAM")  # default example
    ELEVENLABS_MODEL_ID: str = os.getenv("ELEVENLABS_MODEL_ID", "eleven_multilingual_v2")

    # ViT
    VIT_MODEL_DIR: str = os.getenv("VIT_MODEL_DIR", "./models/vit-crop-disease")
    VIT_LABELS_JSON: str = os.getenv("VIT_LABELS_JSON", "./models/vit-crop-disease/labels.json")

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

settings = Settings()
