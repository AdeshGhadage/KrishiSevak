package com.example.krishisevak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.krishisevak.ui.onboarding.SimpleOnboardingScreen
import com.example.krishisevak.ui.onboarding.OnboardingViewModel
import com.example.krishisevak.ui.chat.ChatScreen
import com.example.krishisevak.ui.theme.KrishiSevakTheme
import kotlinx.coroutines.launch

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
    val viewModel: OnboardingViewModel = viewModel()
    var isOnboarded by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()
    
    // Check onboarding status
    LaunchedEffect(Unit) {
        scope.launch {
            isOnboarded = viewModel.isUserOnboarded()
        }
    }
    
    when (isOnboarded) {
        null -> {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        false -> {
            // Show onboarding
            SimpleOnboardingScreen(
                onOnboardingComplete = {
                    isOnboarded = true
                },
                viewModel = viewModel
            )
        }
        true -> {
            // Show main chat interface
            ChatScreen()
        }
    }
}