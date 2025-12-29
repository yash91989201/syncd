package com.example.syncd.screen.initial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.syncd.auth.presentation.AuthViewModel
import com.example.syncd.navigation.Navigator
import com.example.syncd.navigation.Screen
import org.koin.compose.koinInject

@Composable
fun InitialScreen() {
    val authViewModel: AuthViewModel = koinInject()
    val navigator: Navigator = koinInject()

    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Trigger navigation logic when the state changes
    LaunchedEffect(authState.isCheckingSession, authState.isAuthenticated, authState.hasCompletedOnboarding) {
        if (!authState.isCheckingSession) {
            when {
                !authState.isAuthenticated -> {
                    navigator.setRoot(Screen.Welcome)
                }
                authState.isAuthenticated && !authState.hasCompletedOnboarding -> {
                    navigator.setRoot(Screen.Onboarding)
                }
                else -> {
                    navigator.setRoot(Screen.Home)
                }
            }
        }
    }

    // UI: Just a spinner
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}