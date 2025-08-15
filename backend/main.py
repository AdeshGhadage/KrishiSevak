"""
KrishiSevak Backend - FastAPI Application
Intelligent agricultural advisor with multilingual support
"""

from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn
from contextlib import asynccontextmanager
import logging

from .config import settings
from .routers import (
    disease_detection,
    weather_service,
    price_management,
    government_schemes,
    nlp_service,
    health
)
from .services.rag_service import RAGService
from .utils.logger import setup_logger

# Setup logging
logger = setup_logger(__name__)

# Global RAG service instance
rag_service = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan management"""
    global rag_service
    
    # Startup
    logger.info("Starting KrishiSevak Backend...")
    try:
        # Initialize RAG service
        rag_service = RAGService()
        await rag_service.initialize()
        app.state.rag_service = rag_service
        logger.info("RAG service initialized successfully")
    except Exception as e:
        logger.error(f"Failed to initialize RAG service: {e}")
        raise
    
    yield
    
    # Shutdown
    logger.info("Shutting down KrishiSevak Backend...")
    if rag_service:
        await rag_service.cleanup()

# Create FastAPI application
app = FastAPI(
    title="KrishiSevak - Agricultural Advisor API",
    description="Intelligent multilingual agricultural advisor for Indian farmers",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router, prefix="/api/v1", tags=["Health"])
app.include_router(disease_detection.router, prefix="/api/v1", tags=["Disease Detection"])
app.include_router(weather_service.router, prefix="/api/v1", tags=["Weather & Irrigation"])
app.include_router(price_management.router, prefix="/api/v1", tags=["Price Management"])
app.include_router(government_schemes.router, prefix="/api/v1", tags=["Government Schemes"])
app.include_router(nlp_service.router, prefix="/api/v1", tags=["NLP & Voice"])

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "KrishiSevak - Agricultural Advisor API",
        "version": "1.0.0",
        "status": "active",
        "docs": "/docs"
    }

@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    """Global HTTP exception handler"""
    logger.error(f"HTTP Exception: {exc.status_code} - {exc.detail}")
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": exc.detail, "status_code": exc.status_code}
    )

@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    """Global exception handler"""
    logger.error(f"Unhandled exception: {str(exc)}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"error": "Internal server error", "status_code": 500}
    )

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG,
        log_level="info"
    )
