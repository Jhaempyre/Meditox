package com.example.meditox.services


import android.content.Context
import com.example.meditox.utils.HTTPsTokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val Base_Url = "http://api.meditox.in:30085/"
    //private const val Base_Url = "http://0.0.0.0:8080/"


    // This function creates the Retrofit instance with an OkHttpClient and adds an interceptor
    private fun create(context: Context): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(HTTPsTokenInterceptor(context)) // Add the interceptor
            .addInterceptor(interceptor) // Add logging
            .build()
            
        val gson = com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.time.LocalDateTime::class.java, com.example.meditox.utils.GsonLocalDateTimeAdapter())
            .create()

        return Retrofit.Builder()
            .baseUrl(Base_Url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    private fun createWithoutInterceptor(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        
        val client = OkHttpClient.Builder()
            // No auth interceptor, but add logging
            .addInterceptor(interceptor)
            .build()
            
        val gson = com.google.gson.GsonBuilder()
             .registerTypeAdapter(java.time.LocalDateTime::class.java, com.example.meditox.utils.GsonLocalDateTimeAdapter())
             .create()

        return Retrofit.Builder()
            .baseUrl(Base_Url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    // Auth API service WITHOUT interceptor (for token refresh)
    fun createAuthApiServiceWithoutInterceptor(context: Context): AuthApiService {
        return createWithoutInterceptor()
            .create(AuthApiService::class.java)
    }

    // Lazy initialization of the ApiService
    fun createUserApiService(context: Context): AuthApiService {
       return create(context.applicationContext)
           .create(AuthApiService::class.java)
    }

    // Create subscription API service
    fun createSubscriptionApiService(context: Context): SubscriptionApiService {
        return create(context.applicationContext)
            .create(SubscriptionApiService::class.java)
    }

    // Create global data sync API service (no auth needed for global data)
    fun createGlobalDataSyncApiService(): GlobalDataSyncApiService {
        return createWithoutInterceptor()
            .create(GlobalDataSyncApiService::class.java)
    }

    // Create chemist product API service (auth required)
    fun createChemistProductApiService(context: Context): ChemistProductApiService {
        return create(context.applicationContext)
            .create(ChemistProductApiService::class.java)
    }

}






