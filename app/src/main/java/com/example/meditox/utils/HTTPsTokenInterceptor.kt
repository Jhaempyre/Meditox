package com.example.meditox.utils

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class HTTPsTokenInterceptor(private val context: Context): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response{
        val request = chain.request()
        val builder = request.newBuilder()

        //getting the accestokens
        val accessToken = EncryptedTokenManager.getAccessToken(context)

        if (!accessToken.isNullOrEmpty()) {
            builder.addHeader("Authorization", "Bearer $accessToken")
        }
        return chain.proceed(builder.build())
    }

}