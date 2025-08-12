# main.py
from fastapi import FastAPI, File, UploadFile, Form, HTTPException
from pydantic import BaseModel
from typing import Optional

from dependencies import get_weather_data, detect_disease_with_vit, setup_langchain_agent
# from config import llm # Uncomment if you've set up your LLM in config

app = FastAPI(
    title="Agriculture AI Assistant Backend",
    description="Backend for the one-capital agriculture project, orchestrating data for LangChain."
)

# Initialize LangChain agent (this can be done on startup)
langchain_agent = setup_langchain_agent()

# Pydantic models for request bodies
class FarmerData(BaseModel):
    lat: float
    lon: float
    crop_type: str
    stage_of_crop: Optional[str] = None
    problem_description: Optional[str] = None

@app.get("/")
def read_root():
    return {"message": "Welcome to the Agriculture AI Assistant Backend!"}

@app.post("/analyze-data")
async def analyze_farmer_data(data: FarmerData):
    """
    Endpoint to receive general farmer information and provide a LangChain-powered response.
    """
    print(f"Received data: {data.model_dump_json()}")
    
    # 1. Fetch weather data
    weather_info = get_weather_data(data.lat, data.lon)
    if not weather_info:
        raise HTTPException(status_code=500, detail="Could not fetch weather data.")
        
    # 2. Combine all information for LangChain
    context_data = {
        "farmer_info": data.model_dump(),
        "weather_data": weather_info
    }
    
    # 3. Feed to LangChain
    # Here you'll call your LangChain agent with the combined context.
    # The agent will then interact with the vector DB for government schemes.
    # response_from_langchain = langchain_agent.run(context_data)
    
    # Placeholder response
    response_from_langchain = "This is a placeholder response from the LangChain agent based on your provided data."
    
    return {
        "status": "success",
        "result": response_from_langchain
    }

@app.post("/analyze-image")
async def analyze_plant_image(
    lat: float = Form(...),
    lon: float = Form(...),
    image: UploadFile = File(...)
):
    """
    Endpoint to receive an image of a plant and a location to detect disease and provide a response.
    """
    try:
        image_bytes = await image.read()
        
        # 1. Detect disease using ViT model
        disease_info = detect_disease_with_vit(image_bytes)
        
        # 2. Fetch weather data
        weather_info = get_weather_data(lat, lon)
        if not weather_info:
            raise HTTPException(status_code=500, detail="Could not fetch weather data.")
        
        # 3. Combine all information for LangChain
        context_data = {
            "disease_info": disease_info,
            "weather_data": weather_info,
            "location": {"lat": lat, "lon": lon}
        }
        
        # 4. Feed to LangChain for a comprehensive response
        # response_from_langchain = langchain_agent.run(context_data)
        
        # Placeholder response
        response_from_langchain = (
            f"Based on the image, it seems your plant has {disease_info['disease_name']}. "
            f"Here is some information about it and some advice. "
            f"We also considered the current weather."
        )
        
        return {
            "status": "success",
            "result": response_from_langchain
        }
        
    except Exception as e:
        print(f"Error processing image: {e}")
        raise HTTPException(status_code=500, detail="An error occurred while processing the image.")