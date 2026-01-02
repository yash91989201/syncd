package com.example.syncd.screen.log.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyLogCreateRequest(
    val json: DailyLogCreateInput
)

@Serializable
data class DailyLogCreateInput(
    val painLevel: String? = null,
    val energyLevel: String? = null,
    val mood: String? = null,
    val flow: String? = null,
    val stressLevel: String? = null,
    val note: String? = null
)

@Serializable
data class DailyLogUpdateRequest(
    val json: DailyLogUpdateInput
)

@Serializable
data class DailyLogUpdateInput(
    val id: String,
    val data: DailyLogUpdateData
)

@Serializable
data class DailyLogUpdateData(
    val painLevel: String? = null,
    val energyLevel: String? = null,
    val mood: String? = null,
    val flow: String? = null,
    val stressLevel: String? = null,
    val note: String? = null
)

@Serializable
data class DailyLogListRequest(
    val json: DailyLogListInput
)

@Serializable
data class DailyLogListInput(
    val filter: DailyLogFilter? = null,
    val sort: DailyLogSort = DailyLogSort()
)

@Serializable
data class DailyLogFilter(
    val date: String? = null
)

@Serializable
data class DailyLogSort(
    val column: String = "createdAt",
    val order: String = "desc"
)

@Serializable
data class DailyLogCreateResponse(
    val json: DailyLogEntry
)

@Serializable
data class DailyLogUpdateResponse(
    val json: DailyLogEntry
)

@Serializable
data class DailyLogListResponse(
    val json: List<DailyLogEntry>
)

@Serializable
data class DailyLogEntry(
    val id: String,
    val painLevel: String? = null,
    val energyLevel: String? = null,
    val mood: String? = null,
    val flow: String? = null,
    val stressLevel: String? = null,
    val note: String? = null,
    val userId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
