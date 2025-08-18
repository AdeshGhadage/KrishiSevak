from __future__ import annotations
import json, io, base64, threading, os
from typing import List, Tuple
from PIL import Image
import torch
from transformers import AutoImageProcessor, AutoModelForImageClassification
from langchain_core.tools import tool
from app.config import settings

_model_lock = threading.Lock()
_model = None
_processor = None
_labels = None

def _load_model():
    global _model, _processor, _labels
    if _model is None:
        with _model_lock:
            if _model is None:
                model_dir = settings.VIT_MODEL_DIR
                _processor = AutoImageProcessor.from_pretrained(model_dir)
                _model = AutoModelForImageClassification.from_pretrained(model_dir)
                labels_path = settings.VIT_LABELS_JSON
                if os.path.isfile(labels_path):
                    with open(labels_path, "r", encoding="utf-8") as f:
                        _labels = json.load(f)
                else:
                    _labels = getattr(_model.config, "id2label", {})
    return _model, _processor, _labels

def _predict_probs(image: Image.Image, top_k: int = 3):
    model, processor, labels = _load_model()
    inputs = processor(images=image, return_tensors="pt")
    with torch.no_grad():
        logits = model(**inputs).logits
        probs = torch.softmax(logits, dim=-1)[0]
        topk = torch.topk(probs, k=top_k)
    results = []
    for idx, score in zip(topk.indices.tolist(), topk.values.tolist()):
        label = labels.get(idx, str(idx)) if isinstance(labels, dict) else str(idx)
        results.append((label, float(score)))
    return results

@tool("classify_crop_disease_direct", return_direct=False)
def classify_crop_disease(image_base64: str) -> dict:
    """Classify a crop disease from a base64-encoded image and return top-1 and top-k results."""
    try:
        img_bytes = base64.b64decode(image_base64)
        image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
        topk = _predict_probs(image, top_k=3)
        return {"label": topk[0][0], "score": topk[0][1],
                "top_k": [{"label": l, "score": s} for l, s in topk]}
    except Exception as e:
        return {"error": str(e)}
