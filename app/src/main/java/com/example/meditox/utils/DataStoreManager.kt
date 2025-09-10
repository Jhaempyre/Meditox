package com.example.meditox.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.meditox.models.ShopDetails
import com.example.meditox.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

















    // Add this function to clear all data for testing
    suspend fun clearAll(context: Context) {
        Log.d("DataStore", "Clearing all DataStore data")
        context.datastore.edit { prefs ->
            prefs.clear()
        }
    }
}