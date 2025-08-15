"""
NLP and Voice Processing API endpoints
Handles multilingual text and voice interactions using Gemini/Ollama and voice processing
"""

from fastapi import APIRouter, HTTPException, UploadFile, File, Form, Request
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any, Union
from datetime import datetime
import io
import asyncio

from ..config import settings
from ..services.nlp_service import NLPService
from ..services.voice_service import VoiceService
from ..services.translation_service import TranslationService
from ..utils.logger import get_logger

logger = get_logger(__name__)
router = APIRouter()

class TextQuery(BaseModel):
    """Text query model"""
    text: str
    language: str = "en"
    context: Optional[str] = None  # farming, weather, prices, schemes
    user_location: Optional[str] = None

class VoiceQuery(BaseModel):
    """Voice query model"""
    language: str = "en"
    context: Optional[str] = None
    user_location: Optional[str] = None

class NLPResponse(BaseModel):
    """NLP response model"""
    query_id: str
    original_text: str
    original_language: str
    translated_text: Optional[str]
    intent: str  # disease_inquiry, weather_query, price_check, scheme_info, general
    entities: Dict[str, Any]  # extracted entities like crop names, locations, etc.
    confidence_score: float
    response_text: str
    response_language: str
    suggestions: List[str]
    related_topics: List[str]
    timestamp: datetime

class VoiceResponse(BaseModel):
    """Voice response model"""
    query_id: str
    transcribed_text: str
    original_language: str
    nlp_response: NLPResponse
    audio_response_url: Optional[str]  # URL to generated audio response
    processing_time: float

class TranslationRequest(BaseModel):
    """Translation request model"""
    text: str
    source_language: str
    target_language: str
    context: Optional[str] = None

class TranslationResponse(BaseModel):
    """Translation response model"""
    original_text: str
    translated_text: str
    source_language: str
    target_language: str
    confidence_score: float
    alternatives: List[str]

class ConversationContext(BaseModel):
    """Conversation context model"""
    session_id: str
    user_id: str
    conversation_history: List[Dict[str, Any]]
    user_preferences: Dict[str, Any]
    location_context: Optional[str]
    farming_context: Optional[Dict[str, Any]]

@router.post("/nlp/query", response_model=NLPResponse)
async def process_text_query(
    request: Request,
    query: TextQuery,
    session_id: Optional[str] = None
):
    """
    Process text query in multiple languages
    
    Features:
    - Intent recognition
    - Entity extraction
    - Context-aware responses
    - Multilingual support
    - Farming domain knowledge
    """
    
    try:
        nlp_service = getattr(request.app.state, 'nlp_service', None)
        
        if not nlp_service:
            logger.warning("NLP service not initialized, returning mock response")
            return await _get_mock_nlp_response(query)
        
        # Process the query
        response = await nlp_service.process_query(
            text=query.text,
            language=query.language,
            context=query.context,
            location=query.user_location,
            session_id=session_id
        )
        
        return response
        
    except Exception as e:
        logger.error(f"Text query processing failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to process text query: {str(e)}")

@router.post("/nlp/voice-query", response_model=VoiceResponse)
async def process_voice_query(
    request: Request,
    audio: UploadFile = File(..., description="Audio file for voice query"),
    language: str = Form("en", description="Expected language of the audio"),
    context: Optional[str] = Form(None, description="Context for better understanding"),
    user_location: Optional[str] = Form(None, description="User location for localized responses"),
    session_id: Optional[str] = Form(None, description="Session ID for conversation context")
):
    """
    Process voice query with speech-to-text and NLP
    
    Process:
    1. Convert audio to text (Whisper)
    2. Process text query with NLP
    3. Generate voice response (eSpeak-NG)
    4. Return both text and audio responses
    """
    
    try:
        # Validate audio file
        if not audio.content_type.startswith('audio/'):
            raise HTTPException(status_code=400, detail="Invalid file type. Please upload an audio file.")
        
        start_time = datetime.utcnow()
        
        voice_service = getattr(request.app.state, 'voice_service', None)
        nlp_service = getattr(request.app.state, 'nlp_service', None)
        
        if not voice_service or not nlp_service:
            logger.warning("Voice/NLP service not initialized, returning mock response")
            return await _get_mock_voice_response(language)
        
        # Read audio file
        audio_bytes = await audio.read()
        
        # Speech to text
        transcription_result = await voice_service.speech_to_text(
            audio_bytes, 
            language=language
        )
        
        transcribed_text = transcription_result['text']
        detected_language = transcription_result.get('language', language)
        
        # Process text query
        text_query = TextQuery(
            text=transcribed_text,
            language=detected_language,
            context=context,
            user_location=user_location
        )
        
        nlp_response = await nlp_service.process_query(
            text=transcribed_text,
            language=detected_language,
            context=context,
            location=user_location,
            session_id=session_id
        )
        
        # Generate audio response
        audio_response_url = None
        if nlp_response.response_text:
            audio_response = await voice_service.text_to_speech(
                text=nlp_response.response_text,
                language=nlp_response.response_language
            )
            # TODO: Save audio file and return URL
            audio_response_url = f"/api/v1/nlp/audio/{nlp_response.query_id}"
        
        processing_time = (datetime.utcnow() - start_time).total_seconds()
        
        return VoiceResponse(
            query_id=nlp_response.query_id,
            transcribed_text=transcribed_text,
            original_language=detected_language,
            nlp_response=nlp_response,
            audio_response_url=audio_response_url,
            processing_time=processing_time
        )
        
    except Exception as e:
        logger.error(f"Voice query processing failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to process voice query: {str(e)}")

@router.post("/nlp/translate", response_model=TranslationResponse)
async def translate_text(
    request: Request,
    translation_request: TranslationRequest
):
    """
    Translate text between supported languages
    
    Supports:
    - Agricultural terminology
    - Context-aware translation
    - Multiple Indian languages
    """
    
    try:
        translation_service = getattr(request.app.state, 'translation_service', None)
        
        if not translation_service:
            logger.warning("Translation service not initialized, returning mock response")
            return await _get_mock_translation(translation_request)
        
        result = await translation_service.translate(
            text=translation_request.text,
            source_language=translation_request.source_language,
            target_language=translation_request.target_language,
            context=translation_request.context
        )
        
        return result
        
    except Exception as e:
        logger.error(f"Translation failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to translate text: {str(e)}")

@router.get("/nlp/languages")
async def get_supported_languages():
    """Get list of supported languages for NLP and voice processing"""
    
    return {
        "supported_languages": [
            {"code": "en", "name": "English", "native_name": "English"},
            {"code": "hi", "name": "Hindi", "native_name": "हिंदी"},
            {"code": "bn", "name": "Bengali", "native_name": "বাংলা"},
            {"code": "te", "name": "Telugu", "native_name": "తెలుగు"},
            {"code": "mr", "name": "Marathi", "native_name": "मराठी"},
            {"code": "ta", "name": "Tamil", "native_name": "தமிழ்"},
            {"code": "gu", "name": "Gujarati", "native_name": "ગુજરાતી"},
            {"code": "kn", "name": "Kannada", "native_name": "ಕನ್ನಡ"},
            {"code": "ml", "name": "Malayalam", "native_name": "മലയാളം"},
            {"code": "pa", "name": "Punjabi", "native_name": "ਪੰਜਾਬੀ"},
            {"code": "or", "name": "Odia", "native_name": "ଓଡ଼ିଆ"},
            {"code": "as", "name": "Assamese", "native_name": "অসমীয়া"}
        ],
        "voice_supported": ["en", "hi", "bn", "te", "mr", "ta"],
        "text_supported": ["en", "hi", "bn", "te", "mr", "ta", "gu", "kn", "ml", "pa", "or", "as"]
    }

@router.post("/nlp/entities/extract")
async def extract_entities(
    request: Request,
    text: str,
    language: str = "en",
    context: Optional[str] = None
):
    """
    Extract agricultural entities from text
    
    Entities:
    - Crop names
    - Disease names
    - Locations
    - Dates/seasons
    - Agricultural terms
    """
    
    try:
        nlp_service = getattr(request.app.state, 'nlp_service', None)
        
        if not nlp_service:
            # Mock entity extraction
            return {
                "text": text,
                "language": language,
                "entities": {
                    "crops": ["wheat", "rice"],
                    "diseases": ["rust", "blight"],
                    "locations": ["punjab", "delhi"],
                    "dates": ["march", "april"]
                },
                "confidence_scores": {
                    "crops": 0.9,
                    "diseases": 0.8,
                    "locations": 0.95,
                    "dates": 0.7
                }
            }
        
        entities = await nlp_service.extract_entities(
            text=text,
            language=language,
            context=context
        )
        
        return entities
        
    except Exception as e:
        logger.error(f"Entity extraction failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to extract entities: {str(e)}")

@router.get("/nlp/conversation/{session_id}")
async def get_conversation_history(
    request: Request,
    session_id: str,
    limit: int = 20
):
    """Get conversation history for a session"""
    
    try:
        # TODO: Implement actual conversation history retrieval
        return {
            "session_id": session_id,
            "conversation_history": [],
            "total_interactions": 0,
            "session_started": datetime.utcnow(),
            "last_interaction": datetime.utcnow()
        }
        
    except Exception as e:
        logger.error(f"Conversation history fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch conversation history: {str(e)}")

@router.post("/nlp/feedback")
async def submit_nlp_feedback(
    request: Request,
    query_id: str,
    rating: int = Field(..., ge=1, le=5, description="Rating from 1-5"),
    feedback_text: Optional[str] = None,
    issue_type: Optional[str] = None  # accuracy, language, response_time
):
    """Submit feedback for NLP response quality"""
    
    try:
        # TODO: Implement feedback storage and model improvement
        return {
            "message": "Feedback submitted successfully",
            "query_id": query_id,
            "rating": rating,
            "status": "recorded"
        }
        
    except Exception as e:
        logger.error(f"Feedback submission failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to submit feedback: {str(e)}")

# Mock functions for development
async def _get_mock_nlp_response(query: TextQuery) -> NLPResponse:
    """Generate mock NLP response"""
    
    query_id = f"nlp_{datetime.utcnow().timestamp()}"
    
    # Simple intent detection based on keywords
    intent = "general"
    if any(word in query.text.lower() for word in ["disease", "pest", "infection", "spots"]):
        intent = "disease_inquiry"
    elif any(word in query.text.lower() for word in ["weather", "rain", "temperature", "irrigation"]):
        intent = "weather_query"
    elif any(word in query.text.lower() for word in ["price", "market", "sell", "buy"]):
        intent = "price_check"
    elif any(word in query.text.lower() for word in ["scheme", "subsidy", "loan", "government"]):
        intent = "scheme_info"
    
    # Mock response based on intent
    response_text = "I understand you're asking about farming. Let me help you with that."
    if intent == "disease_inquiry":
        response_text = "For plant disease identification, please upload an image of the affected plant. I can help identify diseases and suggest treatments."
    elif intent == "weather_query":
        response_text = "I can provide weather forecasts and irrigation recommendations. Please share your location for accurate information."
    elif intent == "price_check":
        response_text = "I can help you check current market prices for crops and fertilizers. What specific prices are you looking for?"
    elif intent == "scheme_info":
        response_text = "I can provide information about government schemes and check your eligibility. What type of scheme are you interested in?"
    
    # Translate response if needed
    if query.language == "hi":
        response_text = "मैं समझता हूं कि आप खेती के बारे में पूछ रहे हैं। मैं आपकी मदद कर सकता हूं।"
    
    return NLPResponse(
        query_id=query_id,
        original_text=query.text,
        original_language=query.language,
        translated_text=None,
        intent=intent,
        entities={
            "crops": [],
            "locations": [],
            "diseases": []
        },
        confidence_score=0.85,
        response_text=response_text,
        response_language=query.language,
        suggestions=[
            "Upload plant images for disease detection",
            "Check weather forecasts",
            "Explore government schemes"
        ],
        related_topics=["disease_detection", "weather", "schemes", "prices"],
        timestamp=datetime.utcnow()
    )

async def _get_mock_voice_response(language: str) -> VoiceResponse:
    """Generate mock voice response"""
    
    query_id = f"voice_{datetime.utcnow().timestamp()}"
    transcribed_text = "What crops should I plant this season?"
    
    nlp_response = NLPResponse(
        query_id=query_id,
        original_text=transcribed_text,
        original_language=language,
        translated_text=None,
        intent="crop_recommendation",
        entities={"crops": [], "seasons": ["current"]},
        confidence_score=0.9,
        response_text="Based on your location and current season, I recommend planting wheat or mustard. These crops are suitable for winter season and have good market demand.",
        response_language=language,
        suggestions=["Check soil conditions", "Review weather forecast"],
        related_topics=["crop_selection", "weather", "market_prices"],
        timestamp=datetime.utcnow()
    )
    
    return VoiceResponse(
        query_id=query_id,
        transcribed_text=transcribed_text,
        original_language=language,
        nlp_response=nlp_response,
        audio_response_url=f"/api/v1/nlp/audio/{query_id}",
        processing_time=2.5
    )

async def _get_mock_translation(request: TranslationRequest) -> TranslationResponse:
    """Generate mock translation response"""
    
    # Simple mock translations
    translations = {
        ("en", "hi"): {
            "wheat": "गेहूं",
            "rice": "चावल",
            "disease": "रोग",
            "weather": "मौसम"
        },
        ("hi", "en"): {
            "गेहूं": "wheat",
            "चावल": "rice",
            "रोग": "disease",
            "मौसम": "weather"
        }
    }
    
    key = (request.source_language, request.target_language)
    translated_text = request.text
    
    if key in translations:
        for original, translation in translations[key].items():
            translated_text = translated_text.replace(original, translation)
    
    return TranslationResponse(
        original_text=request.text,
        translated_text=translated_text,
        source_language=request.source_language,
        target_language=request.target_language,
        confidence_score=0.9,
        alternatives=[translated_text]
    )
