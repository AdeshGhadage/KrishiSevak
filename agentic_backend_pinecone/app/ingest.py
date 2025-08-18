from __future__ import annotations
import argparse
from langchain_community.document_loaders import AsyncHtmlLoader, PlaywrightURLLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from app.services.rag import _embeddings, ensure_index_exists
from langchain_pinecone import PineconeVectorStore
from app.config import settings

def ingest(urls: list[str], use_playwright: bool = False):
    # Load docs
    if use_playwright:
        loader = PlaywrightURLLoader(urls=urls)
        docs = loader.load()
    else:
        loader = AsyncHtmlLoader(urls)
        docs = loader.load()

    # Split
    splitter = RecursiveCharacterTextSplitter(chunk_size=1200, chunk_overlap=150)
    chunks = splitter.split_documents(docs)

    # Ensure index exists and upsert
    ensure_index_exists()
    emb = _embeddings()
    PineconeVectorStore.from_documents(
        documents=chunks,
        embedding=emb,
        index_name=settings.PINECONE_INDEX,
        namespace=settings.PINECONE_NAMESPACE,
    )
    print(f"Ingested {len(chunks)} chunks into Pinecone index '{settings.PINECONE_INDEX}' namespace '{settings.PINECONE_NAMESPACE}'.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--urls", nargs="+", required=True)
    parser.add_argument("--playwright", action="store_true")
    args = parser.parse_args()
    ingest(args.urls, use_playwright=args.playwright)
