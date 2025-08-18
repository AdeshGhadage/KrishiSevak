from __future__ import annotations
import httpx
from typing import Literal
from langchain_core.tools import tool

GEOCODE_URL = "https://geocoding-api.open-meteo.com/v1/search"
FORECAST_URL = "https://api.open-meteo.com/v1/forecast"

@tool("get_weather", return_direct=False)
def get_weather(city: str, unit: Literal["c","f"]="c") -> dict:
    """Get current weather for a city using Open-Meteo (no API key)."""
    try:
        with httpx.Client(timeout=10.0) as client:
            geor = client.get(GEOCODE_URL, params={"name": city, "count": 1, "language": "en"}).json()
            if not geor.get("results"):
                return {"error": f"No match for '{city}'"}
            loc = geor["results"][0]
            lat, lon = loc["latitude"], loc["longitude"]
            params = {"latitude": lat, "longitude": lon, "current": "temperature_2m,relative_humidity_2m,weather_code"}
            if unit == "f": params["temperature_unit"] = "fahrenheit"
            fr = client.get(FORECAST_URL, params=params).json()
            cur = fr.get("current") or {}
            return {
                "city": loc["name"],
                "country": loc.get("country"),
                "latitude": lat,
                "longitude": lon,
                "temperature": cur.get("temperature_2m"),
                "relative_humidity": cur.get("relative_humidity_2m"),
                "weather_code": cur.get("weather_code"),
                "unit": "F" if unit == "f" else "C"
            }
    except Exception as e:
        return {"error": str(e)}
