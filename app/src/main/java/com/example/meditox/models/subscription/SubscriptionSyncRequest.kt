package com.example.meditox.models.subscription

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionSyncRequest(
    val subscriptionId: String,
    val businessId: String, // From shop details
    val planId: String,
    val planName: String,
    val amount: String,
    val currency: String,
    val status: String,
    val startDate: Long,
    val endDate: Long?,
    val razorpayPaymentId: String,
    val isActive: Boolean,
    val autoRenew: Boolean,
    val createdAt: Long,
    val lastUpdated: Long,
    // Additional fields for backend tracking
    val paymentMethod: String = "razorpay",
    val appVersion: String = "1.0",
    val platform: String = "android"
)

@Serializable
data class SubscriptionSyncResponse(
    val success: Boolean,
    val message: String,
    val subscriptionId: String? = null,
    val backendSubscriptionId: String? = null, // Backend's internal ID
    val status: String? = null,
    val syncedAt: Long? = null
)