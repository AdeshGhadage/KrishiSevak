package com.example.krishisevak.ui.camera

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.krishisevak.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource

@Composable
fun CameraScreen() {
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Plant Disease Detection",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (capturedImage == null) {
            // Camera Preview
            if (hasCameraPermission) {
                CameraPreview(
                    onImageCaptured = { bitmap ->
                        capturedImage = bitmap
                    },
                    lifecycleOwner = lifecycleOwner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                // Permission Request
                PermissionRequest(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        } else {
            // Captured Image Display
            CapturedImageDisplay(
                image = capturedImage!!,
                onRetake = {
                    capturedImage = null
                    analysisResult = null
                },
                onAnalyze = {
                    isAnalyzing = true
                    // Simulate disease analysis
                    simulateDiseaseAnalysis { result ->
                        analysisResult = result
                        isAnalyzing = false
                    }
                }
            )
        }
        
        // Analysis Results
        analysisResult?.let { result ->
            Spacer(modifier = Modifier.height(24.dp))
            DiseaseAnalysisCard(result = result)
        }
        
        // Loading State
        if (isAnalyzing) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Analyzing plant image...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
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
                    
                    // Capture button overlay
                    previewView.setOnClickListener {
                        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                            java.io.File.createTempFile("plant_image", ".jpg", context.cacheDir)
                        ).build()
                        
                        imageCapture.takePicture(
                            outputFileOptions,
                            executor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    // Convert saved file to bitmap
                                    val bitmap = android.graphics.BitmapFactory.decodeFile(
                                        outputFileResults.savedUri?.path ?: ""
                                    )
                                    onImageCaptured(bitmap)
                                }
                                
                                override fun onError(exception: ImageCaptureException) {
                                    // Handle error
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
fun PermissionRequest(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Camera Permission",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "KrishiSevak needs camera access to detect plant diseases. Please grant camera permission to continue.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun CapturedImageDisplay(
    image: Bitmap,
    onRetake: () -> Unit,
    onAnalyze: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Captured Plant Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retake")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retake")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = onAnalyze,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Analytics, contentDescription = "Analyze")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze")
            }
        }
    }
}

@Composable
fun DiseaseAnalysisCard(result: DiseaseAnalysisResult) {
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
                text = "Analysis Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Plant: ${result.plantName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Disease: ${result.diseaseName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Confidence: ${(result.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Treatment Recommendations:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            result.treatments.forEach { treatment ->
                Text(
                    text = "• ${treatment}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

// Data classes for disease analysis
data class DiseaseAnalysisResult(
    val plantName: String,
    val diseaseName: String,
    val confidence: Double,
    val treatments: List<String>
)

// Simulate disease analysis
private fun simulateDiseaseAnalysis(onComplete: (DiseaseAnalysisResult) -> Unit) {
    // Simulate network delay
    kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
        delay(3000)
        
        val sampleResults = listOf(
            DiseaseAnalysisResult(
                plantName = "Tomato",
                diseaseName = "Early Blight",
                confidence = 0.87,
                treatments = listOf(
                    "Remove infected leaves",
                    "Apply copper-based fungicide",
                    "Improve air circulation",
                    "Avoid overhead watering"
                )
            ),
            DiseaseAnalysisResult(
                plantName = "Rice",
                diseaseName = "Bacterial Leaf Blight",
                confidence = 0.92,
                treatments = listOf(
                    "Use resistant varieties",
                    "Apply streptomycin sulfate",
                    "Maintain proper spacing",
                    "Control water management"
                )
            ),
            DiseaseAnalysisResult(
                plantName = "Wheat",
                diseaseName = "Rust",
                confidence = 0.78,
                treatments = listOf(
                    "Apply fungicide at early stage",
                    "Use resistant varieties",
                    "Crop rotation",
                    "Proper field sanitation"
                )
            )
        )
        
        onComplete(sampleResults.random())
    }
}
