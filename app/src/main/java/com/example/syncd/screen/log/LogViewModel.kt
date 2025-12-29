package com.example.syncd.screen.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class FlowLevel(val label: String) {
    LIGHT("Light"),
    MEDIUM("Medium"),
    HEAVY("Heavy"),
    VERY_HEAVY("Very heavy")
}

enum class PainLevel(val label: String) {
    NONE("No pain"),
    MILD("Mild"),
    MODERATE("Moderate"),
    SEVERE("Severe")
}

enum class EnergyLevel(val label: String) {
    LOW("Low"),
    OKAY("Okay"),
    GOOD("Good"),
    HIGH("High")
}

enum class MoodLevel(val label: String) {
    LOW("Low"),
    NEUTRAL("Neutral"),
    GOOD("Good"),
    GREAT("Great")
}

enum class SelectedDate(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    EARLIER("Earlier")
}

data class LogState(
    val selectedDate: SelectedDate = SelectedDate.TODAY,
    val customDate: LocalDate? = null,
    val showDatePicker: Boolean = false,
    val showFlowSection: Boolean = true,
    val selectedFlow: FlowLevel? = null,
    val selectedPain: PainLevel? = null,
    val selectedEnergy: EnergyLevel? = null,
    val selectedMood: MoodLevel? = null,
    val unusualNotes: String = "",
    val isSaving: Boolean = false,
    val showConfirmation: Boolean = false
) {
    val displayDate: String
        get() = when (selectedDate) {
            SelectedDate.TODAY -> "Today"
            SelectedDate.YESTERDAY -> "Yesterday"
            SelectedDate.EARLIER -> customDate?.toString() ?: "Pick a date"
        }
}

class LogViewModel : ViewModel() {

    private val _state = MutableStateFlow(LogState())
    val state: StateFlow<LogState> = _state.asStateFlow()

    fun onDateSelected(date: SelectedDate, customDate: LocalDate? = null) {
        _state.update { 
            it.copy(
                selectedDate = date, 
                customDate = customDate,
                showDatePicker = false
            ) 
        }
    }

    fun toggleDatePicker() {
        _state.update { it.copy(showDatePicker = !it.showDatePicker) }
    }

    fun onFlowSelected(flow: FlowLevel) {
        _state.update { 
            it.copy(selectedFlow = if (it.selectedFlow == flow) null else flow) 
        }
    }

    fun onPainSelected(pain: PainLevel) {
        _state.update { 
            it.copy(selectedPain = if (it.selectedPain == pain) null else pain) 
        }
    }

    fun onEnergySelected(energy: EnergyLevel) {
        _state.update { 
            it.copy(selectedEnergy = if (it.selectedEnergy == energy) null else energy) 
        }
    }

    fun onMoodSelected(mood: MoodLevel) {
        _state.update { 
            it.copy(selectedMood = if (it.selectedMood == mood) null else mood) 
        }
    }

    fun onUnusualNotesChanged(notes: String) {
        _state.update { it.copy(unusualNotes = notes) }
    }

    fun saveLog(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            delay(300)
            
            _state.update { it.copy(isSaving = false, showConfirmation = true) }
            
            delay(2000)
            
            onComplete()
        }
    }

    fun dismissConfirmation() {
        _state.update { it.copy(showConfirmation = false) }
    }
}
