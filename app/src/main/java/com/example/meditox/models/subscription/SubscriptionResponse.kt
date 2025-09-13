package com.example.meditox.models.subscription

import com.google.gson.annotations.SerializedName

// Razorpay subscription response object
data class SubscriptionResponse(
    val id: String,
    val entity: String,
    @SerializedName("plan_id") val planId: String,
    val status: String,
    @SerializedName("current_start") val currentStart: Long?,
    @SerializedName("current_end") val currentEnd: Long?,
    @SerializedName("ended_at") val endedAt: Long?,
    val quantity: Int,
    val notes: Map<String, String>?,
    @SerializedName("charge_at") val chargeAt: Long,
    @SerializedName("start_at") val startAt: Long,
    @SerializedName("end_at") val endAt: Long,
    @SerializedName("auth_attempts") val authAttempts: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("paid_count") val paidCount: Int,
    @SerializedName("customer_notify") val customerNotify: Boolean,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("expire_by") val expireBy: Long,
    @SerializedName("short_url") val shortUrl: String,
    @SerializedName("has_scheduled_changes") val hasScheduledChanges: Boolean,
    @SerializedName("change_scheduled_at") val changeScheduledAt: Long?,
    val source: String,
    @SerializedName("offer_id") val offerId: String?,
    @SerializedName("remaining_count") val remainingCount: Int
)

