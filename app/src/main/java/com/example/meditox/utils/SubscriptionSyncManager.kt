package com.example.meditox.utils

import android.content.Context
import android.util.Log
import com.example.meditox.models.subscription.SubscriptionDetails
import com.example.meditox.models.subscription.SubscriptionSyncRequest
import com.example.meditox.services.ApiClient
import kotlinx.coroutines.flow.first

/**
 * Manager class for syncing subscription data with backend
 */
object SubscriptionSyncManager {
    
    /**
     * Sync subscription details with backend after successful payment
     */
    suspend fun syncSubscriptionToBackend(
        context: Context,
        subscriptionDetails: SubscriptionDetails
    ): Boolean {
        return try {
            Log.d("SubscriptionSync", "Starting subscription sync to backend")
            
            // Get business ID from shop details
            val shopDetails = DataStoreManager.getShopDetails(context).first()
            val businessId = shopDetails?.id?.toString()
            
            if (businessId == null) {
                Log.e("SubscriptionSync", "No business ID found in shop details")
                Log.e("SubscriptionSync", "Shop details: $shopDetails")
                return false
            }
            
            Log.d("SubscriptionSync", "Found business ID: $businessId")
            Log.d("SubscriptionSync", "Shop details: ${shopDetails?.shopName} (ID: $businessId)")
            
            // Create sync request
            val syncRequest = SubscriptionSyncRequest(
                subscriptionId = subscriptionDetails.subscriptionId,
                businessId = businessId,
                planId = subscriptionDetails.planId,
                planName = subscriptionDetails.planName,
                amount = subscriptionDetails.amount,
                currency = subscriptionDetails.currency,
                status = subscriptionDetails.status,
                startDate = subscriptionDetails.startDate,
                endDate = subscriptionDetails.endDate,
                razorpayPaymentId = subscriptionDetails.razorpayPaymentId,
                isActive = subscriptionDetails.isActive,
                autoRenew = subscriptionDetails.autoRenew,
                createdAt = subscriptionDetails.createdAt,
                lastUpdated = subscriptionDetails.lastUpdated,
                paymentMethod = "razorpay",
                appVersion = "1.0", // You can get this from BuildConfig
                platform = "android"
            )
            
            Log.d("SubscriptionSync", "Sync request created:")
            Log.d("SubscriptionSync", "- Subscription ID: ${syncRequest.subscriptionId}")
            Log.d("SubscriptionSync", "- Business ID: ${syncRequest.businessId}")
            Log.d("SubscriptionSync", "- Plan: ${syncRequest.planName}")
            Log.d("SubscriptionSync", "- Amount: ${syncRequest.amount}")
            Log.d("SubscriptionSync", "- Payment ID: ${syncRequest.razorpayPaymentId}")
            
            // Get API service
            val apiService = ApiClient.createSubscriptionApiService(context)
            
            // Make API call (token will be added automatically by HTTPsTokenInterceptor)
            Log.d("SubscriptionSync", "Making API call to sync subscription")
            val response = apiService.syncSubscriptionDetails(syncRequest)
            
            Log.d("SubscriptionSync", "API Response:")
            Log.d("SubscriptionSync", "- Response code: ${response.code()}")
            Log.d("SubscriptionSync", "- Is successful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("SubscriptionSync", "Sync successful:")
                Log.d("SubscriptionSync", "- Success: ${responseBody?.success}")
                Log.d("SubscriptionSync", "- Message: ${responseBody?.message}")
                Log.d("SubscriptionSync", "- Backend ID: ${responseBody?.backendSubscriptionId}")
                
                // Update local storage with backend subscription ID if provided
                responseBody?.backendSubscriptionId?.let { backendId ->
                    updateLocalSubscriptionWithBackendId(context, subscriptionDetails, backendId)
                }
                
                true
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("SubscriptionSync", "Sync failed with code: ${response.code()}")
                Log.e("SubscriptionSync", "Error body: $errorBody")
                false
            }
            
        } catch (e: Exception) {
            Log.e("SubscriptionSync", "Exception during subscription sync", e)
            Log.e("SubscriptionSync", "Exception type: ${e.javaClass.simpleName}")
            Log.e("SubscriptionSync", "Exception message: ${e.message}")
            false
        }
    }
    
    
    /**
     * Update local subscription with backend subscription ID
     */
    private suspend fun updateLocalSubscriptionWithBackendId(
        context: Context,
        originalDetails: SubscriptionDetails,
        backendId: String
    ) {
        try {
            Log.d("SubscriptionSync", "Updating local subscription with backend ID: $backendId")
            
            // You can add a backendSubscriptionId field to SubscriptionDetails if needed
            // For now, we'll update the lastUpdated timestamp
            val updatedDetails = originalDetails.copy(
                lastUpdated = System.currentTimeMillis()
                // backendSubscriptionId = backendId // Add this field to SubscriptionDetails if needed
            )
            
            DataStoreManager.saveSubscriptionDetails(context, updatedDetails)
            Log.d("SubscriptionSync", "Local subscription updated with backend reference")
            
        } catch (e: Exception) {
            Log.e("SubscriptionSync", "Error updating local subscription: ${e.message}")
        }
    }
    
    /**
     * Retry failed sync operations
     */
    suspend fun retryFailedSync(context: Context): Boolean {
        return try {
            val subscriptionDetails = DataStoreManager.getSubscriptionDetails(context).first()
            if (subscriptionDetails != null) {
                Log.d("SubscriptionSync", "Retrying failed subscription sync")
                syncSubscriptionToBackend(context, subscriptionDetails)
            } else {
                Log.w("SubscriptionSync", "No subscription details found for retry")
                false
            }
        } catch (e: Exception) {
            Log.e("SubscriptionSync", "Error during sync retry: ${e.message}")
            false
        }
    }
}