"""
Health check endpoints for KrishiSevak Backend
"""

from fastapi import APIRouter, Depends
from pydantic import BaseModel
from datetime import datetime
import psutil
import platform
from typing import Dict, Any

from ..config import settings

router = APIRouter()

class HealthResponse(BaseModel):
    """Health check response model"""
    status: str
    timestamp: datetime
    version: str
    environment: str
    system_info: Dict[str, Any]
    services_status: Dict[str, str]

class DetailedHealthResponse(BaseModel):
    """Detailed health check response model"""
    status: str
    timestamp: datetime
    version: str
    environment: str
    system_info: Dict[str, Any]
    services_status: Dict[str, str]
    performance_metrics: Dict[str, Any]

@router.get("/health", response_model=HealthResponse)
async def health_check():
    """Basic health check endpoint"""
    
    # Get system information
    system_info = {
        "platform": platform.system(),
        "platform_version": platform.version(),
        "architecture": platform.machine(),
        "processor": platform.processor(),
        "python_version": platform.python_version(),
        "cpu_count": psutil.cpu_count(),
        "memory_total": psutil.virtual_memory().total,
        "disk_total": psutil.disk_usage('/').total,
    }
    
    # Check services status (simplified for now)
    services_status = {
        "database": "healthy",  # TODO: Add actual DB health check
        "vector_db": "healthy",  # TODO: Add vector DB health check
        "ml_models": "healthy",  # TODO: Add model loading check
        "external_apis": "healthy",  # TODO: Add external API checks
    }
    
    return HealthResponse(
        status="healthy",
        timestamp=datetime.utcnow(),
        version="1.0.0",
        environment=settings.ENVIRONMENT,
        system_info=system_info,
        services_status=services_status
    )

@router.get("/health/detailed", response_model=DetailedHealthResponse)
async def detailed_health_check():
    """Detailed health check with performance metrics"""
    
    # Get system information
    system_info = {
        "platform": platform.system(),
        "platform_version": platform.version(),
        "architecture": platform.machine(),
        "processor": platform.processor(),
        "python_version": platform.python_version(),
        "cpu_count": psutil.cpu_count(),
        "memory_total": psutil.virtual_memory().total,
        "disk_total": psutil.disk_usage('/').total,
    }
    
    # Get performance metrics
    cpu_usage = psutil.cpu_percent(interval=1)
    memory = psutil.virtual_memory()
    disk = psutil.disk_usage('/')
    
    performance_metrics = {
        "cpu_usage_percent": cpu_usage,
        "memory_usage_percent": memory.percent,
        "memory_available": memory.available,
        "memory_used": memory.used,
        "disk_usage_percent": (disk.used / disk.total) * 100,
        "disk_free": disk.free,
        "disk_used": disk.used,
    }
    
    # Check services status (simplified for now)
    services_status = {
        "database": "healthy",  # TODO: Add actual DB health check
        "vector_db": "healthy",  # TODO: Add vector DB health check
        "ml_models": "healthy",  # TODO: Add model loading check
        "external_apis": "healthy",  # TODO: Add external API checks
        "cache": "healthy",  # TODO: Add cache health check
    }
    
    # Determine overall status
    overall_status = "healthy"
    if cpu_usage > 90 or memory.percent > 90:
        overall_status = "degraded"
    if any(status == "unhealthy" for status in services_status.values()):
        overall_status = "unhealthy"
    
    return DetailedHealthResponse(
        status=overall_status,
        timestamp=datetime.utcnow(),
        version="1.0.0",
        environment=settings.ENVIRONMENT,
        system_info=system_info,
        services_status=services_status,
        performance_metrics=performance_metrics
    )

@router.get("/health/ready")
async def readiness_check():
    """Kubernetes readiness probe endpoint"""
    # TODO: Add checks for:
    # - Database connectivity
    # - Required models loaded
    # - External API availability
    return {"status": "ready"}

@router.get("/health/live")
async def liveness_check():
    """Kubernetes liveness probe endpoint"""
    return {"status": "alive"}
