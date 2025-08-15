"""
Weather and Irrigation Support API endpoints
Provides village-level weather forecasts and crop-specific irrigation scheduling
"""

from fastapi import APIRouter, HTTPException, Query, Request
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime, timedelta
import asyncio

from ..config import settings
from ..services.weather_service import WeatherService
from ..services.irrigation_service import IrrigationService
from ..utils.logger import get_logger

logger = get_logger(__name__)
router = APIRouter()

class LocationRequest(BaseModel):
    """Location request model"""
    latitude: float = Field(..., description="Latitude coordinate")
    longitude: float = Field(..., description="Longitude coordinate")
    village_name: Optional[str] = Field(None, description="Village name")
    district: Optional[str] = Field(None, description="District name")
    state: Optional[str] = Field(None, description="State name")

class WeatherData(BaseModel):
    """Weather data model"""
    timestamp: datetime
    temperature: float  # Celsius
    humidity: float  # Percentage
    pressure: float  # hPa
    wind_speed: float  # km/h
    wind_direction: str
    precipitation: float  # mm
    precipitation_probability: float  # Percentage
    weather_condition: str
    weather_description: str
    visibility: float  # km
    uv_index: float

class WeatherForecast(BaseModel):
    """Weather forecast response model"""
    location: LocationRequest
    current_weather: WeatherData
    hourly_forecast: List[WeatherData]  # Next 24 hours
    daily_forecast: List[WeatherData]   # Next 7 days
    farming_advisory: List[str]
    alerts: List[Dict[str, Any]]
    last_updated: datetime

class IrrigationSchedule(BaseModel):
    """Irrigation schedule model"""
    crop_type: str
    crop_stage: str  # seedling, vegetative, flowering, fruiting, maturity
    next_irrigation_date: datetime
    irrigation_duration: int  # minutes
    water_requirement: float  # liters per square meter
    irrigation_method: str  # drip, sprinkler, flood
    soil_moisture_level: float  # percentage
    recommendations: List[str]
    water_conservation_tips: List[str]

class CropIrrigationRequest(BaseModel):
    """Crop irrigation request model"""
    location: LocationRequest
    crop_type: str
    crop_stage: str
    planting_date: datetime
    field_size: float  # hectares
    soil_type: str
    irrigation_method: str = "drip"
    last_irrigation_date: Optional[datetime] = None

@router.post("/weather/current", response_model=WeatherForecast)
async def get_current_weather(
    request: Request,
    location: LocationRequest
):
    """
    Get current weather and forecast for specified location
    
    Provides:
    - Current weather conditions
    - 24-hour hourly forecast
    - 7-day daily forecast
    - Farming-specific advisory
    - Weather alerts
    """
    
    try:
        # Get weather service from app state
        weather_service = getattr(request.app.state, 'weather_service', None)
        
        if not weather_service:
            # Fallback for development
            logger.warning("Weather service not initialized, returning mock data")
            return await _get_mock_weather_forecast(location)
        
        # Fetch current weather and forecast
        current_weather_task = weather_service.get_current_weather(
            latitude=location.latitude,
            longitude=location.longitude
        )
        
        hourly_forecast_task = weather_service.get_hourly_forecast(
            latitude=location.latitude,
            longitude=location.longitude,
            hours=24
        )
        
        daily_forecast_task = weather_service.get_daily_forecast(
            latitude=location.latitude,
            longitude=location.longitude,
            days=7
        )
        
        # Execute requests in parallel
        current_weather, hourly_forecast, daily_forecast = await asyncio.gather(
            current_weather_task,
            hourly_forecast_task,
            daily_forecast_task
        )
        
        # Generate farming advisory
        farming_advisory = await weather_service.generate_farming_advisory(
            current_weather, daily_forecast, location
        )
        
        # Check for weather alerts
        alerts = await weather_service.get_weather_alerts(
            latitude=location.latitude,
            longitude=location.longitude
        )
        
        return WeatherForecast(
            location=location,
            current_weather=current_weather,
            hourly_forecast=hourly_forecast,
            daily_forecast=daily_forecast,
            farming_advisory=farming_advisory,
            alerts=alerts,
            last_updated=datetime.utcnow()
        )
        
    except Exception as e:
        logger.error(f"Weather data fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch weather data: {str(e)}")

@router.post("/irrigation/schedule", response_model=IrrigationSchedule)
async def get_irrigation_schedule(
    request: Request,
    irrigation_request: CropIrrigationRequest
):
    """
    Generate crop-specific irrigation schedule based on weather and soil conditions
    
    Considers:
    - Current and forecasted weather
    - Crop type and growth stage
    - Soil moisture levels
    - Water conservation practices
    """
    
    try:
        # Get services from app state
        irrigation_service = getattr(request.app.state, 'irrigation_service', None)
        weather_service = getattr(request.app.state, 'weather_service', None)
        
        if not irrigation_service:
            logger.warning("Irrigation service not initialized, returning mock data")
            return await _get_mock_irrigation_schedule(irrigation_request)
        
        # Get current weather and forecast for irrigation planning
        current_weather = await weather_service.get_current_weather(
            latitude=irrigation_request.location.latitude,
            longitude=irrigation_request.location.longitude
        )
        
        forecast = await weather_service.get_daily_forecast(
            latitude=irrigation_request.location.latitude,
            longitude=irrigation_request.location.longitude,
            days=7
        )
        
        # Calculate irrigation schedule
        schedule = await irrigation_service.calculate_irrigation_schedule(
            crop_type=irrigation_request.crop_type,
            crop_stage=irrigation_request.crop_stage,
            planting_date=irrigation_request.planting_date,
            field_size=irrigation_request.field_size,
            soil_type=irrigation_request.soil_type,
            irrigation_method=irrigation_request.irrigation_method,
            last_irrigation_date=irrigation_request.last_irrigation_date,
            current_weather=current_weather,
            weather_forecast=forecast
        )
        
        return schedule
        
    except Exception as e:
        logger.error(f"Irrigation schedule generation failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to generate irrigation schedule: {str(e)}")

@router.get("/weather/alerts")
async def get_weather_alerts(
    request: Request,
    latitude: float = Query(..., description="Latitude coordinate"),
    longitude: float = Query(..., description="Longitude coordinate"),
    alert_types: Optional[str] = Query(None, description="Comma-separated alert types")
):
    """Get weather alerts for specified location"""
    
    try:
        weather_service = getattr(request.app.state, 'weather_service', None)
        
        if not weather_service:
            return {"alerts": [], "message": "Weather service not available"}
        
        alert_type_list = alert_types.split(',') if alert_types else None
        
        alerts = await weather_service.get_weather_alerts(
            latitude=latitude,
            longitude=longitude,
            alert_types=alert_type_list
        )
        
        return {"alerts": alerts, "total_alerts": len(alerts)}
        
    except Exception as e:
        logger.error(f"Weather alerts fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch weather alerts: {str(e)}")

@router.get("/irrigation/water-requirements/{crop_type}")
async def get_crop_water_requirements(
    crop_type: str,
    crop_stage: str = Query(..., description="Current crop growth stage"),
    location: Optional[str] = Query(None, description="Location for climate-specific data")
):
    """Get water requirements for specific crop and growth stage"""
    
    try:
        # TODO: Implement actual crop water requirement calculation
        water_requirements = {
            "rice": {"seedling": 5, "vegetative": 8, "flowering": 12, "fruiting": 10, "maturity": 6},
            "wheat": {"seedling": 3, "vegetative": 6, "flowering": 8, "fruiting": 7, "maturity": 4},
            "tomato": {"seedling": 2, "vegetative": 4, "flowering": 6, "fruiting": 8, "maturity": 5},
            "cotton": {"seedling": 3, "vegetative": 7, "flowering": 10, "fruiting": 12, "maturity": 8}
        }
        
        if crop_type not in water_requirements:
            raise HTTPException(status_code=404, detail=f"Water requirements for {crop_type} not available")
        
        if crop_stage not in water_requirements[crop_type]:
            raise HTTPException(status_code=404, detail=f"Water requirements for {crop_stage} stage not available")
        
        requirement = water_requirements[crop_type][crop_stage]
        
        return {
            "crop_type": crop_type,
            "crop_stage": crop_stage,
            "water_requirement": requirement,
            "unit": "liters per square meter per day",
            "recommendations": [
                f"Apply {requirement}L/m² daily for {crop_stage} stage",
                "Monitor soil moisture levels regularly",
                "Adjust based on rainfall and temperature"
            ]
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Water requirements fetch failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to fetch water requirements: {str(e)}")

async def _get_mock_weather_forecast(location: LocationRequest) -> WeatherForecast:
    """Generate mock weather forecast for development"""
    
    now = datetime.utcnow()
    
    current_weather = WeatherData(
        timestamp=now,
        temperature=28.5,
        humidity=65.0,
        pressure=1013.2,
        wind_speed=12.0,
        wind_direction="SW",
        precipitation=0.0,
        precipitation_probability=20.0,
        weather_condition="Partly Cloudy",
        weather_description="Partly cloudy with light winds",
        visibility=10.0,
        uv_index=6.0
    )
    
    # Generate hourly forecast (next 24 hours)
    hourly_forecast = []
    for i in range(1, 25):
        hourly_forecast.append(WeatherData(
            timestamp=now + timedelta(hours=i),
            temperature=28.5 + (i % 12 - 6) * 0.8,
            humidity=65.0 + (i % 8 - 4) * 2,
            pressure=1013.2,
            wind_speed=12.0,
            wind_direction="SW",
            precipitation=0.0 if i % 6 != 0 else 2.5,
            precipitation_probability=20.0 + (i % 4) * 10,
            weather_condition="Partly Cloudy",
            weather_description="Partly cloudy",
            visibility=10.0,
            uv_index=6.0 if 6 <= i <= 18 else 0.0
        ))
    
    # Generate daily forecast (next 7 days)
    daily_forecast = []
    for i in range(1, 8):
        daily_forecast.append(WeatherData(
            timestamp=now + timedelta(days=i),
            temperature=28.5 + (i % 5 - 2) * 1.5,
            humidity=65.0,
            pressure=1013.2,
            wind_speed=12.0,
            wind_direction="SW",
            precipitation=5.0 if i % 3 == 0 else 0.0,
            precipitation_probability=30.0 + (i % 3) * 20,
            weather_condition="Partly Cloudy",
            weather_description="Partly cloudy with occasional rain",
            visibility=10.0,
            uv_index=6.0
        ))
    
    farming_advisory = [
        "Good weather for field operations",
        "Light rain expected in 3 days - plan irrigation accordingly",
        "UV levels moderate - safe for extended field work"
    ]
    
    alerts = []
    
    return WeatherForecast(
        location=location,
        current_weather=current_weather,
        hourly_forecast=hourly_forecast,
        daily_forecast=daily_forecast,
        farming_advisory=farming_advisory,
        alerts=alerts,
        last_updated=now
    )

async def _get_mock_irrigation_schedule(irrigation_request: CropIrrigationRequest) -> IrrigationSchedule:
    """Generate mock irrigation schedule for development"""
    
    next_irrigation = datetime.utcnow() + timedelta(days=2)
    
    return IrrigationSchedule(
        crop_type=irrigation_request.crop_type,
        crop_stage=irrigation_request.crop_stage,
        next_irrigation_date=next_irrigation,
        irrigation_duration=45,
        water_requirement=6.0,
        irrigation_method=irrigation_request.irrigation_method,
        soil_moisture_level=35.0,
        recommendations=[
            "Apply irrigation early morning for better efficiency",
            "Monitor soil moisture before next irrigation",
            "Reduce frequency if rain is forecasted"
        ],
        water_conservation_tips=[
            "Use drip irrigation for 30% water savings",
            "Apply mulch to reduce evaporation",
            "Schedule irrigation during cooler hours"
        ]
    )
