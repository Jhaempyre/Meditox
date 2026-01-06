package com.example.meditox.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.first

object DataStoreDebugger {
    suspend fun logAllStates(context: Context, tag: String = "DataStoreDebugger") {
        try {
            val isLoggedIn = DataStoreManager.isLoggedIn(context).first()
            val isRegistered = DataStoreManager.isRegistered(context).first()
            val isBusinessRegistered = DataStoreManager.isBusinessRegistered(context).first()
            val userData = DataStoreManager.getUserData(context).first()
            val shopDetails = DataStoreManager.getShopDetails(context).first()
            val subscriptionDetails = DataStoreManager.getSubscriptionDetails(context).first()

            Log.d(tag, "=== DataStore State ===")
            Log.d(tag, "isLoggedIn: $isLoggedIn")
            Log.d(tag, "isRegistered: $isRegistered")
            Log.d(tag, "isBusinessRegistered: $isBusinessRegistered")
            Log.d(tag, "User ID: ${userData?.id}")
            Log.d(tag, "User Name: ${userData?.name}")
            Log.d(tag, "Shop Name: ${shopDetails?.shopName}")
            Log.d(tag, "Subscription: ${subscriptionDetails?.subscriptionId}")
            Log.d(tag, "=====================")
        } catch (e: Exception) {
            Log.e(tag, "Error reading DataStore: ${e.message}", e)
        }
    }
}