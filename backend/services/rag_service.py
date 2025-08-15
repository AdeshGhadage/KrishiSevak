"""
RAG (Retrieval-Augmented Generation) Service for KrishiSevak
Provides context-aware responses using FAISS/Pinecone vector database
"""

import asyncio
import numpy as np
from typing import List, Dict, Any, Optional
from datetime import datetime
import json
import os
from pathlib import Path

try:
    import faiss
    FAISS_AVAILABLE = True
except ImportError:
    FAISS_AVAILABLE = False

try:
    import pinecone
    PINECONE_AVAILABLE = True
except ImportError:
    PINECONE_AVAILABLE = False

from sentence_transformers import SentenceTransformer
from ..config import settings
from ..utils.logger import get_logger

logger = get_logger(__name__)

class RAGService:
    """
    RAG service for knowledge retrieval and augmented generation
    """
    
    def __init__(self):
        self.vector_db = None
        self.embeddings_model = None
        self.knowledge_base = {}
        self.index_path = settings.FAISS_INDEX_PATH
        self.is_initialized = False
    
    async def initialize(self):
        """Initialize RAG service with vector database and embeddings model"""
        
        try:
            logger.info("Initializing RAG service...")
            
            # Initialize embeddings model
            await self._initialize_embeddings_model()
            
            # Initialize vector database
            if settings.VECTOR_DB_TYPE.lower() == "pinecone" and PINECONE_AVAILABLE:
                await self._initialize_pinecone()
            elif settings.VECTOR_DB_TYPE.lower() == "faiss" and FAISS_AVAILABLE:
                await self._initialize_faiss()
            else:
                logger.warning("No vector database available, using simple similarity search")
                await self._initialize_simple_search()
            
            # Load knowledge base
            await self._load_knowledge_base()
            
            self.is_initialized = True
            logger.info("RAG service initialized successfully")
            
        except Exception as e:
            logger.error(f"Failed to initialize RAG service: {e}", exc_info=True)
            # Initialize with simple fallback
            await self._initialize_fallback()
    
    async def _initialize_embeddings_model(self):
        """Initialize sentence transformer model for embeddings"""
        
        try:
            # Use a lightweight model suitable for agricultural domain
            model_name = "all-MiniLM-L6-v2"  # Good balance of speed and performance
            
            logger.info(f"Loading embeddings model: {model_name}")
            self.embeddings_model = SentenceTransformer(model_name)
            logger.info("Embeddings model loaded successfully")
            
        except Exception as e:
            logger.error(f"Failed to load embeddings model: {e}")
            # Create a mock embeddings model
            self.embeddings_model = MockEmbeddingsModel()
    
    async def _initialize_faiss(self):
        """Initialize FAISS vector database"""
        
        try:
            if not FAISS_AVAILABLE:
                raise ImportError("FAISS not available")
            
            embedding_dim = 384  # Dimension for all-MiniLM-L6-v2
            
            # Create or load FAISS index
            index_file = f"{self.index_path}/faiss.index"
            metadata_file = f"{self.index_path}/metadata.json"
            
            os.makedirs(os.path.dirname(index_file), exist_ok=True)
            
            if os.path.exists(index_file) and os.path.exists(metadata_file):
                # Load existing index
                logger.info("Loading existing FAISS index...")
                self.vector_db = faiss.read_index(index_file)
                
                with open(metadata_file, 'r', encoding='utf-8') as f:
                    self.knowledge_base = json.load(f)
                
                logger.info(f"Loaded FAISS index with {self.vector_db.ntotal} vectors")
            else:
                # Create new index
                logger.info("Creating new FAISS index...")
                self.vector_db = faiss.IndexFlatIP(embedding_dim)  # Inner product for cosine similarity
                self.knowledge_base = {}
                
                # Build initial knowledge base
                await self._build_initial_knowledge_base()
            
        except Exception as e:
            logger.error(f"Failed to initialize FAISS: {e}")
            await self._initialize_simple_search()
    
    async def _initialize_pinecone(self):
        """Initialize Pinecone vector database"""
        
        try:
            if not PINECONE_AVAILABLE:
                raise ImportError("Pinecone not available")
            
            if not settings.PINECONE_API_KEY:
                raise ValueError("Pinecone API key not configured")
            
            # Initialize Pinecone
            pinecone.init(
                api_key=settings.PINECONE_API_KEY,
                environment=settings.PINECONE_ENVIRONMENT
            )
            
            # Create or connect to index
            index_name = "krishisevak-knowledge"
            
            if index_name not in pinecone.list_indexes():
                pinecone.create_index(
                    index_name,
                    dimension=384,  # Dimension for all-MiniLM-L6-v2
                    metric="cosine"
                )
            
            self.vector_db = pinecone.Index(index_name)
            logger.info("Pinecone index initialized successfully")
            
        except Exception as e:
            logger.error(f"Failed to initialize Pinecone: {e}")
            await self._initialize_faiss()
    
    async def _initialize_simple_search(self):
        """Initialize simple text-based search as fallback"""
        
        logger.info("Initializing simple search fallback...")
        self.vector_db = "simple_search"
        await self._load_simple_knowledge_base()
    
    async def _initialize_fallback(self):
        """Initialize minimal fallback service"""
        
        logger.info("Initializing RAG service with minimal fallback...")
        self.vector_db = "fallback"
        self.embeddings_model = MockEmbeddingsModel()
        self.knowledge_base = self._get_basic_knowledge_base()
        self.is_initialized = True
    
    async def _load_knowledge_base(self):
        """Load agricultural knowledge base from various sources"""
        
        try:
            # Load from local JSON files
            knowledge_dir = Path("data/knowledge_base")
            if knowledge_dir.exists():
                for json_file in knowledge_dir.glob("*.json"):
                    with open(json_file, 'r', encoding='utf-8') as f:
                        data = json.load(f)
                        self.knowledge_base.update(data)
            
            # TODO: Add web scraping for government websites
            # TODO: Add periodic updates from agricultural databases
            
            logger.info(f"Knowledge base loaded with {len(self.knowledge_base)} entries")
            
        except Exception as e:
            logger.error(f"Failed to load knowledge base: {e}")
            self.knowledge_base = self._get_basic_knowledge_base()
    
    async def _load_simple_knowledge_base(self):
        """Load basic knowledge for simple search"""
        
        self.knowledge_base = self._get_basic_knowledge_base()
    
    def _get_basic_knowledge_base(self) -> Dict[str, Any]:
        """Get basic agricultural knowledge for fallback"""
        
        return {
            "crops": {
                "wheat": {
                    "planting_season": "October-December",
                    "harvesting_season": "March-May",
                    "water_requirement": "450-650mm",
                    "common_diseases": ["rust", "smut", "blight"],
                    "fertilizer": "NPK 120:60:40 kg/ha"
                },
                "rice": {
                    "planting_season": "June-July",
                    "harvesting_season": "October-November",
                    "water_requirement": "1200-1500mm",
                    "common_diseases": ["blast", "bacterial_blight", "brown_spot"],
                    "fertilizer": "NPK 100:50:50 kg/ha"
                }
            },
            "diseases": {
                "bacterial_blight": {
                    "crops_affected": ["rice"],
                    "symptoms": "Water-soaked lesions on leaves",
                    "treatment": "Copper-based fungicides",
                    "prevention": "Use resistant varieties, proper field sanitation"
                },
                "rust": {
                    "crops_affected": ["wheat"],
                    "symptoms": "Orange-red pustules on leaves",
                    "treatment": "Propiconazole spray",
                    "prevention": "Early sowing, resistant varieties"
                }
            },
            "fertilizers": {
                "urea": {
                    "composition": "46% N",
                    "application": "Split doses during crop growth",
                    "crops": ["wheat", "rice", "corn"]
                },
                "dap": {
                    "composition": "18:46:0 NPK",
                    "application": "Basal application before sowing",
                    "crops": ["wheat", "rice", "pulses"]
                }
            }
        }
    
    async def _build_initial_knowledge_base(self):
        """Build initial vector embeddings for knowledge base"""
        
        try:
            if not self.embeddings_model or not hasattr(self.vector_db, 'add'):
                return
            
            # Prepare documents for embedding
            documents = []
            metadata = []
            
            for category, items in self.knowledge_base.items():
                for item_name, item_data in items.items():
                    doc_text = f"{category}: {item_name}. " + " ".join([
                        f"{k}: {v}" for k, v in item_data.items() if isinstance(v, str)
                    ])
                    
                    documents.append(doc_text)
                    metadata.append({
                        "category": category,
                        "item": item_name,
                        "data": item_data
                    })
            
            if documents:
                # Generate embeddings
                embeddings = self.embeddings_model.encode(documents)
                
                # Add to vector database
                if hasattr(self.vector_db, 'add'):
                    self.vector_db.add(embeddings)
                
                # Save metadata
                metadata_file = f"{self.index_path}/metadata.json"
                with open(metadata_file, 'w', encoding='utf-8') as f:
                    json.dump({str(i): meta for i, meta in enumerate(metadata)}, f, indent=2)
                
                logger.info(f"Built vector index with {len(documents)} documents")
            
        except Exception as e:
            logger.error(f"Failed to build initial knowledge base: {e}")
    
    async def search(self, query: str, top_k: int = 5) -> List[Dict[str, Any]]:
        """
        Search knowledge base for relevant information
        
        Args:
            query: Search query
            top_k: Number of top results to return
            
        Returns:
            List of relevant documents with scores
        """
        
        try:
            if not self.is_initialized:
                await self.initialize()
            
            if self.vector_db == "fallback":
                return await self._simple_keyword_search(query, top_k)
            
            if self.vector_db == "simple_search":
                return await self._simple_text_search(query, top_k)
            
            # Vector-based search
            if hasattr(self.embeddings_model, 'encode'):
                query_embedding = self.embeddings_model.encode([query])
                
                if hasattr(self.vector_db, 'search'):
                    # FAISS search
                    scores, indices = self.vector_db.search(query_embedding, top_k)
                    
                    results = []
                    for score, idx in zip(scores[0], indices[0]):
                        if str(idx) in self.knowledge_base:
                            results.append({
                                "score": float(score),
                                "content": self.knowledge_base[str(idx)],
                                "index": int(idx)
                            })
                    
                    return results
            
            # Fallback to simple search
            return await self._simple_keyword_search(query, top_k)
            
        except Exception as e:
            logger.error(f"Search failed: {e}")
            return await self._simple_keyword_search(query, top_k)
    
    async def _simple_text_search(self, query: str, top_k: int) -> List[Dict[str, Any]]:
        """Simple text-based search"""
        
        query_terms = query.lower().split()
        results = []
        
        for category, items in self.knowledge_base.items():
            for item_name, item_data in items.items():
                score = 0
                content_text = f"{category} {item_name} {str(item_data)}".lower()
                
                for term in query_terms:
                    if term in content_text:
                        score += 1
                
                if score > 0:
                    results.append({
                        "score": score / len(query_terms),
                        "content": {
                            "category": category,
                            "item": item_name,
                            "data": item_data
                        },
                        "type": "text_search"
                    })
        
        # Sort by score and return top_k
        results.sort(key=lambda x: x["score"], reverse=True)
        return results[:top_k]
    
    async def _simple_keyword_search(self, query: str, top_k: int) -> List[Dict[str, Any]]:
        """Fallback keyword-based search"""
        
        query_lower = query.lower()
        results = []
        
        # Define keyword mappings
        crop_keywords = ["wheat", "rice", "corn", "tomato", "potato"]
        disease_keywords = ["disease", "infection", "blight", "rust", "spot"]
        fertilizer_keywords = ["fertilizer", "urea", "dap", "nutrient"]
        
        if any(keyword in query_lower for keyword in crop_keywords):
            results.append({
                "score": 0.9,
                "content": "For crop-specific information, please specify the crop name. Common crops include wheat, rice, corn, and vegetables.",
                "type": "crop_info"
            })
        
        if any(keyword in query_lower for keyword in disease_keywords):
            results.append({
                "score": 0.8,
                "content": "For disease identification, please upload an image of the affected plant. Common diseases include bacterial blight, rust, and leaf spots.",
                "type": "disease_info"
            })
        
        if any(keyword in query_lower for keyword in fertilizer_keywords):
            results.append({
                "score": 0.7,
                "content": "For fertilizer recommendations, consider soil testing and crop requirements. Common fertilizers include Urea (Nitrogen) and DAP (Phosphorus).",
                "type": "fertilizer_info"
            })
        
        return results[:top_k]
    
    async def add_document(self, content: str, metadata: Dict[str, Any]):
        """Add new document to knowledge base"""
        
        try:
            if not self.is_initialized:
                await self.initialize()
            
            # Generate embedding
            if hasattr(self.embeddings_model, 'encode'):
                embedding = self.embeddings_model.encode([content])
                
                # Add to vector database
                if hasattr(self.vector_db, 'add'):
                    self.vector_db.add(embedding)
                
                # Update metadata
                doc_id = len(self.knowledge_base)
                self.knowledge_base[str(doc_id)] = {
                    "content": content,
                    "metadata": metadata,
                    "added_at": datetime.utcnow().isoformat()
                }
                
                logger.info(f"Added document to knowledge base: {doc_id}")
                return doc_id
            
        except Exception as e:
            logger.error(f"Failed to add document: {e}")
            return None
    
    async def update_knowledge_base(self):
        """Update knowledge base with latest information"""
        
        try:
            # TODO: Implement periodic updates from:
            # - Government websites
            # - Agricultural research databases
            # - Market price APIs
            # - Weather APIs
            
            logger.info("Knowledge base update completed")
            
        except Exception as e:
            logger.error(f"Knowledge base update failed: {e}")
    
    async def cleanup(self):
        """Clean up resources"""
        
        try:
            # Save current state
            if hasattr(self.vector_db, 'write_index') and self.index_path:
                index_file = f"{self.index_path}/faiss.index"
                self.vector_db.write_index(index_file)
            
            logger.info("RAG service cleanup completed")
            
        except Exception as e:
            logger.error(f"RAG service cleanup failed: {e}")

class MockEmbeddingsModel:
    """Mock embeddings model for fallback"""
    
    def encode(self, texts):
        """Generate random embeddings for testing"""
        return np.random.rand(len(texts), 384)
