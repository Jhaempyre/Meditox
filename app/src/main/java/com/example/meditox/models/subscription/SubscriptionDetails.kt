package com.example.meditox.models.subscription

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionDetails(
    val subscriptionId: String,
    val planId: String,
    val planName: String,
    val amount: String, // Amount paid (e.g., "₹299")
    val currency: String = "INR",
    val status: String, // "active", "created", "cancelled", etc.
    val startDate: Long, // Timestamp when subscription started
    val endDate: Long? = null, // Timestamp when subscription ends (if applicable)
    val razorpayPaymentId: String, // Payment ID from Razorpay
    val isActive: Boolean = true,
    val autoRenew: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class SubscriptionPlanInfo(
    val planId: String,
    val planName: String,
    val price: String,
    val duration: String,
    val features: List<String>
)