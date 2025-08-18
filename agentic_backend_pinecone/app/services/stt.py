from __future__ import annotations
import subprocess, tempfile, os
from typing import Optional
from app.config import settings
import httpx

def _convert_to_wav(src_path: str) -> str:
    ffmpeg = settings.FFMPEG_BIN
    wav_path = src_path + ".wav"
    cmd = [ffmpeg, "-y", "-i", src_path, "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le", wav_path]
    subprocess.run(cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return wav_path

def _parse_transcript(stdout: str) -> str:
    texts = []
    for ln in stdout.splitlines():
        s = ln.strip()
        if not s: continue
        if s.startswith("[") and "]" in s:
            try: texts.append(s.split("]", 1)[1].strip()); continue
            except Exception: pass
        if not any(tok in s for tok in ("whisper", "ms", "->")) and len(s.split()) >= 1:
            texts.append(s)
    if texts: return " ".join(texts).strip()
    non_empty = [ln.strip() for ln in stdout.splitlines() if ln.strip()]
    return non_empty[-1] if non_empty else ""

def _transcribe_local(audio_path: str, language: Optional[str]) -> str:
    wav_path = audio_path if audio_path.lower().endswith(".wav") else _convert_to_wav(audio_path)
    whisper_bin = settings.WHISPER_CPP_BIN
    model_path = settings.WHISPER_MODEL_PATH
    cmd = [whisper_bin, "-m", model_path, "-f", wav_path]
    if language: cmd += ["-l", language]
    out = subprocess.run(cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return _parse_transcript(out.stdout).strip()

def _transcribe_openai(audio_path: str, language: Optional[str]) -> str:
    api_key = settings.OPENAI_API_KEY
    if not api_key:
        raise RuntimeError("OPENAI_API_KEY not set")
    wav_path = audio_path if audio_path.lower().endswith(".wav") else _convert_to_wav(audio_path)
    base = settings.OPENAI_BASE_URL.rstrip("/")
    url = f"{base}/v1/audio/transcriptions"
    headers = {"Authorization": f"Bearer {api_key}"}
    files = {
        "file": (os.path.basename(wav_path), open(wav_path, "rb"), "audio/wav"),
        "model": (None, settings.OPENAI_WHISPER_MODEL),
    }
    if language:
        files["language"] = (None, language)
    with httpx.Client(timeout=60) as client:
        r = client.post(url, headers=headers, files=files)
        r.raise_for_status()
        data = r.json()
        return (data.get("text") or "").strip()

def transcribe(audio_path: str, language: Optional[str] = None) -> str:
    provider = (settings.STT_PROVIDER or "local").lower()
    if provider == "openai":
        return _transcribe_openai(audio_path, language)
    return _transcribe_local(audio_path, language)
