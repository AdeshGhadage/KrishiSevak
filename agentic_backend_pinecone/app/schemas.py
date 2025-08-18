from typing import Optional, List
from pydantic import BaseModel, Field

class ChatRequest(BaseModel):
    session_id: str = Field(default="default")
    message: str
    stream: bool = Field(default=False)
    images_base64: Optional[List[str]] = None

class ChatResponse(BaseModel):
    text: str
    tool_calls: Optional[list] = None
    sources: Optional[list] = None

class ImageClassifyResponse(BaseModel):
    label: str
    score: float
    top_k: list
