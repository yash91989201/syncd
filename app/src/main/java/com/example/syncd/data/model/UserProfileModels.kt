package com.example.syncd.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val userId: String,
    val ageGroup: AgeGroup,
    val cycleStage: CycleStage,
    val isAthlete: Boolean,
    val physicalActivity: PhysicalActivity?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class AthleteProfile(
    val userId: String,
    val trainingFrequency: TrainingFrequency?,
    val sport: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CompleteUserProfile(
    val userProfile: UserProfile,
    val athleteProfile: AthleteProfile?
)

enum class AgeGroup {
    UNDER_18,
    AGE_18_24,
    AGE_25_34;

    companion object {
        fun fromString(value: String): AgeGroup {
            return when (value) {
                "under_18" -> UNDER_18
                "18_24" -> AGE_18_24
                "25_34" -> AGE_25_34
                else -> AGE_18_24
            }
        }
    }
}

enum class CycleStage {
    REGULAR,
    IRREGULAR,
    PREGNANT,
    TRYING_TO_CONCEIVE,
    PERIMENOPAUSE,
    POSTPARTUM;

    companion object {
        fun fromString(value: String): CycleStage {
            return when (value) {
                "regular" -> REGULAR
                "irregular" -> IRREGULAR
                "pregnant" -> PREGNANT
                "trying_to_conceive" -> TRYING_TO_CONCEIVE
                "perimenopause" -> PERIMENOPAUSE
                "postpartum" -> POSTPARTUM
                else -> REGULAR
            }
        }
    }
}

enum class PhysicalActivity {
    DAILY_RUNNING,
    GYM_FITNESS,
    WALKING,
    YOGA,
    NONE;

    companion object {
        fun fromString(value: String?): PhysicalActivity? {
            return when (value) {
                "daily_running" -> DAILY_RUNNING
                "gym_fitness" -> GYM_FITNESS
                "walking" -> WALKING
                "yoga" -> YOGA
                "none" -> NONE
                null -> null
                else -> null
            }
        }
    }
}

enum class TrainingFrequency {
    ONE_TWO_PER_WEEK,
    THREE_FOUR_PER_WEEK,
    FIVE_SIX_PER_WEEK,
    DAILY,
    TWICE_DAILY;

    companion object {
        fun fromString(value: String?): TrainingFrequency? {
            return when (value) {
                "1_2_per_week" -> ONE_TWO_PER_WEEK
                "3_4_per_week" -> THREE_FOUR_PER_WEEK
                "5_6_per_week" -> FIVE_SIX_PER_WEEK
                "daily" -> DAILY
                "twice_daily" -> TWICE_DAILY
                null -> null
                else -> null
            }
        }
    }
}
