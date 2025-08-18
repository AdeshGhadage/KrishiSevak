package com.example.krishisevak.data.remote.api

import com.example.krishisevak.data.models.ChatRequestDto
import com.example.krishisevak.data.models.ChatResponseDto
import com.example.krishisevak.data.models.HealthResponse
import com.example.krishisevak.data.models.ImageClassifyResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming

interface BackendApiService {

	@GET("v1/health")
	suspend fun health(): HealthResponse

	@POST("v1/chat")
	suspend fun chat(
		@Body request: ChatRequestDto
	): ChatResponseDto

	@Multipart
	@POST("v1/image/classify")
	suspend fun classifyImage(
		@Part file: MultipartBody.Part
	): ImageClassifyResponseDto

	@Multipart
	@POST("v1/voice")
	@Streaming
	suspend fun voiceToChat(
		@Part("session_id") sessionId: RequestBody,
		@Part("tts") tts: RequestBody,
		@Part("language") language: RequestBody?,
		@Part audio: MultipartBody.Part
	): Response<ResponseBody>

	@FormUrlEncoded
	@POST("v1/tts")
	@Streaming
	suspend fun tts(
		@Field("text") text: String,
		@Field("language") language: String? = null
	): Response<ResponseBody>
}


