package com.example.syncd

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.syncd.auth.presentation.AuthViewModel
import com.example.syncd.composables.navbar.NavBar
import com.example.syncd.composables.navbar.shouldShowNavBar
import com.example.syncd.navigation.Navigator
import com.example.syncd.navigation.Screen
import com.example.syncd.ui.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    AppTheme {
        val navigator = koinInject<Navigator>()
        val authViewModel = koinInject<AuthViewModel>()
        val entryProvider = koinEntryProvider()
        val currentScreen = navigator.backStack.lastOrNull()
        val showNavBar = shouldShowNavBar(currentScreen)
        
        val authState by authViewModel.uiState.collectAsStateWithLifecycle()
        
        // Handle navigation after splash screen session check completes
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

        Scaffold(
            bottomBar = {
                if (showNavBar) {
                    NavBar(
                        currentScreen = currentScreen,
                        navigator = navigator
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = { navigator.goBack() },
                    entryProvider = entryProvider,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    )
                )
            }
        }
    }
}


