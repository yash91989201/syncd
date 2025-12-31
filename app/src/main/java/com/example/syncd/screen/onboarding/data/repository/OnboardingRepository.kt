package com.example.syncd.screen.onboarding.data.repository

import com.example.syncd.network.ApiConfig
import com.example.syncd.screen.onboarding.data.model.OnboardingRequest
import com.example.syncd.screen.onboarding.data.model.OnboardingResponse
import com.example.syncd.screen.onboarding.data.model.OnboardingStatusResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class OnboardingRepository(
    private val httpClient: HttpClient
) {
    suspend fun completeOnboarding(request: OnboardingRequest): Result<OnboardingResponse> {
        return runCatching {
            httpClient.post(ApiConfig.Onboarding.COMPLETE) {
                setBody(request)
            }.body<OnboardingResponse>()
        }
    }

    suspend fun isOnboardingComplete(): Result<OnboardingStatusResponse> {
        return runCatching {
            httpClient.post(ApiConfig.Onboarding.IS_COMPLETE).body<OnboardingStatusResponse>()
        }
    }
}
