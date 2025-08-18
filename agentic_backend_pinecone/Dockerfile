FROM python:3.11-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg espeak-ng git build-essential cmake curl && \
    rm -rf /var/lib/apt/lists/*

# Optional: whisper.cpp build + small model
RUN git clone --depth=1 https://github.com/ggml-org/whisper.cpp /opt/whisper.cpp \
 && cmake -B /opt/whisper.cpp/build /opt/whisper.cpp \
 && cmake --build /opt/whisper.cpp/build -j || true \
 && /opt/whisper.cpp/models/download-ggml-model.sh base.en || true

WORKDIR /app
COPY requirements.txt .
RUN pip install -U pip && pip install -r requirements.txt

COPY . .

EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
