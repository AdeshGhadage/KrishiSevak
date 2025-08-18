from __future__ import annotations
import subprocess, tempfile
from app.config import settings

def synthesize_to_wav(text: str) -> str:
    fd, out_path = tempfile.mkstemp(prefix="tts_", suffix=".wav")
    cmd = [settings.ESPEAK_BIN, "-v", settings.TTS_VOICE, "-s", str(settings.TTS_SPEED_WPM), "-w", out_path, text]
    subprocess.run(cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return out_path
