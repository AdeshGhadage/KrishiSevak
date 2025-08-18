package com.example.krishisevak.data.remote

import com.example.krishisevak.data.remote.api.WeatherApiService
import com.example.krishisevak.data.remote.api.BackendApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    // For Android Emulator talking to host machine backend, use 10.0.2.2
    // For physical device on same LAN, replace with your machine IP, e.g. http://192.168.1.10:8000/
    private const val BACKEND_BASE_URL = "http://10.0.2.2:8000/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl(WEATHER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val weatherApiService: WeatherApiService = weatherRetrofit.create(WeatherApiService::class.java)
    
    // Add more API services here as needed
    // For example: Price API, Government Schemes API, etc.

    private val backendRetrofit = Retrofit.Builder()
        .baseUrl(BACKEND_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val backendApiService: BackendApiService = backendRetrofit.create(BackendApiService::class.java)
}
