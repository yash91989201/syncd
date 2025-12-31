package com.example.syncd.di

import com.example.syncd.navigation.Navigator
import com.example.syncd.navigation.Screen
import com.example.syncd.screen.guide.TodayGuideScreen
import com.example.syncd.screen.home.HomeScreen
import com.example.syncd.screen.insights.InsightsScreen
import com.example.syncd.screen.log.LogScreen
import com.example.syncd.screen.login.LoginScreen
import com.example.syncd.screen.onboarding.OnboardingScreen
import com.example.syncd.screen.otp.OTPScreen
import com.example.syncd.screen.welcome.WelcomeScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val navigationModule = module {
    single { Navigator(Screen.Welcome) }

    navigation<Screen.Home> {
        HomeScreen()
    }

    navigation<Screen.Welcome> {
        WelcomeScreen()
    }

    navigation<Screen.Login> {
        LoginScreen()
    }

    navigation<Screen.OTP> { screen ->
        OTPScreen(phoneNumber = screen.phoneNumber)
    }

    navigation<Screen.Onboarding> {
        OnboardingScreen()
    }

    navigation<Screen.Log> {
        LogScreen()
    }

    navigation<Screen.TodayGuide> {
        TodayGuideScreen()
    }

    navigation<Screen.Insights> {
        InsightsScreen()
    }
}
