# KrishiSevak Backend

Intelligent multilingual agricultural advisor API built with FastAPI.

## Features

- **Plant Disease Detection**: ViT-based image classification for crop disease identification
- **Weather & Irrigation**: Village-level weather forecasts and crop-specific irrigation scheduling
- **Price Management**: Real-time fertilizer and crop prices with market comparison
- **Government Schemes**: Automated eligibility checking for agricultural subsidies and schemes
- **Multilingual NLP**: Voice and text processing in 12+ Indian languages
- **RAG System**: Knowledge retrieval using FAISS/Pinecone vector database

## Tech Stack

- **Framework**: FastAPI (Python)
- **AI/ML**: PyTorch, Transformers, ViT, Gemini/Ollama
- **Vector DB**: FAISS/Pinecone for RAG
- **Voice**: Whisper.cpp (STT), eSpeak-NG (TTS)
- **Database**: SQLAlchemy with PostgreSQL/SQLite
- **Cache**: Redis
- **Monitoring**: Prometheus, Structlog

## Quick Start

### 1. Setup Backend

```bash
# Run the setup script (from project root)
./setup_backend.sh
```

This will:
- Create virtual environment
- Install all dependencies
- Set up directory structure
- Create configuration files
- Generate helper scripts

### 2. Configure Environment

Edit `backend/.env` file with your API keys:

```bash
cd backend
cp .env.example .env
# Edit .env with your API keys
```

Required API keys:
- `GEMINI_API_KEY`: Google Gemini API key
- `OPENWEATHER_API_KEY`: OpenWeatherMap API key
- `PINECONE_API_KEY`: Pinecone vector database (optional)

### 3. Start Development Server

```bash
cd backend
./dev.sh
```

The API will be available at:
- **API**: http://localhost:8000
- **Docs**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

## Development

### Project Structure

```
backend/
├── main.py                 # FastAPI application
├── config.py              # Configuration settings
├── requirements.txt       # Python dependencies
├── routers/               # API endpoints
│   ├── disease_detection.py
│   ├── weather_service.py
│   ├── price_management.py
│   ├── government_schemes.py
│   ├── nlp_service.py
│   └── health.py
├── services/              # Business logic
│   ├── rag_service.py
│   ├── vision_service.py
│   ├── weather_service.py
│   └── nlp_service.py
├── utils/                 # Utilities
│   ├── logger.py
│   └── image_utils.py
├── data/                  # Data storage
├── models/                # ML models
├── logs/                  # Application logs
└── uploads/               # File uploads
```

### Available Scripts

```bash
# Development server with hot reload
./dev.sh

# Production server
./run.sh

# Run tests with coverage
./test.sh

# Check dependencies
python check_requirements.py
```

### API Endpoints

#### Disease Detection
- `POST /api/v1/disease-detection` - Detect plant diseases from images
- `GET /api/v1/diseases/supported` - List supported diseases and crops

#### Weather & Irrigation
- `POST /api/v1/weather/current` - Get weather forecast
- `POST /api/v1/irrigation/schedule` - Generate irrigation schedule

#### Price Management
- `GET /api/v1/prices/fertilizers` - Get fertilizer prices
- `GET /api/v1/prices/crops` - Get crop market prices
- `POST /api/v1/prices/compare` - Compare prices across locations

#### Government Schemes
- `GET /api/v1/schemes` - List government schemes
- `POST /api/v1/schemes/eligibility` - Check scheme eligibility
- `POST /api/v1/schemes/{scheme_id}/apply` - Apply for scheme

#### NLP & Voice
- `POST /api/v1/nlp/query` - Process text queries
- `POST /api/v1/nlp/voice-query` - Process voice queries
- `POST /api/v1/nlp/translate` - Translate text

#### Health Monitoring
- `GET /api/v1/health` - Basic health check
- `GET /api/v1/health/detailed` - Detailed system metrics

## Configuration

### Environment Variables

Key configuration options in `.env`:

```bash
# Server
HOST=0.0.0.0
PORT=8000
DEBUG=true

# AI Models
GEMINI_API_KEY=your-key-here
OLLAMA_BASE_URL=http://localhost:11434

# Vector Database
VECTOR_DB_TYPE=faiss  # or pinecone
FAISS_INDEX_PATH=./data/faiss_index

# External APIs
OPENWEATHER_API_KEY=your-key-here

# File Storage
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=10485760  # 10MB
```

### Supported Languages

Text and Voice Support:
- English (en)
- Hindi (hi) 
- Bengali (bn)
- Telugu (te)
- Marathi (mr)
- Tamil (ta)
- Gujarati (gu)
- Kannada (kn)
- Malayalam (ml)
- Punjabi (pa)
- Odia (or)
- Assamese (as)

## Testing

```bash
# Run all tests
./test.sh

# Run specific test file
python -m pytest tests/test_disease_detection.py -v

# Run with coverage
python -m pytest --cov=. --cov-report=html
```

## Deployment

### Docker (Recommended)

```bash
# Build image
docker build -t krishisevak-backend .

# Run container
docker run -p 8000:8000 --env-file .env krishisevak-backend
```

### Manual Deployment

```bash
# Install dependencies
pip install -r requirements.txt

# Run with gunicorn
gunicorn main:app -w 4 -k uvicorn.workers.UvicornWorker --bind 0.0.0.0:8000
```

## Monitoring

### Health Checks

- `/api/v1/health` - Basic health status
- `/api/v1/health/detailed` - System metrics
- `/api/v1/health/ready` - Kubernetes readiness probe
- `/api/v1/health/live` - Kubernetes liveness probe

### Logs

Structured JSON logs are written to:
- Console: Real-time logs
- File: `logs/krishisevak.log` (rotated)

### Metrics

Prometheus metrics available at `/metrics` (when enabled)

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature-name`
3. Make changes and test: `./test.sh`
4. Commit changes: `git commit -m "Add feature"`
5. Push branch: `git push origin feature-name`
6. Create Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- Create GitHub issue
- Check documentation at `/docs`
- Review API examples in `/tests`
