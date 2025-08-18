from __future__ import annotations
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_ollama import ChatOllama
from langchain_core.language_models.chat_models import BaseChatModel
from app.config import settings

def get_llm() -> BaseChatModel:
    provider = settings.USE_LLM.lower()
    if provider == "gemini":
        if not settings.GEMINI_API_KEY:
            raise RuntimeError("GEMINI_API_KEY not set")
        return ChatGoogleGenerativeAI(
            model=settings.GEMINI_MODEL,
            google_api_key=settings.GEMINI_API_KEY,
            temperature=0.2,
        )
    elif provider == "ollama":
        return ChatOllama(
            base_url=settings.OLLAMA_BASE_URL,
            model=settings.OLLAMA_MODEL,
            temperature=0.2,
            stream=True,
        )
    else:
        raise ValueError(f"Unknown USE_LLM={provider}")
