package com.example.syncd.screen.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DashboardUiState(
    val phaseName: String = "",
    val cycleDay: Int = 1,
    val phaseColor: Color = Color(0xFFE8F5E9), // Default pastel green
    val trainingTitle: String = "",
    val trainingDescription: String = "",
    val trainingIntensity: String = "",
    val nutritionTip: String = "",
    val showSafetyAlert: Boolean = false
)

class HomeViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // In a real app, we would calculate this based on lastPeriodStartDate from DataStore
            // and the current system date.
            // For MVP (dummy data), we'll mock a "Follicular Phase Day 7" scenario.

            // We can read medication status to decide on safety alert
            val isOnMedication = userPreferences.isCycleSetupCompleted.first() // Using this as proxy for now, need to add reader for actual flag if needed, but VM handles logic.
            // Actually let's read the real flag if possible, or default to false for dummy.
            // Since we don't have a direct flow for 'isOnMedication' exposed in UserPreferences (only in save),
            // I'll assume false for the dummy data or we could add a reader.

            // Mock Data for "Follicular Phase"
            _uiState.value = DashboardUiState(
                phaseName = "Follicular Phase",
                cycleDay = 7,
                phaseColor = Color(0xFFE8F5E9), // Light Green
                trainingTitle = "Strength & Speed",
                trainingDescription = "Your body responds best to intense workouts today.",
                trainingIntensity = "High Intensity",
                nutritionTip = "Try Sattu today. Helps sustain energy during intense training.",
                showSafetyAlert = false // Follicular usually safe. Ovulation would trigger alert.
            )

            /*
            // Example of how Ovulation logic would look:
            if (phase == Ovulation && !isOnMedication) {
                 showSafetyAlert = true
            }
            */
        }
    }
}