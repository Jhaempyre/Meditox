package com.example.meditox.utils

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.example.meditox.models.subscription.SubscriptionDetails
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for subscription-related operations
 */
object SubscriptionHelper {
    
    /**
     * Get current subscription details
     */
    fun getSubscriptionDetails(context: Context): Flow<SubscriptionDetails?> {
        return DataStoreManager.getSubscriptionDetails(context)
    }
    
    /**
     * Check if user has active subscription
     */
    fun hasActiveSubscription(context: Context): Flow<Boolean> {
        return DataStoreManager.hasActiveSubscription(context)
    }
    
    /**
     * Get active plan name
     */
    fun getActivePlanName(context: Context): Flow<String?> {
        return DataStoreManager.getActivePlanName(context)
    }
    
    /**
     * Check if subscription is expired (suspend function)
     */
    suspend fun isSubscriptionExpired(context: Context): Boolean {
        return DataStoreManager.isSubscriptionExpired(context)
    }
    
    /**
     * Get subscription status as a readable string
     */
    suspend fun getSubscriptionStatus(context: Context): String {
        val details = getSubscriptionDetails(context).first()
        return when {
            details == null -> "No Subscription"
            !details.isActive -> "Inactive"
            isSubscriptionExpired(context) -> "Expired"
            else -> "Active"
        }
    }
    
    /**
     * Get days remaining in subscription
     */
    suspend fun getDaysRemaining(context: Context): Int {
        val details = getSubscriptionDetails(context).first()
        return if (details?.endDate != null) {
            val currentTime = System.currentTimeMillis()
            val timeDiff = details.endDate - currentTime
            (timeDiff / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
        } else {
            0
        }
    }
    
    /**
     * Format subscription start date
     */
    suspend fun getFormattedStartDate(context: Context): String {
        val details = getSubscriptionDetails(context).first()
        return if (details != null) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(details.startDate))
        } else {
            "N/A"
        }
    }
    
    /**
     * Format subscription end date
     */
    suspend fun getFormattedEndDate(context: Context): String {
        val details = getSubscriptionDetails(context).first()
        return if (details?.endDate != null) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(details.endDate))
        } else {
            "N/A"
        }
    }
    
    /**
     * Cancel subscription (update status to inactive)
     */
    suspend fun cancelSubscription(context: Context) {
        DataStoreManager.updateSubscriptionStatus(context, false, "cancelled")
    }
    
    /**
     * Clear all subscription data
     */
    suspend fun clearSubscriptionData(context: Context) {
        DataStoreManager.clearSubscriptionData(context)
    }
    
    /**
     * Get subscription summary for display
     */
    suspend fun getSubscriptionSummary(context: Context): SubscriptionSummary {
        val details = getSubscriptionDetails(context).first()
        return if (details != null) {
            SubscriptionSummary(
                planName = details.planName,
                amount = details.amount,
                status = getSubscriptionStatus(context),
                startDate = getFormattedStartDate(context),
                endDate = getFormattedEndDate(context),
                daysRemaining = getDaysRemaining(context),
                isActive = details.isActive && !isSubscriptionExpired(context)
            )
        } else {
            SubscriptionSummary()
        }
    }
}

/**
 * Data class for subscription summary
 */
data class SubscriptionSummary(
    val planName: String = "No Plan",
    val amount: String = "₹0",
    val status: String = "No Subscription",
    val startDate: String = "N/A",
    val endDate: String = "N/A",
    val daysRemaining: Int = 0,
    val isActive: Boolean = false
)