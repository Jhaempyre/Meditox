package com.example.meditox.services


import android.content.Context
import com.example.meditox.utils.HTTPsTokenInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object ApiClient {

    private const val Base_Url = "http://35.222.189.87:30085/"
    //private const val Base_Url = "http://0.0.0.0:8080/"


    // This function creates the Retrofit instance with an OkHttpClient and adds an interceptor
    private fun create(context: Context): Retrofit {

        val client = OkHttpClient.Builder()
            .addInterceptor(HTTPsTokenInterceptor(context)) // Add the interceptor
            .build()

        return Retrofit.Builder()
            .baseUrl(Base_Url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private fun createWithoutInterceptor(): Retrofit {
        val client = OkHttpClient.Builder()
            // No interceptor here to avoid infinite recursion
            .build()

        return Retrofit.Builder()
            .baseUrl(Base_Url)
            .addConverterFactory(GsonConverterFactory.create())
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

}







