package com.example.krishisevak.ui.home

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.krishisevak.R
import com.example.krishisevak.data.models.*
import com.example.krishisevak.ui.camera.DiseaseAnalysisResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePageHomeScreen() {
    val scrollState = rememberScrollState()
    var expandedSection by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        AppHeader()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Actions Row
        QuickActionsRow(
            onExpandSection = { section -> 
                expandedSection = if (expandedSection == section) null else section 
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Expandable Sections
        
        // Camera & Disease Detection Section
        AnimatedVisibility(
            visible = expandedSection == "camera",
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            CameraSection()
        }
        
        // Weather & Irrigation Section
        AnimatedVisibility(
            visible = expandedSection == "weather",
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            WeatherSection()
        }
        
        // Price Management Section
        AnimatedVisibility(
            visible = expandedSection == "prices",
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            PriceSection()
        }
        
        // Government Schemes Section
        AnimatedVisibility(
            visible = expandedSection == "schemes",
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            SchemesSection()
        }
        
        // Voice Interface Section
        AnimatedVisibility(
            visible = expandedSection == "voice",
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            VoiceSection()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Always visible sections
        FarmingTipCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        WeatherSummaryCard()
        
        Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
    }
}

@Composable
fun AppHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌾 KrishiSevak",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your Complete Agricultural Companion",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QuickActionsRow(onExpandSection: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        val actions = listOf(
            Triple("camera", Icons.Default.Camera, "Camera"),
            Triple("weather", Icons.Default.WbSunny, "Weather"),
            Triple("prices", Icons.Default.AttachMoney, "Prices"),
            Triple("schemes", Icons.Default.Assignment, "Schemes"),
            Triple("voice", Icons.Default.Mic, "Voice")
        )
        
        items(actions.size) { index ->
            val (id, icon, label) = actions[index]
            QuickActionChip(
                icon = icon,
                label = label,
                onClick = { onExpandSection(id) }
            )
        }
    }
}

@Composable
fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ElevatedFilterChip(
        selected = false,
        onClick = onClick,
        label = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp)
                )
                Text(label)
            }
        },
        modifier = Modifier.height(48.dp)
    )
}

@Composable
fun CameraSection() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<DiseaseAnalysisResult?>(null) }
    
    // Check permission on first launch
    LaunchedEffect(Unit) {
        hasCameraPermission = context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Camera",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Plant Disease Detection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (capturedImage == null) {
                if (hasCameraPermission) {
                    CompactCameraPreview(
                        onImageCaptured = { bitmap ->
                            capturedImage = bitmap
                        },
                        lifecycleOwner = lifecycleOwner,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    CompactPermissionRequest(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }
            } else {
                CompactCapturedImageDisplay(
                    image = capturedImage!!,
                    onRetake = {
                        capturedImage = null
                        analysisResult = null
                    },
                    onAnalyze = {
                        isAnalyzing = true
                        simulateDiseaseAnalysis { result ->
                            analysisResult = result
                            isAnalyzing = false
                        }
                    }
                )
            }
            
            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Analyzing plant...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            analysisResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                CompactAnalysisResult(result = result)
            }
        }
    }
}

@Composable
fun WeatherSection() {
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var weatherData by remember { mutableStateOf<Weather?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCrop by remember { mutableStateOf("Rice") }
    
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Weather",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Weather & Irrigation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CompactLocationInput(
                onLocationSelected = { location ->
                    currentLocation = location
                    scope.launch {
                        isLoading = true
                        delay(1500)
                        weatherData = generateCompactWeatherData(location)
                        isLoading = false
                    }
                }
            )
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Loading weather...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            weatherData?.let { weather ->
                Spacer(modifier = Modifier.height(16.dp))
                CompactWeatherDisplay(weather = weather)
            }
        }
    }
}

@Composable
fun PriceSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Prices",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Market Prices",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val samplePrices = listOf(
                Triple("Rice", "₹2,850", "per quintal"),
                Triple("Wheat", "₹2,125", "per quintal"),
                Triple("Cotton", "₹5,650", "per quintal"),
                Triple("Fertilizer (Urea)", "₹325", "per bag")
            )
            
            samplePrices.forEach { (crop, price, unit) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = crop,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = price,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (crop != samplePrices.last().first) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun SchemesSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = "Schemes",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Government Schemes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val schemes = listOf(
                "PM-KISAN Samman Nidhi",
                "Pradhan Mantri Fasal Bima Yojana",
                "Soil Health Card Scheme",
                "National Agriculture Market (e-NAM)"
            )
            
            schemes.forEach { scheme ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = scheme,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { /* Handle eligibility check */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check Eligibility")
            }
        }
    }
}

@Composable
fun VoiceSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Voice Assistant",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FloatingActionButton(
                onClick = { /* Handle voice input */ },
                modifier = Modifier.size(64.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start voice input",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Tap to speak in Hindi, English, or your local language",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun FarmingTipCard() {
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
                text = "🌱 Daily Farming Tip",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Water your plants early in the morning to reduce evaporation and fungal growth. Morning watering allows plants to absorb moisture before the heat of the day.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeatherSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Today's Weather",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "28°C • Partly Cloudy",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Good for fieldwork",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Weather",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Helper Composables

@Composable
fun CompactCameraPreview(
    onImageCaptured: (Bitmap) -> Unit,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    
                    previewView.setOnClickListener {
                        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                            java.io.File.createTempFile("plant_image", ".jpg", ctx.cacheDir)
                        ).build()
                        
                        imageCapture.takePicture(
                            outputFileOptions,
                            executor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val bitmap = android.graphics.BitmapFactory.decodeFile(
                                        outputFileResults.savedUri?.path ?: ""
                                    )
                                    onImageCaptured(bitmap)
                                }
                                
                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                }
                            }
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
            
            previewView
        },
        modifier = modifier
    )
}

@Composable
fun CompactPermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = "Camera",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(onClick = onRequestPermission) {
            Text("Allow Camera")
        }
    }
}

@Composable
fun CompactCapturedImageDisplay(
    image: Bitmap,
    onRetake: () -> Unit,
    onAnalyze: () -> Unit
) {
    Column {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Captured Plant",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retake")
            }
            
            Button(
                onClick = onAnalyze,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Analytics, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Analyze")
            }
        }
    }
}

@Composable
fun CompactAnalysisResult(result: DiseaseAnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "🔍 Analysis Result",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${result.plantName} • ${result.diseaseName}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "Confidence: ${(result.confidence * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (result.treatments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Treatment: ${result.treatments.first()}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CompactLocationInput(onLocationSelected: (Location) -> Unit) {
    var village by remember { mutableStateOf("") }
    
    Column {
        OutlinedTextField(
            value = village,
            onValueChange = { village = it },
            label = { Text("Village/City") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { 
                Icon(Icons.Default.LocationOn, "Location", modifier = Modifier.size(20.dp)) 
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = {
                if (village.isNotEmpty()) {
                    onLocationSelected(
                        Location(
                            id = "1",
                            name = village,
                            district = "Sample District",
                            state = "Sample State",
                            coordinates = Pair(28.6139, 77.2090), // Default to Delhi
                            timezone = "Asia/Kolkata"
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = village.isNotEmpty()
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Get Weather")
        }
    }
}

@Composable
fun CompactWeatherDisplay(weather: Weather) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${weather.current.temperature.toInt()}°C",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = weather.current.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = weather.current.description,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CompactWeatherDetail("Humidity", "${weather.current.humidity.toInt()}%")
            CompactWeatherDetail("Wind", "${weather.current.windSpeed.toInt()} km/h")
            CompactWeatherDetail("Feels", "${weather.current.feelsLike.toInt()}°C")
        }
    }
}

@Composable
fun CompactWeatherDetail(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

// Helper functions

private fun simulateDiseaseAnalysis(onComplete: (DiseaseAnalysisResult) -> Unit) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
        delay(2000)
        
        val sampleResults = listOf(
            DiseaseAnalysisResult(
                plantName = "Tomato",
                diseaseName = "Early Blight",
                confidence = 0.87,
                treatments = listOf("Remove infected leaves", "Apply copper fungicide")
            ),
            DiseaseAnalysisResult(
                plantName = "Rice",
                diseaseName = "Bacterial Leaf Blight",
                confidence = 0.92,
                treatments = listOf("Use resistant varieties", "Apply streptomycin sulfate")
            )
        )
        
        onComplete(sampleResults.random())
    }
}

private fun generateCompactWeatherData(location: Location): Weather {
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
        forecast = emptyList(),
        lastUpdated = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    )
}
