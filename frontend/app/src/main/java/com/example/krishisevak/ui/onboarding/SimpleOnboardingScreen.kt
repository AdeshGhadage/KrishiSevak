package com.example.krishisevak.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleOnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var farmerName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var landArea by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Hindi") }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    
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
            text = stringResource(id = com.example.krishisevak.R.string.welcome_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(id = com.example.krishisevak.R.string.welcome_subtitle),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Farmer Name
        OutlinedTextField(
            value = farmerName,
            onValueChange = { farmerName = it },
            label = { Text(stringResource(id = com.example.krishisevak.R.string.farmer_name)) },
            leadingIcon = { Icon(Icons.Default.Person, "Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Age
        OutlinedTextField(
            value = age,
            onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
            label = { Text(stringResource(id = com.example.krishisevak.R.string.age)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Land Area
        OutlinedTextField(
            value = landArea,
            onValueChange = { landArea = it },
            label = { Text(stringResource(id = com.example.krishisevak.R.string.land_area)) },
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
                value = languages.find { it.first == selectedLanguage }?.second ?: stringResource(id = com.example.krishisevak.R.string.preferred_language),
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(id = com.example.krishisevak.R.string.preferred_language)) },
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
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Complete Setup Button
        Button(
            onClick = {
                scope.launch {
                    try {
                        val user = com.example.krishisevak.data.local.entities.UserEntity(
                            id = 1,
                            name = farmerName,
                            age = age.toIntOrNull() ?: 0,
                            landArea = landArea.toDoubleOrNull() ?: 0.0,
                            latitude = 28.6139, // Default Delhi coordinates
                            longitude = 77.2090,
                            preferredLanguage = selectedLanguage,
                            isOnboardingComplete = true
                        )
                        viewModel.saveUserData(user)
                        onOnboardingComplete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // For now, just complete onboarding even if save fails
                        onOnboardingComplete()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = farmerName.isNotEmpty() && 
                     age.isNotEmpty() && 
                     landArea.isNotEmpty() && 
                     selectedLanguage.isNotEmpty()
        ) {
            Text(
                text = stringResource(id = com.example.krishisevak.R.string.complete_setup),
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
