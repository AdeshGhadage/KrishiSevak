package com.example.krishisevak.data.models

import com.google.gson.annotations.SerializedName

data class ChatRequestDto(
	@SerializedName("session_id") val sessionId: String = "default",
	@SerializedName("message") val message: String,
	@SerializedName("stream") val stream: Boolean = false,
	@SerializedName("images_base64") val imagesBase64: List<String>? = null,
	@SerializedName("user_context") val userContext: String? = null
)

data class ChatResponseDto(
	@SerializedName("text") val text: String,
	@SerializedName("tool_calls") val toolCalls: List<Any>? = null,
	@SerializedName("sources") val sources: List<Any>? = null
)

data class HealthResponse(
	@SerializedName("status") val status: String
)

data class ImageClassifyResponseDto(
	@SerializedName("label") val label: String,
	@SerializedName("score") val score: Double,
	@SerializedName("top_k") val topK: List<Any> = emptyList()
)


