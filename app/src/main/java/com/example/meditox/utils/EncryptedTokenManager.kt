package com.example.meditox.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object EncryptedTokenManager {

    private const val FILE_NAME = "secured_pref"
    private  const val ACCESS_TOKEN_KEY = "access_token"
    private  const val REFRESH_TOKEN_KEY = "refresh_token"


    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences{
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build();
        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM

        )
    }

    fun saveAccessAndRefreshToken(context: Context,accessToken: String, refreshToken: String){
        val pref = getEncryptedSharedPreferences(context)
        pref.edit()
            .putString(ACCESS_TOKEN_KEY,accessToken)
            .putString(REFRESH_TOKEN_KEY,refreshToken)
            .apply()
    }

    fun saveAccessToken(context: Context,accessToken: String){
        val pref = getEncryptedSharedPreferences(context)
        pref.edit()
            .putString(ACCESS_TOKEN_KEY,accessToken)
            .apply()
    }

    fun saveRefreshToken(context: Context,refreshToken: String){
        val pref = getEncryptedSharedPreferences(context)
        pref.edit()
            .putString(REFRESH_TOKEN_KEY,refreshToken)
            .apply()
    }

    fun getAccessToken(context: Context):String?{
        return getEncryptedSharedPreferences(context).getString(ACCESS_TOKEN_KEY,null)
    }

    fun getRefreshToken(context: Context):String?{
        return getEncryptedSharedPreferences(context).getString(REFRESH_TOKEN_KEY,null)
    }

    fun clearToken(context: Context) {
        getEncryptedSharedPreferences(context).edit().clear().apply()
    }

}