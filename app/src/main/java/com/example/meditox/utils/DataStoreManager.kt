package com.example.meditox.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.meditox.models.ShopDetails
import com.example.meditox.models.User
import com.example.meditox.models.subscription.SubscriptionDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString


object DataStoreManager {
    private const val PREFERENCE_NAME = "Meditox_prefs"
    private val Context.datastore by preferencesDataStore(PREFERENCE_NAME)
    private val Phone = stringPreferencesKey("phone")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val IS_REGISTERED = booleanPreferencesKey("is_registered")
    private val IS_BUSINESS_REGISTERED = booleanPreferencesKey("is_business_registered")
    private val USER_DATA = stringPreferencesKey("user_data")
    private val SHOP_DETAILS = stringPreferencesKey("shop_details")
    // Subscription related keys
    private val SUBSCRIPTION_DETAILS = stringPreferencesKey("subscription_details")
    private val HAS_ACTIVE_SUBSCRIPTION = booleanPreferencesKey("has_active_subscription")
    private val BACKEND_SYNC_STATUS = booleanPreferencesKey("backend_sync_status")
    val jsonFormatter = Json {
        ignoreUnknownKeys = true // prevents crash if extra fields exist
        encodeDefaults = true
    }


    suspend fun savePhoneNumber(context: Context, phone: String) {
        context.datastore.edit { preferences ->
            preferences[Phone] = phone
        }
    }

    fun getPhoneNumber(context: Context): Flow<String?> {
        return context.datastore.data.map { prefs ->
            prefs[Phone]
        }
    }

    suspend fun setIsLoggedIn(context: Context, value: Boolean) {
        Log.d("DataStore", "Setting isLoggedIn to: $value")
        context.datastore.edit { prefs ->
            prefs[IS_LOGGED_IN] = value
        }
    }

    suspend fun setIsRegistered(context: Context, value: Boolean) {
        Log.d("DataStore", "Setting isRegistered to: $value")
        context.datastore.edit { prefs ->
            prefs[IS_REGISTERED] = value
        }
    }

    suspend fun setIsBusinessRegistered(context: Context, value: Boolean) {
        Log.d("DataStore", "Setting Business registered to: $value")
        context.datastore.edit { prefs ->
            prefs[IS_BUSINESS_REGISTERED] = value
        }
    }

    fun isLoggedIn(context: Context): Flow<Boolean> {
        return context.datastore.data.map { prefs ->
            val value = prefs[IS_LOGGED_IN] ?: false
            Log.d("DataStore", "Reading isLoggedIn: $value (key exists: ${prefs.contains(IS_LOGGED_IN)})")
            value
        }
    }

    fun isRegistered(context: Context): Flow<Boolean> {
        return context.datastore.data.map { prefs ->
            val value = prefs[IS_REGISTERED] ?: false
            Log.d("DataStore", "Reading isRegistered: $value (key exists: ${prefs.contains(IS_REGISTERED)})")
            value
        }
    }

    fun isBusinessRegistered(context: Context): Flow<Boolean> {
        return context.datastore.data.map { prefs ->
            val value = prefs[IS_BUSINESS_REGISTERED] ?: false
            Log.d("DataStore", "Reading isBusinessRegistered: $value (key exists: ${prefs.contains(IS_REGISTERED)})")
            value
        }
    }

    //save user data
    suspend fun saveUserData(context: Context,user:User){
        val json = jsonFormatter.encodeToString(User.serializer(), user)
        Log.d("DataStore", "Saving user data: $json")
        context.datastore.edit { prefs ->
            prefs[USER_DATA] = json
        }
        Log.d("DataStore", "Data saved:")
    }

    fun getUserData(context: Context):Flow<User?>{
        return context.datastore.data.map { prefs ->
            prefs[USER_DATA]?.let{
                json->try{
                jsonFormatter.decodeFromString<User>(User.serializer(), json)
            }catch (e: Exception) {
                Log.e("DataStore", "Error decoding user data: ${e.message}")
                null
            }
            }
        }
    }

    suspend fun saveShopDetails(context: Context,shopDetails:ShopDetails){
        val json = jsonFormatter.encodeToString(ShopDetails.serializer(), shopDetails)
        Log.d("DataStore", "Saving Shop data: $json")
        context.datastore.edit { prefs ->
            prefs[SHOP_DETAILS] = json
        }
        Log.d("DataStore", "Data saved:")
    }

    fun getShopDetails(context: Context):Flow<ShopDetails?>{
        return context.datastore.data.map { prefs ->
            prefs[SHOP_DETAILS]?.let{
                    json->try{
                jsonFormatter.decodeFromString<ShopDetails>(ShopDetails.serializer(), json)
            }catch (e: Exception) {
                Log.e("DataStore", "Error decoding Shop details: ${e.message}")
                null
            }
            }
        }
    }

    // ===== SUBSCRIPTION MANAGEMENT FUNCTIONS =====
    
    /**
     * Save subscription details after successful payment
     */
    suspend fun saveSubscriptionDetails(context: Context, subscriptionDetails: SubscriptionDetails) {
        val json = jsonFormatter.encodeToString(SubscriptionDetails.serializer(), subscriptionDetails)
        Log.d("DataStore", "Saving subscription details: $json")
        context.datastore.edit { prefs ->
            prefs[SUBSCRIPTION_DETAILS] = json
            prefs[HAS_ACTIVE_SUBSCRIPTION] = subscriptionDetails.isActive
        }
        Log.d("DataStore", "Subscription details saved successfully")
    }

    /**
     * Get current subscription details
     */
    fun getSubscriptionDetails(context: Context): Flow<SubscriptionDetails?> {
        return context.datastore.data.map { prefs ->
            prefs[SUBSCRIPTION_DETAILS]?.let { json ->
                try {
                    jsonFormatter.decodeFromString<SubscriptionDetails>(SubscriptionDetails.serializer(), json)
                } catch (e: Exception) {
                    Log.e("DataStore", "Error decoding subscription details: ${e.message}")
                    null
                }
            }
        }
    }

    /**
     * Check if user has an active subscription
     */
    fun hasActiveSubscription(context: Context): Flow<Boolean> {
        return context.datastore.data.map { prefs ->
            val hasActive = prefs[HAS_ACTIVE_SUBSCRIPTION] ?: false
            Log.d("DataStore", "Reading hasActiveSubscription: $hasActive")
            hasActive
        }
    }

    /**
     * Update subscription status (e.g., when it expires or is cancelled)
     */
    suspend fun updateSubscriptionStatus(context: Context, isActive: Boolean, newStatus: String? = null) {
        Log.d("DataStore", "Updating subscription status - isActive: $isActive, newStatus: $newStatus")
        context.datastore.edit { prefs ->
            prefs[HAS_ACTIVE_SUBSCRIPTION] = isActive
            
            // If we have existing subscription details, update them
            prefs[SUBSCRIPTION_DETAILS]?.let { json ->
                try {
                    val currentDetails = jsonFormatter.decodeFromString<SubscriptionDetails>(SubscriptionDetails.serializer(), json)
                    val updatedDetails = currentDetails.copy(
                        isActive = isActive,
                        status = newStatus ?: currentDetails.status,
                        lastUpdated = System.currentTimeMillis()
                    )
                    prefs[SUBSCRIPTION_DETAILS] = jsonFormatter.encodeToString(SubscriptionDetails.serializer(), updatedDetails)
                } catch (e: Exception) {
                    Log.e("DataStore", "Error updating subscription details: ${e.message}")
                }
            }
        }
    }

    /**
     * Clear subscription data (e.g., when subscription is cancelled or expired)
     */
    suspend fun clearSubscriptionData(context: Context) {
        Log.d("DataStore", "Clearing subscription data")
        context.datastore.edit { prefs ->
            prefs.remove(SUBSCRIPTION_DETAILS)
            prefs[HAS_ACTIVE_SUBSCRIPTION] = false
        }
    }

    /**
     * Check if subscription is expired based on stored data
     */
    suspend fun isSubscriptionExpired(context: Context): Boolean {
        return try {
            val details = getSubscriptionDetails(context).first()
            if (details != null && details.endDate != null) {
                System.currentTimeMillis() > details.endDate
            } else {
                false // No end date means it's ongoing
            }
        } catch (e: Exception) {
            Log.e("DataStore", "Error checking subscription expiry: ${e.message}")
            true // Assume expired on error
        }
    }

    /**
     * Get subscription plan name if active
     */
    fun getActivePlanName(context: Context): Flow<String?> {
        return getSubscriptionDetails(context).map { details ->
            if (details?.isActive == true) details.planName else null
        }
    }

    /**
     * Set backend sync status
     */
    suspend fun setBackendSyncStatus(context: Context, isSuccessful: Boolean) {
        Log.d("DataStore", "Setting backend sync status: $isSuccessful")
        context.datastore.edit { prefs ->
            prefs[BACKEND_SYNC_STATUS] = isSuccessful
        }
    }

    /**
     * Get backend sync status
     */
    fun getBackendSyncStatus(context: Context): Flow<Boolean> {
        return context.datastore.data.map { prefs ->
            val status = prefs[BACKEND_SYNC_STATUS] ?: false
            Log.d("DataStore", "Reading backend sync status: $status")
            status
        }
    }

    /**
     * Reset backend sync status (call before new sync attempt)
     */
    suspend fun resetBackendSyncStatus(context: Context) {
        Log.d("DataStore", "Resetting backend sync status")
        context.datastore.edit { prefs ->
            prefs[BACKEND_SYNC_STATUS] = false
        }
    }

















    // Add this function to clear all data for testing
    suspend fun clearAll(context: Context) {
        Log.d("DataStore", "Clearing all DataStore data")
        context.datastore.edit { prefs ->
            prefs.clear()
        }
    }
}