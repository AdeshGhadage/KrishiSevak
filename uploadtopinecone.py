import os
import io
import argparse
import tempfile
import requests
import pdfplumber
import tiktoken
from google import genai  # Gemini embeddings SDK
from google.genai.types import EmbedContentConfig
from pinecone import Pinecone, ServerlessSpec

# 1. PDF download
def download_pdf(url: str) -> str:
    r = requests.get(url, stream=True, timeout=60)
    r.raise_for_status()
    fd, path = tempfile.mkstemp(suffix=".pdf")
    with os.fdopen(fd, "wb") as f:
        for chunk in r.iter_content(chunk_size=8192):
            if chunk:
                f.write(chunk)
    return path

# 2. Extract text + tables
def extract_pdf_text(path: str) -> str:
    text = ""
    with pdfplumber.open(path) as pdf:
        for i, p in enumerate(pdf.pages, start=1):
            page_text = p.extract_text() or ""
            text += f"\n\n--- Page {i} ---\n\n{page_text}"
    return text

# 3. Chunk text using token-based splitting
def chunk_text(text: str, model_name: str, max_tokens=800, overlap=80):
    enc = tiktoken.encoding_for_model(model_name)
    tokens = enc.encode(text)
    chunks = []
    i = 0
    while i < len(tokens):
        j = min(i + max_tokens, len(tokens))
        chunk = enc.decode(tokens[i:j])
        chunks.append(chunk)
        if j == len(tokens): break
        i = j - overlap
    return chunks

def main(args):
    # Setup clients
    genai_client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))
    pc = Pinecone(api_key=os.getenv("PINECONE_API_KEY"))

    index_name = args.index_name
    namespace = args.namespace

    # Create Pinecone index if not exists
    dims = 3072  # For gemini-embedding-001 or experimental
    if index_name not in [idx["name"] for idx in pc.list_indexes()]:
        pc.create_index(
            name=index_name,
            dimension=dims,
            metric="cosine",
            spec=ServerlessSpec(cloud="aws", region="us-east-1")
        )
    index = pc.Index(index_name)

    for pdf_input in args.pdfs:
        print(f"\nProcessing: {pdf_input}")
        path = download_pdf(pdf_input) if pdf_input.startswith(("http://", "https://")) else pdf_input

        content = extract_pdf_text(path)
        chunks = chunk_text(content, model_name="gemini-embedding-001")

        # Generate embeddings via Gemini
        response = genai_client.models.embed_content(
            model=args.model,
            contents=chunks,
            config=EmbedContentConfig(task_type="RETRIEVAL_DOCUMENT")
        )
        embeddings = [e.values for e in response.embeddings]

        vectors = []
        for i, (chunk, emb) in enumerate(zip(chunks, embeddings)):
            vectors.append({
                "id": f"{os.path.basename(path)}-c{i}",
                "metadata": {"source": os.path.basename(path), "chunk_index": i},
                "values": emb
            })
        index.upsert(vectors=vectors, namespace=namespace)
        print(f"Upserted {len(vectors)} chunks.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--pdfs", nargs="+", required=True,
                        help="PDF file paths or URLs")
    parser.add_argument("--index-name", required=True,
                        help="Name of Pinecone index")
    parser.add_argument("--namespace", default="default",
                        help="Pinecone namespace")
    parser.add_argument("--model", default="gemini-embedding-001",
                        choices=["gemini-embedding-001", "gemini-embedding-exp-03-07"],
                        help="Gemini embedding model to use")
    args = parser.parse_args()
    main(args)