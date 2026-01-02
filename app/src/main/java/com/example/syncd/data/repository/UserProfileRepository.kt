package com.example.syncd.data.repository

import com.example.syncd.data.model.AthleteProfile
import com.example.syncd.data.model.CompleteUserProfile
import com.example.syncd.data.model.UserProfile
import com.example.syncd.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val userProfile: UserProfileDto?,
    val athleteProfile: AthleteProfileDto?
)

@Serializable
data class UserProfileDto(
    val userId: String,
    val ageGroup: String,
    val cycleStage: String,
    val isAthlete: Boolean,
    val physicalActivity: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class AthleteProfileDto(
    val userId: String,
    val trainingFrequency: String?,
    val sport: String,
    val createdAt: String,
    val updatedAt: String
)

class UserProfileRepository(
    private val httpClient: HttpClient
) {
    suspend fun getUserProfile(): Result<CompleteUserProfile?> {
        return runCatching {
            val response = httpClient.post(ApiConfig.User.PROFILE)
                .body<UserProfileResponse>()
            
            response.userProfile?.let { userDto ->
                val userProfile = UserProfile(
                    userId = userDto.userId,
                    ageGroup = com.example.syncd.data.model.AgeGroup.fromString(userDto.ageGroup),
                    cycleStage = com.example.syncd.data.model.CycleStage.fromString(userDto.cycleStage),
                    isAthlete = userDto.isAthlete,
                    physicalActivity = com.example.syncd.data.model.PhysicalActivity.fromString(userDto.physicalActivity),
                    createdAt = userDto.createdAt,
                    updatedAt = userDto.updatedAt
                )
                
                val athleteProfile = response.athleteProfile?.let { athleteDto ->
                    AthleteProfile(
                        userId = athleteDto.userId,
                        trainingFrequency = com.example.syncd.data.model.TrainingFrequency.fromString(athleteDto.trainingFrequency),
                        sport = athleteDto.sport,
                        createdAt = athleteDto.createdAt,
                        updatedAt = athleteDto.updatedAt
                    )
                }
                
                CompleteUserProfile(
                    userProfile = userProfile,
                    athleteProfile = athleteProfile
                )
            }
        }
    }
}
