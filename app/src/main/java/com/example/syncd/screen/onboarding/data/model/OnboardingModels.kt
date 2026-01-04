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

// --- Update (partial) models ---

@Serializable
data class OnboardingUpdateInput(
    val userProfile: UserProfileUpdate? = null,
    val healthCondition: HealthConditionUpdate? = null,
    val cycleProfile: CycleProfileUpdate? = null,
    val athleteProfile: AthleteProfileUpdate? = null
)

@Serializable
data class OnboardingUpdateRequest(
    val json: OnboardingUpdateInput
)

@Serializable
data class UserProfileUpdate(
    val ageGroup: String? = null,
    val cycleStage: String? = null,
    val isAthlete: Boolean? = null,
    val physicalActivity: String? = null
)

@Serializable
data class HealthConditionUpdate(
    val condition: String? = null,
    val medication: String? = null
)

@Serializable
data class CycleProfileUpdate(
    val cycleLength: String? = null,
    val bleedingDays: String? = null,
    val flowIntensity: String? = null,
    val painLevel: String? = null,
    val lastPeriod: String? = null
)

@Serializable
data class AthleteProfileUpdate(
    val trainingFrequency: String? = null,
    val sport: String? = null
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
data class OnboardingGetPayload(
    val userProfile: UserProfile? = null,
    val healthCondition: HealthCondition? = null,
    val cycleProfile: CycleProfile? = null,
    val athleteProfile: AthleteProfile? = null
)

@Serializable
data class OnboardingGetResponse(
    val json: OnboardingGetPayload
)

