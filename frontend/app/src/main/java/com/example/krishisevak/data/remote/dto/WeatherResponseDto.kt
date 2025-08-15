package com.example.krishisevak.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponseDto(
    val latitude: Double,
    val longitude: Double,
    val generationtimeMs: Double,
    val utcOffsetSeconds: Int,
    val timezone: String,
    val timezoneAbbreviation: String,
    val elevation: Double,
    val current: CurrentWeatherDto,
    val currentUnits: CurrentUnitsDto,
    val hourly: HourlyWeatherDto?,
    val hourlyUnits: HourlyUnitsDto?,
    val daily: DailyWeatherDto?,
    val dailyUnits: DailyUnitsDto?
)

data class CurrentWeatherDto(
    val time: String,
    val interval: Int,
    @SerializedName("temperature_2m")
    val temperature2m: Double,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: Int,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double,
    val precipitation: Double,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: Double,
    @SerializedName("wind_direction_10m")
    val windDirection10m: Int,
    @SerializedName("pressure_msl")
    val pressureMsl: Double,
    val visibility: Double,
    @SerializedName("uv_index")
    val uvIndex: Double
)

data class CurrentUnitsDto(
    val time: String,
    val interval: String,
    @SerializedName("temperature_2m")
    val temperature2m: String,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: String,
    @SerializedName("apparent_temperature")
    val apparentTemperature: String,
    val precipitation: String,
    @SerializedName("weather_code")
    val weatherCode: String,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: String,
    @SerializedName("wind_direction_10m")
    val windDirection10m: String,
    @SerializedName("pressure_msl")
    val pressureMsl: String,
    val visibility: String,
    @SerializedName("uv_index")
    val uvIndex: String
)

data class HourlyWeatherDto(
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature2m: List<Double>,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: List<Int>,
    val precipitation: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: List<Double>,
    @SerializedName("wind_direction_10m")
    val windDirection10m: List<Int>,
    @SerializedName("uv_index")
    val uvIndex: List<Double>
)

data class HourlyUnitsDto(
    val time: String,
    @SerializedName("temperature_2m")
    val temperature2m: String,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: String,
    val precipitation: String,
    @SerializedName("weather_code")
    val weatherCode: String,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: String,
    @SerializedName("wind_direction_10m")
    val windDirection10m: String,
    @SerializedName("uv_index")
    val uvIndex: String
)

data class DailyWeatherDto(
    val time: List<String>,
    @SerializedName("temperature_2m_max")
    val temperature2mMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: List<Double>,
    @SerializedName("precipitation_sum")
    val precipitationSum: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("uv_index_max")
    val uvIndexMax: List<Double>
)

data class DailyUnitsDto(
    val time: String,
    @SerializedName("temperature_2m_max")
    val temperature2mMax: String,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: String,
    @SerializedName("precipitation_sum")
    val precipitationSum: String,
    @SerializedName("weather_code")
    val weatherCode: String,
    @SerializedName("uv_index_max")
    val uvIndexMax: String
)
