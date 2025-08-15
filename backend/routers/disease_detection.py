"""
Plant Disease Detection API endpoints
Uses ViT (Vision Transformer) for image-based disease identification
"""

from fastapi import APIRouter, UploadFile, File, HTTPException, Form, Request
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import io
from PIL import Image
import numpy as np
from datetime import datetime
import uuid
import os

from ..config import settings
from ..services.vision_service import VisionService
from ..services.treatment_service import TreatmentService
from ..utils.image_utils import validate_image, preprocess_image
from ..utils.logger import get_logger

logger = get_logger(__name__)
router = APIRouter()

# Initialize services (will be properly initialized in lifespan)
vision_service = None
treatment_service = None

class DiseaseDetectionRequest(BaseModel):
    """Disease detection request model"""
    crop_type: Optional[str] = None
    location: Optional[str] = None
    farmer_language: str = "en"

class DiseaseDetectionResponse(BaseModel):
    """Disease detection response model"""
    detection_id: str
    timestamp: datetime
    crop_type: Optional[str]
    disease_detected: bool
    disease_name: Optional[str]
    disease_name_local: Optional[str]
    confidence_score: float
    severity_level: str  # mild, moderate, severe
    treatment_recommendations: List[Dict[str, Any]]
    prevention_tips: List[str]
    organic_treatments: List[Dict[str, Any]]
    chemical_treatments: List[Dict[str, Any]]
    estimated_yield_impact: Optional[str]
    follow_up_recommendations: List[str]

class TreatmentRecommendation(BaseModel):
    """Treatment recommendation model"""
    treatment_type: str  # organic, chemical, biological
    product_name: str
    dosage: str
    application_method: str
    frequency: str
    cost_estimate: Optional[float]
    availability: str  # local, regional, online
    effectiveness_rating: float

@router.post("/disease-detection", response_model=DiseaseDetectionResponse)
async def detect_disease(
    request: Request,
    image: UploadFile = File(..., description="Plant image for disease detection"),
    crop_type: Optional[str] = Form(None, description="Type of crop (optional)"),
    location: Optional[str] = Form(None, description="Farm location for localized advice"),
    farmer_language: str = Form("en", description="Farmer's preferred language")
):
    """
    Detect plant diseases from uploaded image
    
    Process:
    1. Validate and preprocess image
    2. Run ViT model for disease classification
    3. Generate treatment recommendations
    4. Provide localized advice based on location
    """
    
    try:
        # Validate image
        if not image.content_type.startswith('image/'):
            raise HTTPException(status_code=400, detail="Invalid file type. Please upload an image.")
        
        if image.size > settings.MAX_FILE_SIZE:
            raise HTTPException(status_code=400, detail="Image size too large. Maximum 10MB allowed.")
        
        # Read and validate image
        image_bytes = await image.read()
        pil_image = validate_image(image_bytes)
        
        # Preprocess image for model
        processed_image = preprocess_image(pil_image)
        
        # Generate unique detection ID
        detection_id = str(uuid.uuid4())
        
        # Get vision service from app state
        vision_service = request.app.state.vision_service if hasattr(request.app.state, 'vision_service') else None
        treatment_service = request.app.state.treatment_service if hasattr(request.app.state, 'treatment_service') else None
        
        if not vision_service:
            # Fallback for development - return mock response
            logger.warning("Vision service not initialized, returning mock response")
            return await _get_mock_disease_response(detection_id, crop_type, farmer_language)
        
        # Run disease detection
        detection_result = await vision_service.detect_disease(
            processed_image, 
            crop_type=crop_type
        )
        
        # Extract results
        disease_detected = detection_result['confidence'] > settings.DISEASE_CONFIDENCE_THRESHOLD
        disease_name = detection_result['disease_name'] if disease_detected else None
        confidence_score = detection_result['confidence']
        severity_level = detection_result.get('severity', 'mild')
        
        # Get treatment recommendations if disease detected
        treatment_recommendations = []
        organic_treatments = []
        chemical_treatments = []
        prevention_tips = []
        follow_up_recommendations = []
        
        if disease_detected and treatment_service:
            treatments = await treatment_service.get_recommendations(
                disease_name=disease_name,
                crop_type=crop_type,
                severity=severity_level,
                location=location,
                language=farmer_language
            )
            
            treatment_recommendations = treatments.get('general', [])
            organic_treatments = treatments.get('organic', [])
            chemical_treatments = treatments.get('chemical', [])
            prevention_tips = treatments.get('prevention', [])
            follow_up_recommendations = treatments.get('follow_up', [])
        
        # Get localized disease name
        disease_name_local = disease_name  # TODO: Implement translation service
        
        # Estimate yield impact
        estimated_yield_impact = None
        if disease_detected:
            if severity_level == "severe":
                estimated_yield_impact = "20-40% yield loss if untreated"
            elif severity_level == "moderate":
                estimated_yield_impact = "10-20% yield loss if untreated"
            else:
                estimated_yield_impact = "5-10% yield loss if untreated"
        
        # Save detection record (TODO: Implement database storage)
        logger.info(f"Disease detection completed: {detection_id}, Disease: {disease_name}, Confidence: {confidence_score}")
        
        return DiseaseDetectionResponse(
            detection_id=detection_id,
            timestamp=datetime.utcnow(),
            crop_type=crop_type,
            disease_detected=disease_detected,
            disease_name=disease_name,
            disease_name_local=disease_name_local,
            confidence_score=confidence_score,
            severity_level=severity_level,
            treatment_recommendations=treatment_recommendations,
            prevention_tips=prevention_tips,
            organic_treatments=organic_treatments,
            chemical_treatments=chemical_treatments,
            estimated_yield_impact=estimated_yield_impact,
            follow_up_recommendations=follow_up_recommendations
        )
        
    except Exception as e:
        logger.error(f"Disease detection failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Disease detection failed: {str(e)}")

@router.get("/disease-detection/{detection_id}")
async def get_detection_result(detection_id: str):
    """Get previous detection result by ID"""
    # TODO: Implement database lookup
    raise HTTPException(status_code=404, detail="Detection result not found")

@router.get("/diseases/supported")
async def get_supported_diseases():
    """Get list of supported diseases and crops"""
    # TODO: Return actual supported diseases from model
    return {
        "supported_crops": [
            "rice", "wheat", "corn", "tomato", "potato", "cotton", 
            "sugarcane", "soybean", "groundnut", "sunflower"
        ],
        "supported_diseases": [
            "bacterial_blight", "brown_spot", "leaf_smut", "tungro",
            "blast", "sheath_blight", "stem_rot", "yellow_dwarf"
        ]
    }

@router.post("/disease-detection/batch")
async def batch_disease_detection(
    request: Request,
    images: List[UploadFile] = File(..., description="Multiple plant images"),
    crop_type: Optional[str] = Form(None),
    location: Optional[str] = Form(None),
    farmer_language: str = Form("en")
):
    """Batch process multiple images for disease detection"""
    
    if len(images) > 10:
        raise HTTPException(status_code=400, detail="Maximum 10 images allowed per batch")
    
    results = []
    for image in images:
        try:
            # Process each image individually
            result = await detect_disease(
                request=request,
                image=image,
                crop_type=crop_type,
                location=location,
                farmer_language=farmer_language
            )
            results.append(result)
        except Exception as e:
            logger.error(f"Failed to process image {image.filename}: {str(e)}")
            results.append({
                "filename": image.filename,
                "error": str(e),
                "status": "failed"
            })
    
    return {"batch_results": results, "total_processed": len(results)}

async def _get_mock_disease_response(detection_id: str, crop_type: Optional[str], language: str) -> DiseaseDetectionResponse:
    """Generate mock response for development/testing"""
    
    mock_treatments = [
        {
            "treatment_type": "organic",
            "product_name": "Neem Oil Spray",
            "dosage": "2ml per liter of water",
            "application_method": "Foliar spray",
            "frequency": "Twice weekly",
            "cost_estimate": 50.0,
            "availability": "local",
            "effectiveness_rating": 0.8
        }
    ]
    
    return DiseaseDetectionResponse(
        detection_id=detection_id,
        timestamp=datetime.utcnow(),
        crop_type=crop_type,
        disease_detected=True,
        disease_name="bacterial_blight",
        disease_name_local="बैक्टीरियल ब्लाइट" if language == "hi" else "bacterial_blight",
        confidence_score=0.85,
        severity_level="moderate",
        treatment_recommendations=mock_treatments,
        prevention_tips=[
            "Ensure proper field drainage",
            "Use disease-resistant varieties",
            "Maintain field hygiene"
        ],
        organic_treatments=mock_treatments,
        chemical_treatments=[],
        estimated_yield_impact="10-20% yield loss if untreated",
        follow_up_recommendations=[
            "Monitor plants weekly",
            "Reapply treatment if symptoms persist"
        ]
    )
