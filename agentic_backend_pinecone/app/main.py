from __future__ import annotations
import base64, os, tempfile
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from starlette.responses import JSONResponse, FileResponse
from sse_starlette.sse import EventSourceResponse

from app.schemas import ChatRequest, ChatResponse, ImageClassifyResponse
from app.agents.independent_agent import IndependentAgent
from app.services.stt import transcribe
from app.services.tts import synthesize_to_wav
from app.tools.vit import classify_crop_disease as classify_crop_disease_direct

app = FastAPI(title="Agentic AI Backend (Pinecone)", version="0.3.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

_agent: IndependentAgent | None = None

def get_agent() -> IndependentAgent:
    global _agent
    if _agent is None:
        _agent = IndependentAgent()
    return _agent

@app.get("/v1/health")
def health():
    return {"status":"ok"}

@app.post("/v1/chat")
async def chat(req: ChatRequest):
    if req.stream:
        async def event_gen():
            inputs = {"input": req.message, "attachments_overview": f"{len(req.images_base64)} image(s)" if req.images_base64 else "none"}
            for token in get_agent().stream(session_id=req.session_id, inputs=inputs, images_base64=req.images_base64):
                yield {"event": "token", "data": token}
            yield {"event": "done", "data": ""}
        return EventSourceResponse(event_gen())
    else:
        # Prepend one-time user context to first message in a session
        user_text = req.message
        if req.user_context:
            user_text = f"[User context]\n{req.user_context}\n[/User context]\n\n{req.message}"
        result = get_agent().respond(
            session_id=req.session_id,
            user_text=user_text,
            images_base64=req.images_base64,
            stream=False
        )
        return ChatResponse(text=result["text"], tool_calls=result.get("intermediate_steps"))

@app.post("/v1/voice")
async def voice_to_chat(session_id: str = Form("default"),
                        tts: bool = Form(True),
                        language: str | None = Form(None),
                        audio: UploadFile = File(...)):
    suffix = os.path.splitext(audio.filename or "audio.wav")[1]
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as f:
        raw_path = f.name
        f.write(await audio.read())
    try:
        text = transcribe(raw_path, language=language)
        if not text.strip():
            if tts:
                out_path = synthesize_to_wav("Sorry, I couldn't hear anything. Please try again.", language=language)
                ext = os.path.splitext(out_path)[1].lower()
                media = "audio/mpeg" if ext == ".mp3" else "audio/wav"
                fname = "reply.mp3" if ext == ".mp3" else "reply.wav"
                return FileResponse(out_path, media_type=media, filename=fname)
            return JSONResponse({"error": "empty_transcript", "transcript": text}, status_code=400)

        result = get_agent().respond(session_id=session_id, user_text=text, stream=False)
        reply = result["text"]
        if tts:
            out_path = synthesize_to_wav(reply, language=language)
            # Decide media type by extension
            ext = os.path.splitext(out_path)[1].lower()
            media = "audio/mpeg" if ext == ".mp3" else "audio/wav"
            fname = "reply.mp3" if ext == ".mp3" else "reply.wav"
            return FileResponse(out_path, media_type=media, filename=fname)
        else:
            return JSONResponse({"transcript": text, "reply": reply, "tool_calls": result.get("intermediate_steps")})
    finally:
        try: os.remove(raw_path)
        except Exception: pass

@app.post("/v1/image/classify", response_model=ImageClassifyResponse)
async def image_classify(file: UploadFile = File(...)):
    b = await file.read()
    b64 = base64.b64encode(b).decode("utf-8")
    res = classify_crop_disease_direct.invoke({"image_base64": b64})
    if "error" in res:
        raise HTTPException(status_code=400, detail=res["error"])
    return ImageClassifyResponse(**res)

@app.post("/v1/tts")
async def tts(text: str = Form(...), language: str | None = Form(None)):
    out_path = synthesize_to_wav(text, language=language)
    ext = os.path.splitext(out_path)[1].lower()
    media = "audio/mpeg" if ext == ".mp3" else "audio/wav"
    fname = "speech.mp3" if ext == ".mp3" else "speech.wav"
    return FileResponse(out_path, media_type=media, filename=fname)
