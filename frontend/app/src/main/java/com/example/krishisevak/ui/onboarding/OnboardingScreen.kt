package com.example.krishisevak.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location as AndroidLocation
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var farmerName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var landArea by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<AndroidLocation?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val languages = listOf(
        "Hindi" to "हिंदी",
        "English" to "English",
        "Punjabi" to "ਪੰਜਾਬੀ",
        "Bengali" to "বাংলা",
        "Tamil" to "தமிழ்",
        "Telugu" to "తెలుగు",
        "Marathi" to "मराठी",
        "Gujarati" to "ગુજરાતી",
        "Kannada" to "ಕನ್ನಡ",
        "Malayalam" to "മലയാളം"
    )
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            getCurrentLocation(fusedLocationClient) { loc ->
                location = loc
                isLoadingLocation = false
            }
        }
    }
    
    // Check location permission on launch
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Header
        Icon(
            imageVector = Icons.Default.Agriculture,
            contentDescription = "KrishiSevak",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Welcome to KrishiSevak",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Let's set up your farming profile",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Farmer Name
        OutlinedTextField(
            value = farmerName,
            onValueChange = { farmerName = it },
            label = { Text("Farmer Name") },
            leadingIcon = { Icon(Icons.Default.Person, "Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Age
        OutlinedTextField(
            value = age,
            onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Land Area
        OutlinedTextField(
            value = landArea,
            onValueChange = { landArea = it },
            label = { Text("Land Area (in acres)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Language Selection
        ExposedDropdownMenuBox(
            expanded = showLanguageDropdown,
            onExpandedChange = { showLanguageDropdown = !showLanguageDropdown }
        ) {
            OutlinedTextField(
                value = languages.find { it.first == selectedLanguage }?.second ?: "Select Language",
                onValueChange = { },
                readOnly = true,
                label = { Text("Preferred Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLanguageDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = showLanguageDropdown,
                onDismissRequest = { showLanguageDropdown = false }
            ) {
                languages.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedLanguage = code
                            showLanguageDropdown = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Location Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Farm Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (location != null) {
                    Text(
                        text = "Location captured successfully!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Lat: ${String.format("%.6f", location!!.latitude)}, Lng: ${String.format("%.6f", location!!.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "We need your farm location to provide accurate weather and local information.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            if (hasLocationPermission) {
                                isLoadingLocation = true
                                getCurrentLocation(fusedLocationClient) { loc ->
                                    location = loc
                                    isLoadingLocation = false
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoadingLocation
                    ) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Getting Location...")
                        } else {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Current Location")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Complete Setup Button
        Button(
            onClick = {
                scope.launch {
                    val user = com.example.krishisevak.data.local.entities.UserEntity(
                        id = 1,
                        name = farmerName,
                        age = age.toIntOrNull() ?: 0,
                        landArea = landArea.toDoubleOrNull() ?: 0.0,
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0,
                        preferredLanguage = selectedLanguage,
                        isOnboardingComplete = true
                    )
                    viewModel.saveUserData(user)
                    onOnboardingComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = farmerName.isNotEmpty() && 
                     age.isNotEmpty() && 
                     landArea.isNotEmpty() && 
                     selectedLanguage.isNotEmpty() && 
                     location != null
        ) {
            Text(
                text = "Complete Setup",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "All information is stored locally on your device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (AndroidLocation?) -> Unit
) {
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: AndroidLocation? ->
                onLocationReceived(location)
            }
            .addOnFailureListener {
                onLocationReceived(null)
            }
    } catch (e: SecurityException) {
        onLocationReceived(null)
    }
}
