package com.example.krishisevak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.krishisevak.ui.home.HomeScreen
import com.example.krishisevak.ui.camera.CameraScreen
import com.example.krishisevak.ui.weather.WeatherScreen
import com.example.krishisevak.ui.disease.DiseaseDetectionScreen
import com.example.krishisevak.ui.schemes.GovernmentSchemesScreen
import com.example.krishisevak.ui.prices.PriceManagementScreen
import com.example.krishisevak.ui.voice.VoiceInterfaceScreen
import com.example.krishisevak.ui.theme.KrishiSevakTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Mic

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KrishiSevakTheme {
                KrishiSevakApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KrishiSevakApp() {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "home",
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "camera",
                    onClick = { navController.navigate("camera") },
                    icon = { Icon(Icons.Default.Camera, contentDescription = "Camera") },
                    label = { Text("Camera") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "weather",
                    onClick = { navController.navigate("weather") },
                    icon = { Icon(Icons.Default.WbSunny, contentDescription = "Weather") },
                    label = { Text("Weather") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "disease",
                    onClick = { navController.navigate("disease") },
                    icon = { Icon(Icons.Default.BugReport, contentDescription = "Disease") },
                    label = { Text("Disease") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "schemes",
                    onClick = { navController.navigate("schemes") },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Schemes") },
                    label = { Text("Schemes") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "prices",
                    onClick = { navController.navigate("prices") },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Prices") },
                    label = { Text("Prices") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "voice",
                    onClick = { navController.navigate("voice") },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Voice") },
                    label = { Text("Voice") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("camera") { CameraScreen() }
            composable("weather") { WeatherScreen() }
            composable("disease") { DiseaseDetectionScreen() }
            composable("schemes") { GovernmentSchemesScreen() }
            composable("prices") { PriceManagementScreen() }
            composable("voice") { VoiceInterfaceScreen() }
        }
    }
}