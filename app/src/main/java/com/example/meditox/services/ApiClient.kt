package com.example.meditox.services


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
   private const val Base_Url = "http://34.55.116.151:30080/"
//    private const val Base_Url = "http://0.0.0.0:8080/"

    val userApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Base_Url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)

    }
}