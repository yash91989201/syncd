package com.example.syncd.screen.home.data.repository

import com.example.syncd.network.ApiConfig
import com.example.syncd.screen.home.data.model.GetPhaseInfoResponse
import com.example.syncd.screen.home.data.model.PhaseInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post

class HomeRepository(
    private val httpClient: HttpClient
) {
    suspend fun getPhaseInfo(): Result<PhaseInfo?> {
        return runCatching {
            val response = httpClient.post(ApiConfig.Cycle.GET_PHASE_INFO)
                .body<GetPhaseInfoResponse>()
            response.json
        }
    }
}
