package com.example.meditox.services

import com.example.meditox.models.subscription.SubscriptionSyncRequest
import com.example.meditox.models.subscription.SubscriptionSyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

interface SubscriptionApiService {
    
    @POST("api/subscription/sync")
    suspend fun syncSubscriptionDetails(
        @Body request: SubscriptionSyncRequest
    ): Response<SubscriptionSyncResponse>
    
    @POST("api/subscription/update-status")
    suspend fun updateSubscriptionStatus(
        @Body request: Map<String, Any>
    ): Response<SubscriptionSyncResponse>
    
    @POST("api/subscription/cancel")
    suspend fun cancelSubscription(
        @Body request: Map<String, String>
    ): Response<SubscriptionSyncResponse>
}