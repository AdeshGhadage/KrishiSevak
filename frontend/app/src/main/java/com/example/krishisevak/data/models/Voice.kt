package com.example.krishisevak.data.models

data class VoiceCommand(
    val id: String,
    val text: String,
    val language: String,
    val timestamp: String,
    val confidence: Double,
    val intent: VoiceIntent? = null
)

data class VoiceResponse(
    val id: String,
    val text: String,
    val language: String,
    val timestamp: String,
    val responseType: String, // "TEXT", "SPEECH", "BOTH"
    val shouldSpeak: Boolean = true
)

data class VoiceIntent(
    val id: String,
    val type: IntentType,
    val confidence: Double,
    val entities: List<VoiceEntity> = emptyList(),
    val parameters: Map<String, String> = emptyMap()
)

data class VoiceEntity(
    val id: String,
    val type: EntityType,
    val value: String,
    val confidence: Double,
    val startIndex: Int,
    val endIndex: Int
)

data class Language(
    val id: String,
    val code: String, // ISO 639-1 code (e.g., "en", "hi")
    val name: String,
    val nativeName: String,
    val isSupported: Boolean = true
)

data class SpeechSettings(
    val id: String,
    val language: String,
    val voice: String? = null,
    val speed: Double = 1.0, // 0.5 to 2.0
    val pitch: Double = 1.0, // 0.5 to 2.0
    val volume: Double = 1.0 // 0.0 to 1.0
)

data class ConversationSession(
    val id: String,
    val userId: String,
    val startTime: String,
    val endTime: String? = null,
    val commands: List<VoiceCommand> = emptyList(),
    val responses: List<VoiceResponse> = emptyList(),
    val language: String
)

enum class IntentType {
    WEATHER_QUERY,
    DISEASE_DETECTION,
    PRICE_QUERY,
    SCHEME_QUERY,
    IRRIGATION_ADVICE,
    GENERAL_QUERY,
    UNKNOWN
}

enum class EntityType {
    CROP_NAME,
    LOCATION,
    DATE,
    QUANTITY,
    DISEASE_NAME,
    FERTILIZER_NAME,
    UNKNOWN
}

object SupportedLanguages {
    val languages = listOf(
        Language("1", "en", "English", "English"),
        Language("2", "hi", "Hindi", "हिंदी"),
        Language("3", "bn", "Bengali", "বাংলা"),
        Language("4", "te", "Telugu", "తెలుగు"),
        Language("5", "mr", "Marathi", "मराठी"),
        Language("6", "ta", "Tamil", "தமிழ்"),
        Language("7", "gu", "Gujarati", "ગુજરાતી"),
        Language("8", "kn", "Kannada", "ಕನ್ನಡ"),
        Language("9", "ml", "Malayalam", "മലയാളം"),
        Language("10", "pa", "Punjabi", "ਪੰਜਾਬੀ"),
        Language("11", "or", "Odia", "ଓଡ଼ିଆ"),
        Language("12", "as", "Assamese", "অসমীয়া")
    )
}
