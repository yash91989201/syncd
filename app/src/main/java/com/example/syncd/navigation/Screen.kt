package com.example.syncd.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    object Welcome

    @Serializable
    object Intro

    @Serializable
    object Login

    @Serializable
    data class OTP(val phoneNumber: String)

    @Serializable
    object Onboarding

    @Serializable
    object Home
}
