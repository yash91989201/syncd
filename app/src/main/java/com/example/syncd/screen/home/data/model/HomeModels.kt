package com.example.syncd.screen.home.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GetPhaseInfoResponse(
    val json: PhaseInfo?
)

@Serializable
data class PhaseInfo(
    val phase: String,
    val dayOfCycle: Int,
    val cycleLength: Int,
    val daysUntilNextPeriod: Int
)
