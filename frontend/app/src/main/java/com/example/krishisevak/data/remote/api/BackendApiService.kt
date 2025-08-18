package com.example.krishisevak.data.remote.api

import com.example.krishisevak.data.models.ChatRequestDto
import com.example.krishisevak.data.models.ChatResponseDto
import com.example.krishisevak.data.models.HealthResponse
import com.example.krishisevak.data.models.ImageClassifyResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
}


