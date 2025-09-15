package com.example.meditox.utils

import com.example.meditox.models.subscription.SubscriptionResponse

/**
 * Singleton object to temporarily hold subscription data during the payment flow
 * This bridges the gap between subscription creation and payment success
 */
object SubscriptionDataHolder {
    private var currentSubscriptionResponse: SubscriptionResponse? = null
    private var currentPlanId: String? = null
    private var currentPlanName: String? = null
    private var currentPlanPrice: String? = null
    private var currentPlanDuration: String? = null
    
    /**
     * Store subscription data when subscription is created
     */
    fun storeSubscriptionData(
        subscriptionResponse: SubscriptionResponse,
        planId: String,
        planName: String,
        planPrice: String,
        planDuration: String
    ) {
        currentSubscriptionResponse = subscriptionResponse
        currentPlanId = planId
        currentPlanName = planName
        currentPlanPrice = planPrice
        currentPlanDuration = planDuration
    }
    
    /**
     * Get stored subscription response
     */
    fun getSubscriptionResponse(): SubscriptionResponse? = currentSubscriptionResponse
    
    /**
     * Get stored plan details
     */
    fun getPlanId(): String? = currentPlanId
    fun getPlanName(): String? = currentPlanName
    fun getPlanPrice(): String? = currentPlanPrice
    fun getPlanDuration(): String? = currentPlanDuration
    
    /**
     * Clear stored data after payment is complete
     */
    fun clearData() {
        currentSubscriptionResponse = null
        currentPlanId = null
        currentPlanName = null
        currentPlanPrice = null
        currentPlanDuration = null
    }
    
    /**
     * Check if we have valid subscription data
     */
    fun hasValidData(): Boolean {
        return currentSubscriptionResponse != null && 
               currentPlanId != null && 
               currentPlanName != null && 
               currentPlanPrice != null
    }
}