package com.example.syncd.screen.onboarding.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OnboardingInput(
    val userProfile: UserProfile,
    val healthCondition: HealthCondition,
    val cycleProfile: CycleProfile,
    val athleteProfile: AthleteProfile? = null
)

@Serializable
data class OnboardingRequest(
    val json: OnboardingInput
)

@Serializable
data class UserProfile(
    val ageGroup: String,
    val cycleStage: String,
    val isAthlete: Boolean,
    val physicalActivity: String? = null
)

@Serializable
data class HealthCondition(
    val condition: String,
    val medication: String
)

@Serializable
data class CycleProfile(
    val cycleLength: String,
    val bleedingDays: String,
    val flowIntensity: String,
    val painLevel: String,
    val lastPeriod: String
)

@Serializable
data class AthleteProfile(
    val trainingFrequency: String,
    val sport: String
)

@Serializable
data class OnboardingResponseData(
    val success: Boolean? = null,
    val message: String? = null
)

@Serializable
data class OnboardingResponse(
    val json: OnboardingResponseData
)

@Serializable
data class OnboardingStatusData(
    val complete: Boolean
)

@Serializable
data class OnboardingStatusResponse(
    val json: OnboardingStatusData
)

