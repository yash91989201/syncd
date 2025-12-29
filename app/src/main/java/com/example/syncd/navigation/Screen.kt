package com.example.syncd.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    object Initial: Screen()

    @Serializable
    object Welcome: Screen()

    @Serializable
    object Intro: Screen()

    @Serializable
    object Login:Screen()

    @Serializable
    data class OTP(val phoneNumber: String): Screen()

    @Serializable
    object Onboarding:Screen()

    @Serializable
    object Home:Screen()

    @Serializable
    object Log:Screen()

    @Serializable
    object TodayGuide:Screen()

    @Serializable
    object Insights: Screen()
}
