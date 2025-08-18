# Agentic AI Backend — Pinecone Edition

This backend exposes a single autonomous agent (LangChain) that **decides** when to use tools:
- `get_weather` (Open-Meteo)
- `rag_search` (**Pinecone** retriever over your KB)
- `classify_crop_disease` (ViT, Transformers)

## Quickstart

```bash
python -m venv .venv && source .venv/bin/activate
pip install -U pip
pip install -r requirements.txt

cp .env.example .env
# Set PINECONE_API_KEY and GEMINI_API_KEY (or switch to Ollama)

# Build Pinecone index from URLs
python -m app.ingest --urls https://example.gov/page1 https://example.gov/page2

# Run
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

> **Note:** New Pinecone SDK package name is `pinecone`. If you have old `pinecone-client` installed, uninstall it first:
> `pip uninstall -y pinecone-client && pip install pinecone`
