package com.example.krishisevak.ui.chat

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val image: Bitmap? = null,
    val isFromUser: Boolean,
    val timestamp: Date = Date(),
    val isVoiceMessage: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentMessage by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Permission launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val newMessage = ChatMessage(
                image = it,
                isFromUser = true,
                text = "Plant image captured"
            )
            messages = messages + newMessage
            
            // Simulate AI response
            scope.launch {
                kotlinx.coroutines.delay(1500)
                val response = ChatMessage(
                    text = "I can see your plant image. Let me analyze it for any diseases or issues. The plant appears to be healthy with good leaf color. Would you like me to provide specific care recommendations?",
                    isFromUser = false
                )
                messages = messages + response
                
                // Auto-scroll to bottom
                scope.launch {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }
    
    // Check permissions on launch
    LaunchedEffect(Unit) {
        hasCameraPermission = context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        hasAudioPermission = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        // Add welcome message
        messages = listOf(
            ChatMessage(
                text = "Welcome to KrishiSevak! 🌾\n\nI'm your AI farming assistant. You can:\n• Type your farming questions\n• Take photos of plants for disease detection\n• Use voice commands in your local language\n\nHow can I help you today?",
                isFromUser = false
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌾",
                            fontSize = 20.sp
                        )
                    }
                    
                    Column {
                        Text(
                            text = "KrishiSevak AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
        }
        
        // Input Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Input field
                    OutlinedTextField(
                        value = currentMessage,
                        onValueChange = { currentMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { 
                            Text(
                                text = "Ask anything about farming...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        trailingIcon = {
                            if (currentMessage.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (currentMessage.trim().isNotEmpty()) {
                                            val userMessage = ChatMessage(
                                                text = currentMessage.trim(),
                                                isFromUser = true
                                            )
                                            messages = messages + userMessage
                                            currentMessage = ""
                                            
                                            // Simulate AI response
                                            scope.launch {
                                                kotlinx.coroutines.delay(1000)
                                                val response = generateAIResponse(userMessage.text)
                                                messages = messages + response
                                                
                                                // Auto-scroll to bottom
                                                scope.launch {
                                                    listState.animateScrollToItem(messages.size - 1)
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )
                    
                    // Camera button
                    FloatingActionButton(
                        onClick = {
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = "Take Photo",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    // Voice button
                    FloatingActionButton(
                        onClick = {
                            if (hasAudioPermission) {
                                isRecording = !isRecording
                                // Handle voice recording
                                if (isRecording) {
                                    // Start recording
                                } else {
                                    // Stop recording and process
                                    val voiceMessage = ChatMessage(
                                        text = "Voice message processed: 'What fertilizer is best for wheat?'",
                                        isFromUser = true,
                                        isVoiceMessage = true
                                    )
                                    messages = messages + voiceMessage
                                    
                                    // Generate AI response
                                    scope.launch {
                                        kotlinx.coroutines.delay(1000)
                                        val response = generateAIResponse(voiceMessage.text)
                                        messages = messages + response
                                    }
                                }
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (isRecording) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = if (isRecording) 
                                MaterialTheme.colorScheme.onError 
                            else 
                                MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Image if present
                message.image?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Plant image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (message.text.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Text message
                if (message.text.isNotEmpty()) {
                    Text(
                        text = message.text,
                        color = if (message.isFromUser) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Voice indicator
                if (message.isVoiceMessage) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice message",
                            modifier = Modifier.size(12.dp),
                            tint = if (message.isFromUser) 
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Voice",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.isFromUser) 
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Timestamp
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isFromUser) 
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

private fun generateAIResponse(userMessage: String): ChatMessage {
    val responses = when {
        userMessage.contains("fertilizer", ignoreCase = true) -> listOf(
            "For wheat, I recommend using DAP (Diammonium Phosphate) during sowing and Urea for top dressing. Apply 125 kg DAP + 65 kg Urea per hectare. The ideal ratio is 120:60:40 (NPK).",
            "Consider soil testing first to determine exact nutrient requirements. Organic options include well-decomposed farmyard manure (5-7 tons per hectare) or vermicompost."
        )
        
        userMessage.contains("weather", ignoreCase = true) -> listOf(
            "Current weather looks favorable for most crops. Temperature is moderate with good humidity levels. Perfect for fieldwork today!",
            "I recommend checking the 7-day forecast before planning irrigation or pesticide applications."
        )
        
        userMessage.contains("disease", ignoreCase = true) -> listOf(
            "Common crop diseases this season include leaf blight and powdery mildew. Early detection is key - look for yellowing leaves or white powdery spots.",
            "For organic treatment, try neem oil spray (5ml per liter of water). For severe cases, copper-based fungicides are effective."
        )
        
        userMessage.contains("price", ignoreCase = true) -> listOf(
            "Current market prices: Wheat ₹2,125/quintal, Rice ₹2,850/quintal, Cotton ₹5,650/quintal. Prices are stable this week.",
            "I suggest selling wheat now as prices are at seasonal high. Hold rice for better rates next month."
        )
        
        else -> listOf(
            "That's a great farming question! Based on current agricultural practices, I recommend consulting with local agricultural extension officers for region-specific advice.",
            "I understand your concern. Could you provide more details about your specific crop or farming situation so I can give you more targeted advice?",
            "Thank you for asking! This is an important aspect of farming. Would you like me to break this down into specific steps or explain the process in detail?"
        )
    }
    
    return ChatMessage(
        text = responses.random(),
        isFromUser = false
    )
}
