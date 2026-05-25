package com.example.nebulagourmet.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // ESTA ES LA CONFIGURACIÓN DEFINITIVA:
    // Al usar el nombre del PC + .local, el celular lo encontrará en cualquier red Wi-Fi
    // sin tener que cambiar la IP nunca más.
    private const val BASE_URL = "http://DESKTOP-KLBFDSB.local:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(ApiService::class.java)
    }
}
