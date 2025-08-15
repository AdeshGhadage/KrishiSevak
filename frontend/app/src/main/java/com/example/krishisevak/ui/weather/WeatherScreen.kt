package com.example.krishisevak.ui.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.krishisevak.R
import com.example.krishisevak.data.models.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen() {
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var weatherData by remember { mutableStateOf<Weather?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCrop by remember { mutableStateOf("Rice") }
    
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Weather & Irrigation",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Location Input
        LocationInput(
            onLocationSelected = { location ->
                currentLocation = location
                scope.launch {
                    isLoading = true
                    // Simulate weather API call
                    kotlinx.coroutines.delay(2000)
                    weatherData = generateSampleWeatherData(location)
                    isLoading = false
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Crop Selection
        CropSelection(
            selectedCrop = selectedCrop,
            onCropSelected = { crop -> selectedCrop = crop }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            LoadingWeather()
        } else {
            weatherData?.let { weather ->
                // Current Weather
                CurrentWeatherCard(weather = weather)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Weather Forecast
                WeatherForecastCard(forecast = weather.forecast)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Irrigation Recommendation
                IrrigationRecommendationCard(
                    cropName = selectedCrop,
                    weather = weather
                )
            }
        }
    }
}

@Composable
fun LocationInput(onLocationSelected: (Location) -> Unit) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Enter Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = village,
                onValueChange = { village = it },
                label = { Text("Village Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, "Location") }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.MyLocation, "Latitude") }
                )
                
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.MyLocation, "Longitude") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                        onLocationSelected(
                            Location(
                                id = "1",
                                name = village.ifEmpty { "Unknown" },
                                district = "Unknown",
                                state = "Unknown",
                                coordinates = Pair(
                                    latitude.toDoubleOrNull() ?: 0.0,
                                    longitude.toDoubleOrNull() ?: 0.0
                                ),
                                timezone = "Asia/Kolkata"
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = latitude.isNotEmpty() && longitude.isNotEmpty()
            ) {
                Icon(Icons.Default.LocationOn, "Search")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Weather")
            }
        }
    }
}

@Composable
fun CropSelection(
    selectedCrop: String,
    onCropSelected: (String) -> Unit
) {
    val crops = listOf("Rice", "Wheat", "Corn", "Soybeans", "Cotton", "Sugarcane")
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Crop for Irrigation Advice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(crops.size) { index ->
                    val crop = crops[index]
                    FilterChip(
                        selected = selectedCrop == crop,
                        onClick = { onCropSelected(crop) },
                        label = { Text(crop) }
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingWeather() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Loading weather data...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun CurrentWeatherCard(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Weather",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${weather.current.temperature.toInt()}°C",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weather.current.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Icon(
                    imageVector = getWeatherIcon(weather.current.description),
                    contentDescription = weather.current.description,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            WeatherDetailsRow(weather = weather.current)
        }
    }
}

@Composable
fun WeatherDetailsRow(weather: CurrentWeather) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WeatherDetailItem(
            icon = Icons.Default.Thermostat,
            label = "Feels Like",
            value = "${weather.feelsLike.toInt()}°C"
        )
        
        WeatherDetailItem(
            icon = Icons.Default.Opacity,
            label = "Humidity",
            value = "${weather.humidity}%"
        )
        
        WeatherDetailItem(
            icon = Icons.Default.Air,
            label = "Wind",
            value = "${weather.windSpeed.toInt()} km/h"
        )
    }
}

@Composable
fun WeatherDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun WeatherForecastCard(forecast: List<WeatherForecast>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(forecast.size) { index ->
                    val dayForecast = forecast[index]
                    ForecastDayCard(forecast = dayForecast)
                }
            }
        }
    }
}

@Composable
fun ForecastDayCard(forecast: WeatherForecast) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatDayOfWeek(forecast.date),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Icon(
                imageVector = getWeatherIcon(forecast.description),
                contentDescription = forecast.description,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${forecast.temperature.max.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${forecast.temperature.min.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun IrrigationRecommendationCard(cropName: String, weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Irrigation Recommendation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val recommendation = generateIrrigationRecommendation(cropName, weather)
            
            Text(
                text = "Crop: $cropName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Recommendation: ${recommendation.recommendation}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Reason: ${recommendation.reason}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Priority: ${recommendation.priority}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            recommendation.estimatedWater?.let { water ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Estimated Water: ${water} mm",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Timing: ${recommendation.timing}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun getWeatherIcon(description: String): ImageVector {
    return when {
        description.contains("rain", ignoreCase = true) -> Icons.Default.Opacity
        description.contains("cloud", ignoreCase = true) -> Icons.Default.Cloud
        description.contains("sun", ignoreCase = true) -> Icons.Default.WbSunny
        description.contains("storm", ignoreCase = true) -> Icons.Default.Thunderstorm
        description.contains("snow", ignoreCase = true) -> Icons.Default.AcUnit
        else -> Icons.Default.WbSunny
    }
}

// Utility functions

fun generateIrrigationRecommendation(cropName: String, weather: Weather): com.example.krishisevak.data.models.IrrigationRecommendation {
    val currentTemp = weather.current.temperature
    val humidity = weather.current.humidity
    
    return when {
        currentTemp > 30 && humidity < 60 -> com.example.krishisevak.data.models.IrrigationRecommendation(
            id = "1",
            cropName = cropName,
            recommendation = "Irrigation recommended due to high temperature and low humidity",
            reason = "High temperature increases evapotranspiration, requiring additional water",
            priority = "HIGH",
            estimatedWater = 25.0,
            timing = "Early morning or evening"
        )
        currentTemp < 15 -> com.example.krishisevak.data.models.IrrigationRecommendation(
            id = "2",
            cropName = cropName,
            recommendation = "Reduce irrigation - low temperature reduces water needs",
            reason = "Cold weather reduces plant water consumption",
            priority = "LOW",
            estimatedWater = 5.0,
            timing = "Morning"
        )
        else -> com.example.krishisevak.data.models.IrrigationRecommendation(
            id = "3",
            cropName = cropName,
            recommendation = "Moderate irrigation may be needed",
            reason = "Monitor soil moisture and adjust based on crop stage",
            priority = "MEDIUM",
            estimatedWater = 15.0,
            timing = "Morning"
        )
    }
}

fun generateSampleWeatherData(location: com.example.krishisevak.data.models.Location): Weather {
    val now = Calendar.getInstance()
    
    return Weather(
        id = "1",
        location = location,
        current = CurrentWeather(
            id = "1",
            temperature = 28.5,
            feelsLike = 30.2,
            humidity = 65.0,
            windSpeed = 12.0,
            windDirection = "SE",
            pressure = 1013.25,
            visibility = 10.0,
            uvIndex = 7.5,
            condition = WeatherCondition.PARTLY_CLOUDY,
            description = "Partly cloudy",
            icon = "partly-cloudy"
        ),
        forecast = (1..7).map { dayOffset ->
            val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
            WeatherForecast(
                id = dayOffset.toString(),
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time),
                temperature = TemperatureRange(
                    id = dayOffset.toString(),
                    min = 20.0 + dayOffset,
                    max = 30.0 + dayOffset,
                    day = 28.0 + dayOffset,
                    night = 22.0 + dayOffset,
                    morning = 25.0 + dayOffset,
                    evening = 26.0 + dayOffset
                ),
                condition = if (dayOffset % 2 == 0) WeatherCondition.CLEAR else WeatherCondition.PARTLY_CLOUDY,
                description = if (dayOffset % 2 == 0) "Sunny" else "Partly cloudy",
                humidity = 60.0 + (dayOffset * 2),
                windSpeed = 10.0 + dayOffset,
                precipitation = Precipitation(
                    id = dayOffset.toString(),
                    probability = if (dayOffset % 3 == 0) 0.3 else 0.1,
                    amount = if (dayOffset % 3 == 0) 2.5 else null,
                    type = if (dayOffset % 3 == 0) PrecipitationType.RAIN else PrecipitationType.NONE,
                    duration = if (dayOffset % 3 == 0) "2-3 hours" else null
                ),
                sunrise = "06:00",
                sunset = "18:00"
            )
        },
        lastUpdated = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now.time)
    )
}

// Utility function to format date string to day abbreviation
private fun formatDayOfWeek(dateString: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date ?: Date()
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    } catch (e: Exception) {
        dateString.substring(5, 8) // Fallback: extract month abbreviation
    }
}
