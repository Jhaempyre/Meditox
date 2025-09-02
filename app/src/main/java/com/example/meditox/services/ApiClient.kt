package com.example.meditox.services


import android.content.Context
import com.example.meditox.utils.HTTPsTokenInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object ApiClient {

    private lateinit var context: Context

    private const val Base_Url = "http://35.222.189.87:30080/"
    //private const val Base_Url = "http://0.0.0.0:8080/"

    // Initialize ApiClient with ApplicationContext
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    // This function creates the Retrofit instance with an OkHttpClient and adds an interceptor
    private fun create(): Retrofit {
        if (!this::context.isInitialized) { // Check if context is initialized
            throw IllegalStateException("ApiClient has not been initialized. Call ApiClient.init(context) first.")
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(HTTPsTokenInterceptor(context)) // Add the interceptor
            .build()

        return Retrofit.Builder()
            .baseUrl(Base_Url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // Lazy initialization of the ApiService
    val userApiService: AuthApiService by lazy {
        create() // Now we can create Retrofit since context has been initialized
            .create(AuthApiService::class.java)
    }
}

