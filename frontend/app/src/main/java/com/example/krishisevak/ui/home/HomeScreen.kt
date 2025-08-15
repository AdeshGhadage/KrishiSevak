package com.example.krishisevak.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "KrishiSevak",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your Intelligent Agricultural Advisor",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Feature Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeatureCard(
                    title = "Plant Disease Detection",
                    description = "Identify plant diseases using your camera",
                    icon = Icons.Default.Camera,
                    onClick = { navController.navigate("camera") }
                )
            }
            
            item {
                FeatureCard(
                    title = "Weather & Irrigation",
                    description = "Get weather forecasts and irrigation advice",
                    icon = Icons.Default.WbSunny,
                    onClick = { navController.navigate("weather") }
                )
            }
            
            item {
                FeatureCard(
                    title = "Disease Analysis",
                    description = "Detailed disease information and treatments",
                    icon = Icons.Default.BugReport,
                    onClick = { navController.navigate("disease") }
                )
            }
            
            item {
                FeatureCard(
                    title = "Government Schemes",
                    description = "Check eligibility for subsidies and schemes",
                    icon = Icons.Default.Assignment,
                    onClick = { navController.navigate("schemes") }
                )
            }
            
            item {
                FeatureCard(
                    title = "Price Management",
                    description = "Track crop and fertilizer prices",
                    icon = Icons.Default.AttachMoney,
                    onClick = { navController.navigate("prices") }
                )
            }
            
            item {
                FeatureCard(
                    title = "Voice Interface",
                    description = "Multilingual voice commands and responses",
                    icon = Icons.Default.Mic,
                    onClick = { navController.navigate("voice") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Actions
        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                text = "Take Photo",
                icon = Icons.Default.Camera,
                onClick = { navController.navigate("camera") }
            )
            
            QuickActionButton(
                text = "Check Weather",
                icon = Icons.Default.WbSunny,
                onClick = { navController.navigate("weather") }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Farming Tip of the Day
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
                    text = "🌱 Farming Tip of the Day",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Water your plants early in the morning to reduce evaporation and fungal growth. Morning watering allows plants to absorb moisture before the heat of the day.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(text = text, fontSize = 12.sp)
        }
    }
}
