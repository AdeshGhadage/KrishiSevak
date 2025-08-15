"""
Fertilizer and Crop Price Management API endpoints
Provides market prices for fertilizers and crops with location-based data
"""

from fastapi import APIRouter, HTTPException, Query, Request
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime, timedelta
from enum import Enum

from ..config import settings
from ..services.price_service import PriceService
from ..services.market_service import MarketService
from ..utils.logger import get_logger

logger = get_logger(__name__)
router = APIRouter()

class PriceType(str, Enum):
    """Price type enumeration"""
    FERTILIZER = "fertilizer"
    CROP = "crop"
    SEED = "seed"
    PESTICIDE = "pesticide"

class PriceData(BaseModel):
    """Price data model"""
    item_name: str
    item_type: PriceType
    current_price: float
    unit: str  # kg, quintal, liter, etc.
    currency: str = "INR"
    location: str
    market_name: str
    last_updated: datetime
    price_trend: str  # increasing, decreasing, stable
    price_change_percent: float
    historical_prices: List[Dict[str, Any]]

class FertilizerPrice(BaseModel):
    """Fertilizer price model"""
    fertilizer_name: str
    fertilizer_type: str  # urea, dap, potash, organic, etc.
    price_per_kg: float
    brand: str
    composition: str  # NPK ratio, etc.
    location: str
    dealer_name: str
    dealer_contact: Optional[str]
    availability: str  # in_stock, limited, out_of_stock
    subsidized_price: Optional[float]
    market_price: float
    last_updated: datetime

class CropPrice(BaseModel):
    """Crop price model"""
    crop_name: str
    variety: str
    price_per_quintal: float
    market_name: str
    location: str
    grade: str  # A, B, C quality grades
    moisture_content: float
    arrival_quantity: float  # quintals arrived in market
    price_trend: str
    min_price: float
    max_price: float
    modal_price: float  # most common price
    last_updated: datetime

class PriceAlert(BaseModel):
    """Price alert model"""
    alert_id: str
    user_id: str
    item_name: str
    item_type: PriceType
    target_price: float
    current_price: float
    alert_type: str  # price_drop, price_rise, target_reached
    location: str
    created_date: datetime
    is_active: bool

class PriceComparisonRequest(BaseModel):
    """Price comparison request model"""
    item_name: str
    item_type: PriceType
    locations: List[str]
    date_range: Optional[int] = Field(30, description="Days to compare")

@router.get("/prices/fertilizers", response_model=List[FertilizerPrice])
async def get_fertilizer_prices(
    request: Request,
    location: str = Query(..., description="Location/district for price data"),
    fertilizer_type: Optional[str] = Query(None, description="Type of fertilizer"),
    brand: Optional[str] = Query(None, description="Brand name"),
    limit: int = Query(20, description="Maximum number of results")
):
    """
    Get current fertilizer prices for specified location
    
    Provides:
    - Current market prices
    - Subsidized prices where applicable
    - Brand comparisons
    - Dealer information
    - Availability status
    """
    
    try:
        price_service = getattr(request.app.state, 'price_service', None)
        
        if not price_service:
            logger.warning("Price service not initialized, returning mock data")
            return await _get_mock_fertilizer_prices(location, fertilizer_type, limit)
        
        fertilizer_prices = await price_service.get_fertilizer_prices(
            location=location,
            fertilizer_type=fertilizer_type,
            brand=brand,
            limit=limit
        )
        
        return fertilizer_prices
        
    except Exception as e:
        logger.error(f"Fertilizer price fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch fertilizer prices: {str(e)}")

@router.get("/prices/crops", response_model=List[CropPrice])
async def get_crop_prices(
    request: Request,
    location: str = Query(..., description="Location/district for price data"),
    crop_name: Optional[str] = Query(None, description="Name of the crop"),
    variety: Optional[str] = Query(None, description="Crop variety"),
    limit: int = Query(20, description="Maximum number of results")
):
    """
    Get current crop market prices for specified location
    
    Provides:
    - Current market rates
    - Price trends
    - Quality grades
    - Market arrival data
    - Min/max/modal prices
    """
    
    try:
        price_service = getattr(request.app.state, 'price_service', None)
        
        if not price_service:
            logger.warning("Price service not initialized, returning mock data")
            return await _get_mock_crop_prices(location, crop_name, limit)
        
        crop_prices = await price_service.get_crop_prices(
            location=location,
            crop_name=crop_name,
            variety=variety,
            limit=limit
        )
        
        return crop_prices
        
    except Exception as e:
        logger.error(f"Crop price fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch crop prices: {str(e)}")

@router.post("/prices/compare")
async def compare_prices(
    request: Request,
    comparison_request: PriceComparisonRequest
):
    """
    Compare prices across multiple locations and time periods
    
    Provides:
    - Price comparison across locations
    - Historical price trends
    - Best markets to buy/sell
    - Price forecasts
    """
    
    try:
        price_service = getattr(request.app.state, 'price_service', None)
        
        if not price_service:
            return await _get_mock_price_comparison(comparison_request)
        
        comparison_data = await price_service.compare_prices(
            item_name=comparison_request.item_name,
            item_type=comparison_request.item_type,
            locations=comparison_request.locations,
            date_range=comparison_request.date_range
        )
        
        return comparison_data
        
    except Exception as e:
        logger.error(f"Price comparison failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to compare prices: {str(e)}")

@router.get("/prices/trends/{item_name}")
async def get_price_trends(
    request: Request,
    item_name: str,
    item_type: PriceType,
    location: str = Query(..., description="Location for price trends"),
    days: int = Query(30, description="Number of days for trend analysis")
):
    """Get price trends and forecasts for specific item"""
    
    try:
        price_service = getattr(request.app.state, 'price_service', None)
        
        if not price_service:
            return await _get_mock_price_trends(item_name, location, days)
        
        trends = await price_service.get_price_trends(
            item_name=item_name,
            item_type=item_type,
            location=location,
            days=days
        )
        
        return trends
        
    except Exception as e:
        logger.error(f"Price trends fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch price trends: {str(e)}")

@router.post("/prices/alerts")
async def create_price_alert(
    request: Request,
    item_name: str,
    item_type: PriceType,
    target_price: float,
    location: str,
    alert_type: str = "target_reached",
    user_id: str = "default_user"
):
    """Create price alert for specific item and target price"""
    
    try:
        # TODO: Implement actual price alert creation
        alert = PriceAlert(
            alert_id=f"alert_{datetime.utcnow().timestamp()}",
            user_id=user_id,
            item_name=item_name,
            item_type=item_type,
            target_price=target_price,
            current_price=0.0,  # Will be updated by background service
            alert_type=alert_type,
            location=location,
            created_date=datetime.utcnow(),
            is_active=True
        )
        
        return {
            "message": "Price alert created successfully",
            "alert": alert,
            "status": "active"
        }
        
    except Exception as e:
        logger.error(f"Price alert creation failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to create price alert: {str(e)}")

@router.get("/prices/markets/{location}")
async def get_nearby_markets(
    request: Request,
    location: str,
    radius_km: int = Query(50, description="Search radius in kilometers"),
    market_type: Optional[str] = Query(None, description="Type of market")
):
    """Get nearby markets for specified location"""
    
    try:
        market_service = getattr(request.app.state, 'market_service', None)
        
        if not market_service:
            return await _get_mock_nearby_markets(location, radius_km)
        
        markets = await market_service.get_nearby_markets(
            location=location,
            radius_km=radius_km,
            market_type=market_type
        )
        
        return {"markets": markets, "total_markets": len(markets)}
        
    except Exception as e:
        logger.error(f"Nearby markets fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch nearby markets: {str(e)}")

@router.get("/prices/subsidies")
async def get_subsidy_info(
    request: Request,
    location: str = Query(..., description="Location for subsidy information"),
    item_type: Optional[PriceType] = Query(None, description="Type of subsidized item")
):
    """Get government subsidy information for agricultural inputs"""
    
    try:
        # TODO: Implement actual subsidy information fetch
        subsidies = [
            {
                "scheme_name": "Pradhan Mantri Kisan Samman Nidhi",
                "item_type": "fertilizer",
                "subsidy_amount": 6000,
                "subsidy_percentage": 50,
                "eligibility_criteria": "Small and marginal farmers",
                "application_process": "Online through PM-KISAN portal",
                "last_updated": datetime.utcnow()
            }
        ]
        
        return {"subsidies": subsidies, "location": location}
        
    except Exception as e:
        logger.error(f"Subsidy information fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch subsidy information: {str(e)}")

# Mock functions for development
async def _get_mock_fertilizer_prices(location: str, fertilizer_type: Optional[str], limit: int) -> List[FertilizerPrice]:
    """Generate mock fertilizer prices"""
    
    mock_prices = [
        FertilizerPrice(
            fertilizer_name="Urea",
            fertilizer_type="nitrogen",
            price_per_kg=6.50,
            brand="IFFCO",
            composition="46% N",
            location=location,
            dealer_name="Krishi Kendra",
            dealer_contact="+91-9876543210",
            availability="in_stock",
            subsidized_price=5.80,
            market_price=12.00,
            last_updated=datetime.utcnow()
        ),
        FertilizerPrice(
            fertilizer_name="DAP",
            fertilizer_type="phosphorus",
            price_per_kg=27.50,
            brand="KRIBHCO",
            composition="18:46:0",
            location=location,
            dealer_name="Agri Supply Store",
            dealer_contact="+91-9876543211",
            availability="limited",
            subsidized_price=24.00,
            market_price=32.00,
            last_updated=datetime.utcnow()
        )
    ]
    
    return mock_prices[:limit]

async def _get_mock_crop_prices(location: str, crop_name: Optional[str], limit: int) -> List[CropPrice]:
    """Generate mock crop prices"""
    
    mock_prices = [
        CropPrice(
            crop_name="Wheat",
            variety="HD-2967",
            price_per_quintal=2150.0,
            market_name="Mandi Samiti",
            location=location,
            grade="A",
            moisture_content=12.0,
            arrival_quantity=500.0,
            price_trend="increasing",
            min_price=2100.0,
            max_price=2200.0,
            modal_price=2150.0,
            last_updated=datetime.utcnow()
        ),
        CropPrice(
            crop_name="Rice",
            variety="Basmati-1121",
            price_per_quintal=4500.0,
            market_name="Wholesale Market",
            location=location,
            grade="A",
            moisture_content=14.0,
            arrival_quantity=300.0,
            price_trend="stable",
            min_price=4400.0,
            max_price=4600.0,
            modal_price=4500.0,
            last_updated=datetime.utcnow()
        )
    ]
    
    return mock_prices[:limit]

async def _get_mock_price_comparison(comparison_request: PriceComparisonRequest) -> Dict[str, Any]:
    """Generate mock price comparison"""
    
    return {
        "item_name": comparison_request.item_name,
        "item_type": comparison_request.item_type,
        "comparison_data": [
            {
                "location": loc,
                "current_price": 2150.0 + (hash(loc) % 100),
                "average_price": 2100.0 + (hash(loc) % 100),
                "price_trend": "stable",
                "last_updated": datetime.utcnow()
            }
            for loc in comparison_request.locations
        ],
        "best_buy_location": comparison_request.locations[0],
        "best_sell_location": comparison_request.locations[-1],
        "price_forecast": "Prices expected to remain stable"
    }

async def _get_mock_price_trends(item_name: str, location: str, days: int) -> Dict[str, Any]:
    """Generate mock price trends"""
    
    base_price = 2150.0
    trends = []
    
    for i in range(days):
        date = datetime.utcnow() - timedelta(days=days-i)
        price = base_price + (i % 10 - 5) * 20
        trends.append({
            "date": date,
            "price": price,
            "volume": 100 + (i % 20)
        })
    
    return {
        "item_name": item_name,
        "location": location,
        "trends": trends,
        "overall_trend": "stable",
        "forecast": "Prices likely to increase by 5% next month",
        "volatility": "low"
    }

async def _get_mock_nearby_markets(location: str, radius_km: int) -> List[Dict[str, Any]]:
    """Generate mock nearby markets"""
    
    return [
        {
            "market_name": "Central Mandi",
            "location": f"{location} City",
            "distance_km": 5.2,
            "market_type": "wholesale",
            "operating_hours": "06:00 - 18:00",
            "contact": "+91-9876543210",
            "facilities": ["cold_storage", "weighing", "grading"]
        },
        {
            "market_name": "Krishi Upaj Mandi",
            "location": f"{location} Rural",
            "distance_km": 12.8,
            "market_type": "retail",
            "operating_hours": "07:00 - 19:00",
            "contact": "+91-9876543211",
            "facilities": ["storage", "transport"]
        }
    ]
