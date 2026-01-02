package com.example.syncd

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.syncd.auth.presentation.AuthViewModel
import com.example.syncd.utils.LocaleManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by inject()
    private val localeManager: LocaleManager by inject()
    
    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleManager.getSavedLanguage(newBase)
        val context = LocaleManager.applyLocaleToContext(newBase, languageCode)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        lifecycleScope.launch {
            authViewModel.uiState.collect { state ->
                if (!state.isCheckingSession) {
                    keepSplashScreen = false
                }
            }
        }
        
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}