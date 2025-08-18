from __future__ import annotations
import subprocess, tempfile, os
from app.config import settings
import httpx

def _voice_for_language(language: str | None) -> str:
    # Map ISO-ish codes to espeak voices; fallback to configured voice
    if not language:
        return settings.TTS_VOICE
    lang = language.lower()
    mapping = {
        "en": "en-us",
        "en-in": "en-in",
        "hi": "hi",
        "bn": "bn",
        "mr": "mr",
        "ta": "ta",
        "te": "te",
        "kn": "kn",
        "ml": "ml",
        "gu": "gu",
        "pa": "pa",
        "ur": "ur",
    }
    return mapping.get(lang, settings.TTS_VOICE)

def _synthesize_espeak(text: str, language: str | None = None) -> str:
    fd, out_path = tempfile.mkstemp(prefix="tts_", suffix=".wav")
    voice = _voice_for_language(language)
    cmd = [settings.ESPEAK_BIN, "-v", voice, "-s", str(settings.TTS_SPEED_WPM), "-w", out_path, text]
    subprocess.run(cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return out_path

def _synthesize_elevenlabs(text: str, language: str | None = None) -> str:
    api_key = settings.ELEVENLABS_API_KEY
    if not api_key:
        raise RuntimeError("ELEVENLABS_API_KEY not set")
    voice_id = settings.ELEVENLABS_VOICE_ID
    if not voice_id or not voice_id.strip():
        raise RuntimeError("ELEVENLABS_VOICE_ID not set")
    model_id = settings.ELEVENLABS_MODEL_ID
    url = f"https://api.elevenlabs.io/v1/text-to-speech/{voice_id}"
    headers = {
        "xi-api-key": api_key,
        "Accept": "audio/mpeg",
        "Content-Type": "application/json",
    }
    json = {
        "text": text,
        "model_id": model_id,
        "voice_settings": {"stability": 0.5, "similarity_boost": 0.75},
    }
    fd, out_path = tempfile.mkstemp(prefix="tts_", suffix=".mp3")
    with httpx.Client(timeout=60) as client:
        r = client.post(url, headers=headers, json=json)
        r.raise_for_status()
        with open(out_path, "wb") as f:
            f.write(r.content)
    return out_path

def synthesize_to_wav(text: str, language: str | None = None) -> str:
    provider = (settings.TTS_PROVIDER or "espeak").lower()
    if provider == "elevenlabs":
        return _synthesize_elevenlabs(text, language)
    return _synthesize_espeak(text, language)
