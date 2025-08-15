package com.example.krishisevak.data.models

data class Weather(
    val id: String,
    val location: Location,
    val current: CurrentWeather,
    val forecast: List<WeatherForecast> = emptyList(),
    val lastUpdated: String
)

data class Location(
    val id: String,
    val name: String,
    val district: String,
    val state: String,
    val country: String = "India",
    val coordinates: Pair<Double, Double>, // latitude, longitude
    val timezone: String
)

data class CurrentWeather(
    val id: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Double,
    val windSpeed: Double,
    val windDirection: String,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Double,
    val condition: WeatherCondition,
    val description: String,
    val icon: String
)

data class WeatherForecast(
    val id: String,
    val date: String,
    val temperature: TemperatureRange,
    val condition: WeatherCondition,
    val description: String,
    val humidity: Double,
    val windSpeed: Double,
    val precipitation: Precipitation? = null,
    val sunrise: String,
    val sunset: String
)

data class TemperatureRange(
    val id: String,
    val min: Double,
    val max: Double,
    val day: Double,
    val night: Double,
    val morning: Double,
    val evening: Double
)

data class Precipitation(
    val id: String,
    val probability: Double, // 0.0 to 1.0
    val amount: Double? = null,
    val type: PrecipitationType,
    val duration: String? = null
)

data class IrrigationRecommendation(
    val id: String,
    val cropName: String,
    val recommendation: String,
    val reason: String,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val estimatedWater: Double? = null, // in mm
    val timing: String
)

enum class WeatherCondition {
    CLEAR,
    CLOUDY,
    PARTLY_CLOUDY,
    RAINY,
    THUNDERSTORM,
    FOGGY,
    WINDY,
    SNOWY,
    HAZY
}

enum class PrecipitationType {
    RAIN,
    DRIZZLE,
    SNOW,
    SLEET,
    HAIL,
    NONE
}
