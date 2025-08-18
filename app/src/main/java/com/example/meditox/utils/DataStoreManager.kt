package com.example.meditox.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {
    private const val PREFERENCE_NAME="Meditox_prefs"
    private val Context.datastore by preferencesDataStore(PREFERENCE_NAME)
    private val Phone = stringPreferencesKey("phone")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val IS_REGISTERED = booleanPreferencesKey("is_registered")


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
        context.datastore.edit { prefs ->
            prefs[IS_LOGGED_IN] = value
        }
    }

    suspend fun setIsRegistered(context: Context, value: Boolean) {
        context.datastore.edit { prefs ->
            prefs[IS_REGISTERED] = value
        }
    }

    fun isLoggedIn(context: Context): Flow<Boolean> {
        return context.datastore.data.map { prefs ->
            prefs[IS_LOGGED_IN] ?: false
        }
    }

    fun isRegistered(context: Context): Flow<Boolean> {
        return context.datastore.data.map { prefs ->
            prefs[IS_REGISTERED] ?: false
        }
    }


}