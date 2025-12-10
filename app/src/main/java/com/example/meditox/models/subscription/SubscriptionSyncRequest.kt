package com.example.meditox.models.subscription

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.example.meditox.utils.LocalDateTimeDeserializer
data class SubscriptionSyncRequest(
    @SerializedName("subscriptionId")
    val subscriptionId: String,
    @SerializedName("businessId")
    val businessId: String, // From shop details
    @SerializedName("planId")
    val planId: String,
    @SerializedName("planName")
    val planName: String,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("startDate")
    val startDate: Long,
    @SerializedName("endDate")
    val endDate: Long?,
    @SerializedName("razorpayPaymentId")
    val razorpayPaymentId: String,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("autoRenew")
    val autoRenew: Boolean,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("lastUpdated")
    val lastUpdated: Long,
    // Additional fields for backend tracking
    @SerializedName("paymentMethod")
    val paymentMethod: String = "razorpay",
    @SerializedName("appVersion")
    val appVersion: String = "1.0",
    @SerializedName("platform")
    val platform: String = "android"
)

data class SubscriptionSyncResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("subscriptionId")
    val subscriptionId: String? = null,
    @SerializedName("backendSubscriptionId")
    val backendSubscriptionId: String? = null, // Backend's internal ID
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("syncedAt")
    @JsonAdapter(LocalDateTimeDeserializer::class)
    val syncedAt: String? = null  // Handles both array and string formats from backend
)