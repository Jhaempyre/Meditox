package com.example.meditox.utils

import android.content.Context
import android.util.Log
import com.example.meditox.models.auth.RefreshTokenRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.services.AuthApiService
import okhttp3.Interceptor
import okhttp3.Response

class HTTPsTokenInterceptor(private val context: Context): Interceptor {

    private val apiService = ApiClient.createUserApiService(context)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        // Getting the access token
        val accessToken = EncryptedTokenManager.getAccessToken(context)

        // Add authorization header if token exists
        if (!accessToken.isNullOrEmpty()) {
            builder.addHeader("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(builder.build())

        // Check if we got 401 or 403 (unauthorized/forbidden)
        if (response.code() == 401 || response.code() == 403) {
            Log.d("HTTPsTokenInterceptor", "Token expired, attempting refresh...")
            response.close()

            // Synchronized block to prevent multiple simultaneous refresh attempts
            synchronized(this) {
                val currentAccessToken = EncryptedTokenManager.getAccessToken(context)

                // Double-check if token is still the same (another thread might have refreshed it)
                if (currentAccessToken == accessToken) {
                    val newAccessToken = refreshAccessToken()

                    if (newAccessToken != null) {
                        Log.d("HTTPsTokenInterceptor", "Token refreshed successfully")
                        // Save the new token
                        EncryptedTokenManager.saveAccessToken(context, newAccessToken)

                        // Retry the original request with new token
                        val newRequestBuilder = request.newBuilder()
                        newRequestBuilder.removeHeader("Authorization")
                        newRequestBuilder.addHeader("Authorization", "Bearer $newAccessToken")
                        val newRequest = newRequestBuilder.build()

                        return chain.proceed(newRequest)
                    } else {
                        Log.e("HTTPsTokenInterceptor", "Failed to refresh token")
                        // Token refresh failed, clear stored tokens and return original response
                        EncryptedTokenManager.clearToken(context)
                        return response
                    }
                } else {
                    Log.d("HTTPsTokenInterceptor", "Token already refreshed by another thread")
                    // Another thread already refreshed the token, retry with new token
                    val updatedToken = EncryptedTokenManager.getAccessToken(context)
                    if (!updatedToken.isNullOrEmpty()) {
                        val newRequestBuilder = request.newBuilder()
                        newRequestBuilder.removeHeader("Authorization")
                        newRequestBuilder.addHeader("Authorization", "Bearer $updatedToken")
                        val newRequest = newRequestBuilder.build()

                        return chain.proceed(newRequest)
                    }
                }
            }
        }

        return response
    }

     private fun refreshAccessToken(): String? {
        return try {
            val refreshToken = EncryptedTokenManager.getRefreshToken(context)

            if (refreshToken.isNullOrEmpty()) {
                Log.e("HTTPsTokenInterceptor", "No refresh token available")
                return null
            }

            Log.d("HTTPsTokenInterceptor", "Attempting to refresh token...")

            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    Log.d("HTTPsTokenInterceptor", "Token refresh API call successful")
                    return apiResponse.data // The new access token
                } else {
                    Log.e("HTTPsTokenInterceptor", "Token refresh failed: ${apiResponse.message}")
                    return null
                }
            } else {
                Log.e("HTTPsTokenInterceptor", "Token refresh API call failed: ${response.code()} ${response.message()}")
                return null
            }
        } catch (e: Exception) {
            Log.e("HTTPsTokenInterceptor", "Exception during token refresh", e)
            null
        }
    }





    //today will implement token interceptor should refresh token anytime when gets a 403 in request

}
