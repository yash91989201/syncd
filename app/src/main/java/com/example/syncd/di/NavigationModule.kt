package com.example.syncd.di

import com.example.syncd.navigation.Screen
import com.example.syncd.screen.home.HomeScreen
import com.example.syncd.screen.welcome.WelcomeScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val navigationModule = module {

    navigation<Screen.Home> {
        HomeScreen()
    }

    navigation<Screen.Welcome> {
        WelcomeScreen()
    }
}
