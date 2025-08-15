"""
Government Schemes and Subsidies API endpoints
Provides information about government agricultural schemes and automated eligibility checking
"""

from fastapi import APIRouter, HTTPException, Query, Request
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime, date
from enum import Enum

from ..config import settings
from ..services.government_service import GovernmentService
from ..utils.logger import get_logger

logger = get_logger(__name__)
router = APIRouter()

class SchemeCategory(str, Enum):
    """Government scheme categories"""
    SUBSIDY = "subsidy"
    LOAN = "loan"
    INSURANCE = "insurance"
    TRAINING = "training"
    EQUIPMENT = "equipment"
    SEED = "seed"
    FERTILIZER = "fertilizer"
    IRRIGATION = "irrigation"
    CROP_SUPPORT = "crop_support"

class EligibilityStatus(str, Enum):
    """Eligibility status enumeration"""
    ELIGIBLE = "eligible"
    NOT_ELIGIBLE = "not_eligible"
    PARTIALLY_ELIGIBLE = "partially_eligible"
    PENDING_VERIFICATION = "pending_verification"

class FarmerProfile(BaseModel):
    """Farmer profile for eligibility checking"""
    farmer_id: str
    name: str
    age: int
    gender: str
    category: str  # general, obc, sc, st
    land_holding: float  # acres
    annual_income: float
    location: str
    state: str
    district: str
    village: str
    education_level: str
    farming_experience: int  # years
    primary_crops: List[str]
    has_kisan_card: bool = False
    has_aadhaar: bool = True
    has_bank_account: bool = True
    is_marginal_farmer: Optional[bool] = None
    is_small_farmer: Optional[bool] = None

class GovernmentScheme(BaseModel):
    """Government scheme model"""
    scheme_id: str
    scheme_name: str
    scheme_name_local: Optional[str]
    category: SchemeCategory
    description: str
    description_local: Optional[str]
    ministry: str
    state: Optional[str]  # None for central schemes
    benefit_amount: Optional[float]
    benefit_type: str  # cash, subsidy, loan, equipment
    eligibility_criteria: List[str]
    required_documents: List[str]
    application_process: str
    application_deadline: Optional[date]
    scheme_duration: Optional[str]
    official_website: Optional[str]
    contact_information: Dict[str, str]
    last_updated: datetime
    is_active: bool

class EligibilityCheck(BaseModel):
    """Eligibility check result"""
    scheme_id: str
    scheme_name: str
    eligibility_status: EligibilityStatus
    eligibility_score: float  # 0-1
    eligible_benefits: List[str]
    missing_criteria: List[str]
    required_documents: List[str]
    estimated_benefit: Optional[float]
    application_steps: List[str]
    deadline: Optional[date]
    recommendations: List[str]

class SchemeApplication(BaseModel):
    """Scheme application model"""
    application_id: str
    farmer_id: str
    scheme_id: str
    application_date: datetime
    status: str  # submitted, under_review, approved, rejected
    submitted_documents: List[str]
    application_data: Dict[str, Any]
    review_comments: Optional[str]
    approval_date: Optional[datetime]
    disbursement_date: Optional[datetime]
    disbursed_amount: Optional[float]

@router.get("/schemes", response_model=List[GovernmentScheme])
async def get_government_schemes(
    request: Request,
    category: Optional[SchemeCategory] = Query(None, description="Filter by scheme category"),
    state: Optional[str] = Query(None, description="Filter by state"),
    location: Optional[str] = Query(None, description="Farmer's location for relevant schemes"),
    active_only: bool = Query(True, description="Show only active schemes"),
    limit: int = Query(50, description="Maximum number of results")
):
    """
    Get list of government schemes based on filters
    
    Provides:
    - Central and state government schemes
    - Category-wise filtering
    - Location-based relevant schemes
    - Multilingual scheme information
    """
    
    try:
        government_service = getattr(request.app.state, 'government_service', None)
        
        if not government_service:
            logger.warning("Government service not initialized, returning mock data")
            return await _get_mock_government_schemes(category, state, limit)
        
        schemes = await government_service.get_schemes(
            category=category,
            state=state,
            location=location,
            active_only=active_only,
            limit=limit
        )
        
        return schemes
        
    except Exception as e:
        logger.error(f"Government schemes fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch government schemes: {str(e)}")

@router.post("/schemes/eligibility", response_model=List[EligibilityCheck])
async def check_scheme_eligibility(
    request: Request,
    farmer_profile: FarmerProfile,
    scheme_ids: Optional[List[str]] = None
):
    """
    Check farmer's eligibility for government schemes
    
    Process:
    1. Analyze farmer profile against scheme criteria
    2. Calculate eligibility scores
    3. Identify missing requirements
    4. Provide application guidance
    """
    
    try:
        government_service = getattr(request.app.state, 'government_service', None)
        
        if not government_service:
            logger.warning("Government service not initialized, returning mock data")
            return await _get_mock_eligibility_checks(farmer_profile, scheme_ids)
        
        # Determine farmer categories
        if farmer_profile.land_holding <= 2.5:
            farmer_profile.is_marginal_farmer = True
            farmer_profile.is_small_farmer = False
        elif farmer_profile.land_holding <= 5.0:
            farmer_profile.is_marginal_farmer = False
            farmer_profile.is_small_farmer = True
        else:
            farmer_profile.is_marginal_farmer = False
            farmer_profile.is_small_farmer = False
        
        eligibility_results = await government_service.check_eligibility(
            farmer_profile=farmer_profile,
            scheme_ids=scheme_ids
        )
        
        return eligibility_results
        
    except Exception as e:
        logger.error(f"Eligibility check failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to check eligibility: {str(e)}")

@router.get("/schemes/{scheme_id}", response_model=GovernmentScheme)
async def get_scheme_details(
    request: Request,
    scheme_id: str,
    language: str = Query("en", description="Language for scheme details")
):
    """Get detailed information about a specific scheme"""
    
    try:
        government_service = getattr(request.app.state, 'government_service', None)
        
        if not government_service:
            return await _get_mock_scheme_details(scheme_id, language)
        
        scheme = await government_service.get_scheme_details(
            scheme_id=scheme_id,
            language=language
        )
        
        if not scheme:
            raise HTTPException(status_code=404, detail="Scheme not found")
        
        return scheme
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Scheme details fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch scheme details: {str(e)}")

@router.post("/schemes/{scheme_id}/apply")
async def apply_for_scheme(
    request: Request,
    scheme_id: str,
    farmer_profile: FarmerProfile,
    application_data: Dict[str, Any]
):
    """Submit application for a government scheme"""
    
    try:
        government_service = getattr(request.app.state, 'government_service', None)
        
        if not government_service:
            # Mock application submission
            application_id = f"APP_{datetime.utcnow().timestamp()}"
            return {
                "application_id": application_id,
                "status": "submitted",
                "message": "Application submitted successfully",
                "next_steps": [
                    "Wait for document verification",
                    "Check application status regularly",
                    "Ensure all documents are valid"
                ]
            }
        
        application_result = await government_service.submit_application(
            scheme_id=scheme_id,
            farmer_profile=farmer_profile,
            application_data=application_data
        )
        
        return application_result
        
    except Exception as e:
        logger.error(f"Scheme application failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to submit application: {str(e)}")

@router.get("/schemes/applications/{farmer_id}")
async def get_farmer_applications(
    request: Request,
    farmer_id: str,
    status: Optional[str] = Query(None, description="Filter by application status")
):
    """Get farmer's scheme applications and their status"""
    
    try:
        government_service = getattr(request.app.state, 'government_service', None)
        
        if not government_service:
            return await _get_mock_farmer_applications(farmer_id, status)
        
        applications = await government_service.get_farmer_applications(
            farmer_id=farmer_id,
            status=status
        )
        
        return {"applications": applications, "total_applications": len(applications)}
        
    except Exception as e:
        logger.error(f"Farmer applications fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch applications: {str(e)}")

@router.get("/schemes/subsidies/fertilizer")
async def get_fertilizer_subsidies(
    request: Request,
    location: str = Query(..., description="Location for subsidy information"),
    crop_type: Optional[str] = Query(None, description="Crop type for specific subsidies")
):
    """Get fertilizer subsidy information for location"""
    
    try:
        # Mock fertilizer subsidy data
        subsidies = [
            {
                "subsidy_scheme": "Nutrient Based Subsidy (NBS)",
                "fertilizer_type": "DAP",
                "subsidy_rate": 24.0,  # per kg
                "subsidy_percentage": 75.0,
                "market_price": 32.0,
                "subsidized_price": 24.0,
                "eligibility": "All farmers",
                "application_required": False,
                "available_at": "All registered dealers",
                "last_updated": datetime.utcnow()
            },
            {
                "subsidy_scheme": "Pradhan Mantri Kisan Samman Nidhi",
                "fertilizer_type": "Urea",
                "subsidy_rate": 5.80,
                "subsidy_percentage": 52.0,
                "market_price": 12.0,
                "subsidized_price": 5.80,
                "eligibility": "Small and marginal farmers",
                "application_required": True,
                "available_at": "Cooperative societies",
                "last_updated": datetime.utcnow()
            }
        ]
        
        return {
            "location": location,
            "subsidies": subsidies,
            "total_subsidies": len(subsidies),
            "last_updated": datetime.utcnow()
        }
        
    except Exception as e:
        logger.error(f"Fertilizer subsidies fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch fertilizer subsidies: {str(e)}")

@router.get("/schemes/loans/kisan-credit")
async def get_kisan_credit_card_info(
    request: Request,
    location: str = Query(..., description="Location for KCC information"),
    crop_type: Optional[str] = Query(None, description="Crop type for loan calculation")
):
    """Get Kisan Credit Card (KCC) loan information"""
    
    try:
        # Mock KCC information
        kcc_info = {
            "scheme_name": "Kisan Credit Card",
            "description": "Credit facility for farmers to meet production credit needs",
            "loan_amount": {
                "calculation_basis": "Scale of finance per crop + 10% for post-harvest expenses + 20% for maintenance",
                "maximum_limit": 300000,  # for small farmers
                "interest_rate": 7.0,  # per annum
                "subsidized_rate": 4.0,  # with interest subvention
            },
            "eligibility": [
                "Farmers (individual/joint) - Owner cultivators",
                "Tenant farmers, oral lessees & share croppers",
                "Farmers engaged in allied activities"
            ],
            "required_documents": [
                "Aadhaar Card",
                "Land ownership documents",
                "Income certificate",
                "Recent passport size photograph"
            ],
            "application_process": "Through any commercial bank, RRB, or cooperative bank",
            "validity": "5 years",
            "repayment_period": "12 months for crops, 5 years for assets",
            "benefits": [
                "No collateral required up to Rs. 1.6 lakh",
                "Interest subvention available",
                "Flexible repayment based on harvesting cycle",
                "Insurance coverage available"
            ]
        }
        
        return kcc_info
        
    except Exception as e:
        logger.error(f"KCC information fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch KCC information: {str(e)}")

# Mock functions for development
async def _get_mock_government_schemes(
    category: Optional[SchemeCategory], 
    state: Optional[str], 
    limit: int
) -> List[GovernmentScheme]:
    """Generate mock government schemes"""
    
    mock_schemes = [
        GovernmentScheme(
            scheme_id="PMKISAN001",
            scheme_name="Pradhan Mantri Kisan Samman Nidhi",
            scheme_name_local="प्रधान मंत्री किसान सम्मान निधि",
            category=SchemeCategory.SUBSIDY,
            description="Income support to farmers providing Rs. 6000 per year",
            description_local="किसानों को आय सहायता प्रदान करने वाली योजना",
            ministry="Ministry of Agriculture and Farmers Welfare",
            state=None,
            benefit_amount=6000.0,
            benefit_type="cash",
            eligibility_criteria=[
                "Small and marginal farmers",
                "Land holding up to 2 hectares",
                "Valid Aadhaar card"
            ],
            required_documents=["Aadhaar", "Land records", "Bank details"],
            application_process="Online through PM-KISAN portal or Common Service Centers",
            application_deadline=None,
            scheme_duration="Ongoing",
            official_website="https://pmkisan.gov.in",
            contact_information={"helpline": "155261", "email": "pmkisan-ict@gov.in"},
            last_updated=datetime.utcnow(),
            is_active=True
        ),
        GovernmentScheme(
            scheme_id="PMFBY001",
            scheme_name="Pradhan Mantri Fasal Bima Yojana",
            scheme_name_local="प्रधान मंत्री फसल बीमा योजना",
            category=SchemeCategory.INSURANCE,
            description="Crop insurance scheme providing comprehensive risk coverage",
            description_local="व्यापक जोखिम कवरेज प्रदान करने वाली फसल बीमा योजना",
            ministry="Ministry of Agriculture and Farmers Welfare",
            state=None,
            benefit_amount=None,
            benefit_type="insurance",
            eligibility_criteria=[
                "All farmers (loanee and non-loanee)",
                "Farmers growing notified crops",
                "Sharecroppers and tenant farmers with valid agreements"
            ],
            required_documents=["Aadhaar", "Land records", "Sowing certificate", "Bank details"],
            application_process="Through banks, Common Service Centers, or online portal",
            application_deadline=date(2024, 7, 31),
            scheme_duration="Seasonal",
            official_website="https://pmfby.gov.in",
            contact_information={"helpline": "14447", "email": "support@pmfby.gov.in"},
            last_updated=datetime.utcnow(),
            is_active=True
        )
    ]
    
    return mock_schemes[:limit]

async def _get_mock_eligibility_checks(
    farmer_profile: FarmerProfile, 
    scheme_ids: Optional[List[str]]
) -> List[EligibilityCheck]:
    """Generate mock eligibility checks"""
    
    return [
        EligibilityCheck(
            scheme_id="PMKISAN001",
            scheme_name="Pradhan Mantri Kisan Samman Nidhi",
            eligibility_status=EligibilityStatus.ELIGIBLE if farmer_profile.land_holding <= 2.0 else EligibilityStatus.NOT_ELIGIBLE,
            eligibility_score=0.9 if farmer_profile.land_holding <= 2.0 else 0.3,
            eligible_benefits=["Rs. 6000 annual income support"] if farmer_profile.land_holding <= 2.0 else [],
            missing_criteria=[] if farmer_profile.land_holding <= 2.0 else ["Land holding exceeds 2 hectares"],
            required_documents=["Aadhaar card", "Land records", "Bank account details"],
            estimated_benefit=6000.0 if farmer_profile.land_holding <= 2.0 else None,
            application_steps=[
                "Visit PM-KISAN portal",
                "Fill registration form",
                "Upload required documents",
                "Submit application"
            ],
            deadline=None,
            recommendations=[
                "Ensure Aadhaar is linked to bank account",
                "Keep land documents ready"
            ]
        )
    ]

async def _get_mock_scheme_details(scheme_id: str, language: str) -> GovernmentScheme:
    """Generate mock scheme details"""
    
    return GovernmentScheme(
        scheme_id=scheme_id,
        scheme_name="Pradhan Mantri Kisan Samman Nidhi",
        scheme_name_local="प्रधान मंत्री किसान सम्मान निधि" if language == "hi" else None,
        category=SchemeCategory.SUBSIDY,
        description="Income support scheme for farmers",
        description_local="किसानों के लिए आय सहायता योजना" if language == "hi" else None,
        ministry="Ministry of Agriculture and Farmers Welfare",
        state=None,
        benefit_amount=6000.0,
        benefit_type="cash",
        eligibility_criteria=["Small and marginal farmers", "Land holding up to 2 hectares"],
        required_documents=["Aadhaar", "Land records", "Bank details"],
        application_process="Online through PM-KISAN portal",
        application_deadline=None,
        scheme_duration="Ongoing",
        official_website="https://pmkisan.gov.in",
        contact_information={"helpline": "155261"},
        last_updated=datetime.utcnow(),
        is_active=True
    )

async def _get_mock_farmer_applications(farmer_id: str, status: Optional[str]) -> List[Dict[str, Any]]:
    """Generate mock farmer applications"""
    
    return [
        {
            "application_id": "APP123456",
            "scheme_name": "Pradhan Mantri Kisan Samman Nidhi",
            "application_date": datetime.utcnow() - timedelta(days=30),
            "status": "approved",
            "benefit_amount": 2000.0,
            "disbursement_date": datetime.utcnow() - timedelta(days=15),
            "next_installment": datetime.utcnow() + timedelta(days=90)
        }
    ]
