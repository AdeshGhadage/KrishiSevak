package com.example.krishisevak.ui.chat

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.compose.runtime.DisposableEffect
import com.example.krishisevak.data.models.ChatRequestDto
import com.example.krishisevak.data.local.KrishiSevakDatabase
import com.example.krishisevak.data.remote.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import java.util.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import org.json.JSONObject

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
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    val mediaPlayer = remember { MediaPlayer() }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val db = remember { KrishiSevakDatabase.getDatabase(context) }
    var preferredLanguage by remember { mutableStateOf("en") }
    LaunchedEffect(Unit) {
        val user = db.userDao().getOnboardedUser()
        preferredLanguage = mapLanguageToCode(user?.preferredLanguage)
    }
    
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
            
            scope.launch {
                val classifyResult = try {
                    val part = bitmapToMultipart("file", it)
                    val res = withContext(Dispatchers.IO) {
                        NetworkModule.backendApiService.classifyImage(part)
                    }
                    "Detected: ${res.label} (confidence ${(res.score * 100).toInt()}%)"
                } catch (e: Exception) {
                    "Image analysis failed: ${e.message ?: "unknown error"}"
                }
                val response = ChatMessage(
                    text = classifyResult,
                    isFromUser = false
                )
                messages = messages + response
                scope.launch { listState.animateScrollToItem(messages.size - 1) }
            }
        }
    }
    
    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { pickedUri: Uri? ->
        pickedUri?.let { uri ->
            // Decode for preview
            val bmp = try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } catch (_: Exception) { null }
            if (bmp != null) {
                val newMessage = ChatMessage(
                    image = bmp,
                    isFromUser = true,
                    text = "Plant image selected"
                )
                messages = messages + newMessage
            }
            // Upload for classification
            scope.launch {
                val classifyResult = try {
                    val part = uriToMultipart(context, "file", uri)
                    val res = withContext(Dispatchers.IO) {
                        NetworkModule.backendApiService.classifyImage(part)
                    }
                    "Detected: ${res.label} (confidence ${(res.score * 100).toInt()}%)"
                } catch (e: Exception) {
                    "Image analysis failed: ${e.message ?: "unknown error"}"
                }
                val response = ChatMessage(
                    text = classifyResult,
                    isFromUser = false
                )
                messages = messages + response
                scope.launch { listState.animateScrollToItem(messages.size - 1) }
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

    DisposableEffect(Unit) {
        onDispose {
            try { mediaRecorder?.release() } catch (_: Exception) {}
            try { mediaPlayer.release() } catch (_: Exception) {}
        }
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
                                            
                                            scope.launch {
                                                val responseText = try {
                                                    val user = db.userDao().getOnboardedUser()
                                                    val contextStr = if (user != null) {
                                                        "Farmer Name: ${user.name}; Age: ${user.age}; Land Area(acres): ${user.landArea}; Location(lat,lon): ${user.latitude}, ${user.longitude}; Preferred Language: ${user.preferredLanguage}"
                                                    } else null
                                                    val req = ChatRequestDto(message = userMessage.text, userContext = contextStr)
                                                    val res = withContext(Dispatchers.IO) {
                                                        NetworkModule.backendApiService.chat(req)
                                                    }
                                                    res.text
                                                } catch (e: Exception) {
                                                    "Chat request failed: ${e.message ?: "unknown error"}"
                                                }
                                                val response = ChatMessage(
                                                    text = responseText,
                                                    isFromUser = false
                                                )
                                                messages = messages + response
                                                scope.launch { listState.animateScrollToItem(messages.size - 1) }
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
                    // Gallery button
                    FloatingActionButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = context.getString(com.example.krishisevak.R.string.choose_from_gallery),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
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
                                if (!isRecording) {
                                    try {
                                        val outFile = File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
                                        val recorder = MediaRecorder()
                                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                                        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                        recorder.setAudioEncodingBitRate(128_000)
                                        recorder.setAudioSamplingRate(44_100)
                                        recorder.setOutputFile(outFile.absolutePath)
                                        recorder.prepare()
                                        recorder.start()
                                        mediaRecorder = recorder
                                        recordingFile = outFile
                                        isRecording = true
                                        messages = messages + ChatMessage(
                                            text = "Recording...",
                                            isFromUser = false
                                        )
                                    } catch (e: Exception) {
                                        isRecording = false
                                        messages = messages + ChatMessage(
                                            text = "Failed to start recording: ${e.message}",
                                            isFromUser = false
                                        )
                                    }
                                } else {
                                    val recorder = mediaRecorder
                                    val outFile = recordingFile
                                    mediaRecorder = null
                                    recordingFile = null
                                    try {
                                        recorder?.stop()
                                    } catch (_: Exception) {}
                                    try { recorder?.release() } catch (_: Exception) {}
                                    isRecording = false
                                    if (outFile != null && outFile.exists()) {
                                        val userVoiceMsg = ChatMessage(
                                            text = "[Voice message sent]",
                                            isFromUser = true,
                                            isVoiceMessage = true
                                        )
                                        messages = messages + userVoiceMsg
                                        scope.launch {
                                            try {
                                                val sessionBody = "default".toRequestBody("text/plain".toMediaType())
                                                val ttsBody = "true".toRequestBody("text/plain".toMediaType())
                                                val sttLang = mapToWhisperLanguageCode(preferredLanguage)
                                                val langBody: RequestBody? = sttLang.toRequestBody("text/plain".toMediaType())
                                                val audioPart = MultipartBody.Part.createFormData(
                                                    name = "audio",
                                                    filename = outFile.name,
                                                    body = outFile.asRequestBody("audio/m4a".toMediaType())
                                                )
                                                val resp = withContext(Dispatchers.IO) {
                                                    NetworkModule.backendApiService.voiceToChat(
                                                        sessionId = sessionBody,
                                                        tts = ttsBody,
                                                        language = langBody,
                                                        audio = audioPart
                                                    )
                                                }
                                                if (resp.isSuccessful) {
                                                    val body = resp.body()
                                                    if (body != null) {
                                                        val audioOut = saveResponseToFile(context, body)
                                                        playAudio(mediaPlayer, audioOut)
                                                    } else {
                                                        messages = messages + ChatMessage(text = "Voice processing failed: empty body", isFromUser = false)
                                                    }
                                                } else {
                                                    messages = messages + ChatMessage(text = "Voice processing failed: ${resp.code()}", isFromUser = false)
                                                }
                                            } catch (e: Exception) {
                                                messages = messages + ChatMessage(text = "Voice processing error: ${e.message ?: "unknown error"}", isFromUser = false)
                                            }
                                        }
                                    } else {
                                        messages = messages + ChatMessage(
                                            text = "No recording file found",
                                            isFromUser = false
                                        )
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
                    if (message.isFromUser) {
                        Text(
                            text = message.text,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val botTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
                        AndroidView(
                            factory = { ctx ->
                                android.widget.TextView(ctx).apply {
                                    setTextColor(botTextColor)
                                }
                            },
                            update = { tv ->
                                val markwon = io.noties.markwon.Markwon.create(tv.context)
                                markwon.setMarkdown(tv, message.text)
                            }
                        )
                    }
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

private fun bitmapToMultipart(formField: String, bitmap: Bitmap): MultipartBody.Part {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    val bytes = stream.toByteArray()
    val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
    return MultipartBody.Part.createFormData(formField, "image.jpg", requestBody)
}

private fun uriToMultipart(context: android.content.Context, formField: String, uri: Uri): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val mime = contentResolver.getType(uri) ?: "image/jpeg"
    val fileName = "image_${System.currentTimeMillis()}" + if (mime.contains("png")) ".png" else ".jpg"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val requestBody = bytes.toRequestBody(mime.toMediaType())
    return MultipartBody.Part.createFormData(formField, fileName, requestBody)
}

private fun saveResponseToFile(context: android.content.Context, body: ResponseBody): File {
    val out = File(context.cacheDir, "tts_${System.currentTimeMillis()}.bin")
    body.byteStream().use { input ->
        FileOutputStream(out).use { output ->
            val buffer = ByteArray(8 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                output.write(buffer, 0, read)
            }
            output.flush()
        }
    }
    return out
}

private fun playAudio(player: MediaPlayer, file: File) {
    try {
        player.reset()
        player.setDataSource(file.absolutePath)
        player.prepare()
        player.start()
    } catch (_: Exception) {}
}

private fun mapLanguageToCode(name: String?): String {
    return when (name?.lowercase()?.trim()) {
        "english" -> "en-in" // Indian English
        "hindi" -> "hi"
        "punjabi" -> "pa"
        "bengali" -> "bn"
        "tamil" -> "ta"
        "telugu" -> "te"
        "marathi" -> "mr"
        "gujarati" -> "gu"
        "kannada" -> "kn"
        "malayalam" -> "ml"
        else -> "en-in"
    }
}

private fun mapToWhisperLanguageCode(langTag: String?): String {
    // OpenAI Whisper expects ISO 639-1 like "en", "hi". Our mapLanguageToCode returns e.g. en-in.
    val tag = (langTag ?: "en").lowercase()
    return when {
        tag.startsWith("en") -> "en"
        tag.startsWith("hi") -> "hi"
        tag.startsWith("bn") -> "bn"
        tag.startsWith("mr") -> "mr"
        tag.startsWith("ta") -> "ta"
        tag.startsWith("te") -> "te"
        tag.startsWith("kn") -> "kn"
        tag.startsWith("ml") -> "ml"
        tag.startsWith("gu") -> "gu"
        tag.startsWith("pa") -> "pa"
        else -> "en"
    }
}
