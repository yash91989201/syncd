package com.example.syncd

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.example.syncd.di.initializeKoin
import com.example.syncd.utils.LocaleManager
import org.koin.android.ext.koin.androidContext

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(androidContext = this@MyApplication)
        }
    }
    
    override fun attachBaseContext(base: Context) {
        val languageCode = LocaleManager.getSavedLanguage(base)
        val context = LocaleManager.applyLocaleToContext(base, languageCode)
        super.attachBaseContext(context)
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val languageCode = LocaleManager.getSavedLanguage(this)
        LocaleManager.applyLocaleToContext(this, languageCode)
    }
}