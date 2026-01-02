package com.example.syncd.screen.log.data.repository

import com.example.syncd.network.ApiConfig
import com.example.syncd.screen.log.data.model.DailyLogCreateInput
import com.example.syncd.screen.log.data.model.DailyLogCreateRequest
import com.example.syncd.screen.log.data.model.DailyLogCreateResponse
import com.example.syncd.screen.log.data.model.DailyLogEntry
import com.example.syncd.screen.log.data.model.DailyLogFilter
import com.example.syncd.screen.log.data.model.DailyLogListInput
import com.example.syncd.screen.log.data.model.DailyLogListRequest
import com.example.syncd.screen.log.data.model.DailyLogListResponse
import com.example.syncd.screen.log.data.model.DailyLogSort
import com.example.syncd.screen.log.data.model.DailyLogUpdateData
import com.example.syncd.screen.log.data.model.DailyLogUpdateInput
import com.example.syncd.screen.log.data.model.DailyLogUpdateRequest
import com.example.syncd.screen.log.data.model.DailyLogUpdateResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class LogRepository(
    private val httpClient: HttpClient
) {
    suspend fun createLog(
        painLevel: String?,
        energyLevel: String?,
        mood: String?,
        flow: String?,
        note: String?
    ): Result<DailyLogEntry> {
        return runCatching {
            val response = httpClient.post(ApiConfig.DailyLog.CREATE) {
                setBody(
                    DailyLogCreateRequest(
                        json = DailyLogCreateInput(
                            painLevel = painLevel,
                            energyLevel = energyLevel,
                            mood = mood,
                            flow = flow,
                            note = note
                        )
                    )
                )
            }.body<DailyLogCreateResponse>()
            response.json
        }
    }

    suspend fun updateLog(
        id: String,
        painLevel: String?,
        energyLevel: String?,
        mood: String?,
        flow: String?,
        note: String?
    ): Result<DailyLogEntry> {
        return runCatching {
            val response = httpClient.post(ApiConfig.DailyLog.UPDATE) {
                setBody(
                    DailyLogUpdateRequest(
                        json = DailyLogUpdateInput(
                            id = id,
                            data = DailyLogUpdateData(
                                painLevel = painLevel,
                                energyLevel = energyLevel,
                                mood = mood,
                                flow = flow,
                                note = note
                            )
                        )
                    )
                )
            }.body<DailyLogUpdateResponse>()
            response.json
        }
    }

    suspend fun listLogs(date: String? = null): Result<List<DailyLogEntry>> {
        return runCatching {
            val response = httpClient.post(ApiConfig.DailyLog.LIST) {
                setBody(
                    DailyLogListRequest(
                        json = DailyLogListInput(
                            filter = if (date != null) DailyLogFilter(date = date) else null,
                            sort = DailyLogSort(column = "createdAt", order = "desc")
                        )
                    )
                )
            }.body<DailyLogListResponse>()
            response.json
        }
    }
}
