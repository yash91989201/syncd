package com.example.syncd

import android.app.Application
import com.example.syncd.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(androidContext = this@MyApplication)
        }
    }
}