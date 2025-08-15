#!/bin/bash

# KrishiSevak Backend Setup Script
# This script sets up the virtual environment and installs dependencies

set -e  # Exit on any error

echo "🌾 Setting up KrishiSevak Backend..."

# Check if Python 3.8+ is available
python_version=$(python3 --version 2>&1 | cut -d' ' -f2)
required_version="3.8"

if ! python3 -c "import sys; sys.exit(0 if sys.version_info >= (3,8) else 1)" 2>/dev/null; then
    echo "❌ Error: Python 3.8 or higher is required. Found: $python_version"
    echo "Please install Python 3.8+ and try again."
    exit 1
fi

echo "✅ Python version: $python_version"

# Create backend directory if it doesn't exist
mkdir -p backend
cd backend

# Create virtual environment
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python3 -m venv venv
    echo "✅ Virtual environment created"
else
    echo "✅ Virtual environment already exists"
fi

# Activate virtual environment
echo "🔧 Activating virtual environment..."
source venv/bin/activate

# Upgrade pip
echo "⬆️ Upgrading pip..."
pip install --upgrade pip

# Install dependencies
echo "📚 Installing dependencies..."
if [ -f "requirements.txt" ]; then
    pip install -r requirements.txt
    echo "✅ Dependencies installed"
else
    echo "❌ Error: requirements.txt not found"
    exit 1
fi

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p data/faiss_index
mkdir -p logs
mkdir -p uploads
mkdir -p models/vit_disease_detection
mkdir -p models/whisper

# Create .env file from template if it doesn't exist
if [ ! -f ".env" ]; then
    echo "⚙️ Creating .env configuration file..."
    cat > .env << 'EOF'
# KrishiSevak Backend Configuration

# Server Configuration
HOST=0.0.0.0
PORT=8000
DEBUG=true
ENVIRONMENT=development

# Security
SECRET_KEY=your-secret-key-change-this-in-production
ALLOWED_ORIGINS=["http://localhost:3000", "http://127.0.0.1:3000"]

# Database
DATABASE_URL=sqlite:///./krishisevak.db

# AI/ML Models
VISION_MODEL_PATH=./models/vit_disease_detection
GEMINI_API_KEY=your-gemini-api-key-here
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama2

# Voice Processing
WHISPER_MODEL_PATH=./models/whisper
ESPEAK_PATH=/usr/bin/espeak

# RAG Configuration
VECTOR_DB_TYPE=faiss
PINECONE_API_KEY=your-pinecone-api-key-here
PINECONE_ENVIRONMENT=your-pinecone-environment
FAISS_INDEX_PATH=./data/faiss_index

# External APIs
OPENWEATHER_API_KEY=your-openweather-api-key-here
GOVERNMENT_DATA_BASE_URL=https://api.data.gov.in

# File Storage
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=10485760

# Cache
REDIS_URL=redis://localhost:6379
CACHE_TTL=3600

# Logging
LOG_LEVEL=INFO
LOG_FILE=./logs/krishisevak.log
EOF

    echo "✅ .env file created. Please update with your API keys!"
else
    echo "✅ .env file already exists"
fi

# Create a startup script
echo "🚀 Creating startup script..."
cat > run.sh << 'EOF'
#!/bin/bash
# KrishiSevak Backend Startup Script

echo "🌾 Starting KrishiSevak Backend..."

# Activate virtual environment
source venv/bin/activate

# Check if .env exists
if [ ! -f ".env" ]; then
    echo "❌ Error: .env file not found. Please create one from the template."
    exit 1
fi

# Start the FastAPI server
echo "🚀 Starting FastAPI server..."
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
EOF

chmod +x run.sh

# Create development script
echo "🛠️ Creating development script..."
cat > dev.sh << 'EOF'
#!/bin/bash
# KrishiSevak Backend Development Script

echo "🔧 Starting KrishiSevak Backend in Development Mode..."

# Activate virtual environment
source venv/bin/activate

# Install development dependencies if needed
pip install pytest pytest-asyncio pytest-cov black isort flake8 mypy

# Start with hot reload
echo "🚀 Starting with hot reload..."
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload --log-level debug
EOF

chmod +x dev.sh

# Create test script
echo "🧪 Creating test script..."
cat > test.sh << 'EOF'
#!/bin/bash
# KrishiSevak Backend Test Script

echo "🧪 Running KrishiSevak Backend Tests..."

# Activate virtual environment
source venv/bin/activate

# Run tests
python -m pytest tests/ -v --cov=. --cov-report=html

echo "✅ Tests completed. Coverage report available in htmlcov/"
EOF

chmod +x test.sh

# Create requirements check script
echo "📋 Creating requirements check script..."
cat > check_requirements.py << 'EOF'
#!/usr/bin/env python3
"""
Check if all required dependencies are available
"""

import sys
import importlib

required_packages = [
    'fastapi',
    'uvicorn',
    'pydantic',
    'sqlalchemy',
    'PIL',
    'numpy',
    'requests',
    'python_multipart',
    'pydantic_settings'
]

optional_packages = [
    ('torch', 'PyTorch for ML models'),
    ('transformers', 'Hugging Face Transformers'),
    ('faiss', 'FAISS for vector search'),
    ('openai', 'OpenAI API'),
    ('redis', 'Redis for caching'),
]

def check_package(package_name):
    try:
        importlib.import_module(package_name)
        return True
    except ImportError:
        return False

print("🔍 Checking required dependencies...")

missing_required = []
for package in required_packages:
    if check_package(package):
        print(f"✅ {package}")
    else:
        print(f"❌ {package}")
        missing_required.append(package)

print("\n🔍 Checking optional dependencies...")
for package, description in optional_packages:
    if check_package(package):
        print(f"✅ {package} - {description}")
    else:
        print(f"⚠️  {package} - {description} (optional)")

if missing_required:
    print(f"\n❌ Missing required packages: {missing_required}")
    print("Run: pip install -r requirements.txt")
    sys.exit(1)
else:
    print("\n✅ All required dependencies are available!")
    
print("\n🚀 Backend is ready to run!")
print("Start with: ./run.sh")
print("Development mode: ./dev.sh")
print("Run tests: ./test.sh")
EOF

chmod +x check_requirements.py

echo ""
echo "🎉 KrishiSevak Backend setup completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Update API keys in backend/.env file"
echo "2. Check dependencies: cd backend && python check_requirements.py"
echo "3. Start development server: cd backend && ./dev.sh"
echo "4. Open http://localhost:8000/docs for API documentation"
echo ""
echo "📚 Available scripts:"
echo "  ./run.sh        - Start production server"
echo "  ./dev.sh        - Start development server with hot reload"
echo "  ./test.sh       - Run tests with coverage"
echo "  python check_requirements.py - Check dependencies"
echo ""
echo "📖 Documentation will be available at:"
echo "  http://localhost:8000/docs (Swagger UI)"
echo "  http://localhost:8000/redoc (ReDoc)"
echo ""

# Return to original directory
cd ..

echo "✅ Setup complete! Backend is ready for development."
