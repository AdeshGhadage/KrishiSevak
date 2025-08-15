"""
Logging configuration for KrishiSevak Backend
"""

import logging
import logging.handlers
import sys
from pathlib import Path
import structlog
from datetime import datetime

from ..config import settings

def setup_logger(name: str = None) -> structlog.stdlib.BoundLogger:
    """
    Setup structured logging for the application
    
    Args:
        name: Logger name (usually __name__)
        
    Returns:
        Configured structlog logger
    """
    
    # Ensure log directory exists
    log_file_path = Path(settings.LOG_FILE)
    log_file_path.parent.mkdir(parents=True, exist_ok=True)
    
    # Configure standard library logging
    logging.basicConfig(
        format="%(message)s",
        stream=sys.stdout,
        level=getattr(logging, settings.LOG_LEVEL.upper())
    )
    
    # File handler for persistent logging
    file_handler = logging.handlers.RotatingFileHandler(
        settings.LOG_FILE,
        maxBytes=10*1024*1024,  # 10MB
        backupCount=5
    )
    file_handler.setLevel(logging.INFO)
    
    # Configure structlog
    structlog.configure(
        processors=[
            structlog.stdlib.filter_by_level,
            structlog.stdlib.add_logger_name,
            structlog.stdlib.add_log_level,
            structlog.stdlib.PositionalArgumentsFormatter(),
            structlog.processors.TimeStamper(fmt="iso"),
            structlog.processors.StackInfoRenderer(),
            structlog.processors.format_exc_info,
            structlog.processors.UnicodeDecoder(),
            structlog.processors.JSONRenderer()
        ],
        context_class=dict,
        logger_factory=structlog.stdlib.LoggerFactory(),
        wrapper_class=structlog.stdlib.BoundLogger,
        cache_logger_on_first_use=True,
    )
    
    # Get logger
    logger = structlog.get_logger(name)
    
    # Add file handler to the underlying logger
    underlying_logger = logging.getLogger(name)
    underlying_logger.addHandler(file_handler)
    
    return logger

def get_logger(name: str = None) -> structlog.stdlib.BoundLogger:
    """
    Get a logger instance
    
    Args:
        name: Logger name (usually __name__)
        
    Returns:
        Logger instance
    """
    return structlog.get_logger(name)

class LoggerMiddleware:
    """
    Middleware for logging HTTP requests and responses
    """
    
    def __init__(self, app):
        self.app = app
        self.logger = get_logger("http")
    
    async def __call__(self, scope, receive, send):
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return
        
        start_time = datetime.utcnow()
        
        # Log request
        self.logger.info(
            "HTTP request started",
            method=scope.get("method"),
            path=scope.get("path"),
            query_string=scope.get("query_string", b"").decode(),
            client=scope.get("client"),
            headers=dict(scope.get("headers", []))
        )
        
        async def send_wrapper(message):
            if message["type"] == "http.response.start":
                # Calculate response time
                end_time = datetime.utcnow()
                response_time = (end_time - start_time).total_seconds()
                
                # Log response
                self.logger.info(
                    "HTTP request completed",
                    method=scope.get("method"),
                    path=scope.get("path"),
                    status_code=message.get("status"),
                    response_time=response_time,
                    headers=dict(message.get("headers", []))
                )
            
            await send(message)
        
        await self.app(scope, receive, send_wrapper)
