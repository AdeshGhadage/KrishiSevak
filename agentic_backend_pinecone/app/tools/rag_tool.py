from __future__ import annotations
from langchain_core.tools import tool
from app.services.rag import load_retriever, ensure_index_exists

@tool("rag_search", return_direct=False)
def rag_search(query: str, k: int = 4) -> dict:
    """Search the KB in Pinecone and return top-k passages."""
    ensure_index_exists()
    retriever = load_retriever(k=k)
    docs = retriever.invoke(query)
    return {"matches": [{"text": d.page_content[:1200], "metadata": d.metadata} for d in docs]}
