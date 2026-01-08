package com.example.meditox.services

import com.example.meditox.models.subscription.SubscriptionSyncRequest
import com.example.meditox.models.subscription.SubscriptionSyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Path

interface SubscriptionApiService {
    
    @POST("/api/v2/subscription/sync")
    suspend fun syncSubscriptionDetails(
        @Body request: SubscriptionSyncRequest
    ): Response<SubscriptionSyncResponse>
    
    @POST("api/subscription/update-status")
    suspend fun updateSubscriptionStatus(
        @Body request: Map<String, Any>
    ): Response<SubscriptionSyncResponse>
    
    @POST("api/v2/subscription/cancel/{ID}")
    suspend fun cancelSubscription(
        @Path("ID") ID: String
    ): Response<SubscriptionSyncResponse>
}