package com.example.meditox.models.subscription

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionApiResponse(
    val subscriptionId: String,
    val planId: String,
    val planName: String,
    val amount: String?, // Can be null as shown in the API response
    val currency: String,
    val status: String,
    val startDate: Long,
    val endDate: Long,
    val autoRenewal: Boolean,
    val razorpaySubscriptionId: String,
    val customerId: String?,
    val userId: String,
    val isActive: Boolean
)

@Serializable
data class SubscriptionApiData(
    val subscriptionId: String,
    val planId: String,
    val planName: String,
    val amount: String?, // Can be null as shown in the API response
    val currency: String,
    val status: String,
    val startDate: Long,
    val endDate: Long,
    val autoRenewal: Boolean,
    val razorpaySubscriptionId: String,
    val customerId: String?,
    val userId: String,
    val isActive: Boolean
)
// Add this extension function
fun SubscriptionApiResponse.toSubscriptionDetails(): SubscriptionDetails {
    return SubscriptionDetails(
        subscriptionId = subscriptionId,
        planId = planId,
        planName = planName,
        amount = amount ?: "0",
        currency = currency,
        status = status,
        startDate = startDate,
        endDate = endDate,
        razorpayPaymentId = razorpaySubscriptionId,
        isActive = isActive,
        autoRenew = autoRenewal
    )
}
fun SubscriptionApiData.toSubscriptionDetails(): SubscriptionDetails {
    return SubscriptionDetails(
        subscriptionId = subscriptionId,
        planId = planId,
        planName = planName,
        amount = amount ?: "0",
        currency = currency,
        status = status,
        startDate = startDate,
        endDate = endDate,
        razorpayPaymentId = razorpaySubscriptionId,
        isActive = isActive,
        autoRenew = autoRenewal
    )
}
