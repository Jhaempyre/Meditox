package com.example.meditox

import android.app.Application
import android.util.Log
import androidx.multidex.BuildConfig
import timber.log.Timber

class MeditoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.tag("MeditoxApp").d("Application started")
    }
}
