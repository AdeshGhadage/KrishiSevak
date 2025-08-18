from __future__ import annotations
from typing import Optional
import re
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_core.embeddings import Embeddings
from langchain_pinecone import PineconeVectorStore

from pinecone import Pinecone, ServerlessSpec, CloudProvider, AwsRegion, GcpRegion, AzureRegion, Metric

from app.config import settings

def _embeddings() -> Embeddings:
    # Multilingual E5-base (dim=768) by default
    return HuggingFaceEmbeddings(model_name=settings.EMBEDDING_MODEL)

def _pc() -> Pinecone:
    if not settings.PINECONE_API_KEY:
        raise RuntimeError("PINECONE_API_KEY not set")
    return Pinecone(api_key=settings.PINECONE_API_KEY)

def _normalized_index_name(raw_name: str) -> str:
    name = (raw_name or "default-index").lower()
    name = re.sub(r"[^a-z0-9-]", "-", name)
    name = re.sub(r"-+", "-", name).strip("-")
    return name or "default-index"

def _cloud_provider(s: str):
    s = (s or "aws").lower()
    if s == "aws":
        return CloudProvider.AWS
    if s == "gcp":
        return CloudProvider.GCP
    if s == "azure":
        return CloudProvider.AZURE
    return CloudProvider.AWS

def _region_enum(cloud: str, region: str):
    # best-effort conversion to Enum; fall back to raw string if not matched
    key = (region or "").upper().replace("-", "_")
    try:
        if cloud == "aws":
            return getattr(AwsRegion, key)
        if cloud == "gcp":
            return getattr(GcpRegion, key)
        if cloud == "azure":
            return getattr(AzureRegion, key)
    except Exception:
        pass
    return region  # let SDK handle string

def ensure_index_exists() -> None:
    pc = _pc()
    idx = _normalized_index_name(settings.PINECONE_INDEX)
    names = pc.list_indexes().names()
    if idx not in names:
        emb = _embeddings()
        dim = len(emb.embed_query("dimension probe"))
        cloud = settings.PINECONE_CLOUD.lower()
        region = settings.PINECONE_REGION
        pc.create_index(
            name=idx,
            dimension=dim,
            metric=Metric.COSINE,
            spec=ServerlessSpec(
                cloud=_cloud_provider(cloud),
                region=_region_enum(cloud, region)
            )
        )

def load_retriever(k: int = 4):
    emb = _embeddings()
    # Assumes ensure_index_exists has been called
    vs = PineconeVectorStore(
        index_name=_normalized_index_name(settings.PINECONE_INDEX),
        embedding=emb,
        namespace=settings.PINECONE_NAMESPACE,
        pinecone_api_key=settings.PINECONE_API_KEY,
    )
    return vs.as_retriever(search_kwargs={"k": k})
