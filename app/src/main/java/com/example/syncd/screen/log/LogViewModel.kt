package com.example.syncd.screen.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.screen.log.data.model.DailyLogEntry
import com.example.syncd.screen.log.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class FlowLevel(val label: String, val apiValue: String) {
    LIGHT("Light", "light"),
    MEDIUM("Medium", "medium"),
    HEAVY("Heavy", "heavy"),
    VERY_HEAVY("Very heavy", "very_heavy")
}

enum class PainLevel(val label: String, val apiValue: String) {
    NONE("No pain", "none"),
    MILD("Mild", "mild"),
    MODERATE("Moderate", "moderate"),
    SEVERE("Severe", "severe")
}

enum class EnergyLevel(val label: String, val apiValue: String) {
    LOW("Low", "low"),
    OKAY("Okay", "okay"),
    GOOD("Good", "good"),
    HIGH("High", "high")
}

enum class MoodLevel(val label: String, val apiValue: String) {
    LOW("Low", "low"),
    NEUTRAL("Neutral", "neutral"),
    GOOD("Good", "good"),
    GREAT("Great", "great")
}

data class LogState(
    val selectedDate: LocalDate = LocalDate.now(),
    val showDatePicker: Boolean = false,
    val showFlowSection: Boolean = true,
    val selectedFlow: FlowLevel? = null,
    val selectedPain: PainLevel? = null,
    val selectedEnergy: EnergyLevel? = null,
    val selectedMood: MoodLevel? = null,
    val unusualNotes: String = "",
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val showConfirmation: Boolean = false,
    val error: String? = null,
    val existingLogId: String? = null
) {
    val displayDate: String
        get() {
            val today = LocalDate.now()
            return when (selectedDate) {
                today -> "Today"
                today.minusDays(1) -> "Yesterday"
                else -> selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            }
        }
    
    val hasExistingLog: Boolean
        get() = existingLogId != null
}

class LogViewModel(
    private val logRepository: LogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LogState())
    val state: StateFlow<LogState> = _state.asStateFlow()

    init {
        fetchLogForCurrentDate()
    }

    private fun fetchLogForCurrentDate() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val dateString = getDateString()
            logRepository.listLogs(date = dateString)
                .onSuccess { logs ->
                    val existingLog = logs.firstOrNull()
                    if (existingLog != null) {
                        populateFromExistingLog(existingLog)
                    } else {
                        clearFormData()
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun populateFromExistingLog(log: DailyLogEntry) {
        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                existingLogId = log.id,
                selectedFlow = log.flow?.let { apiValue -> 
                    FlowLevel.entries.find { it.apiValue == apiValue }
                },
                selectedPain = log.painLevel?.let { apiValue ->
                    PainLevel.entries.find { it.apiValue == apiValue }
                },
                selectedEnergy = log.energyLevel?.let { apiValue ->
                    EnergyLevel.entries.find { it.apiValue == apiValue }
                },
                selectedMood = log.mood?.let { apiValue ->
                    MoodLevel.entries.find { it.apiValue == apiValue }
                },
                unusualNotes = log.note ?: ""
            )
        }
    }

    private fun clearFormData() {
        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                existingLogId = null,
                selectedFlow = null,
                selectedPain = null,
                selectedEnergy = null,
                selectedMood = null,
                unusualNotes = ""
            )
        }
    }

    private fun getDateString(): String {
        return _state.value.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun onDateSelected(date: LocalDate) {
        _state.update { 
            it.copy(
                selectedDate = date,
                showDatePicker = false
            ) 
        }
        fetchLogForCurrentDate()
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
            _state.update { it.copy(isSaving = true, error = null) }
            
            val currentState = _state.value
            val result = if (currentState.hasExistingLog) {
                logRepository.updateLog(
                    id = currentState.existingLogId!!,
                    painLevel = currentState.selectedPain?.apiValue,
                    energyLevel = currentState.selectedEnergy?.apiValue,
                    mood = currentState.selectedMood?.apiValue,
                    flow = currentState.selectedFlow?.apiValue,
                    note = currentState.unusualNotes.ifBlank { null }
                )
            } else {
                logRepository.createLog(
                    painLevel = currentState.selectedPain?.apiValue,
                    energyLevel = currentState.selectedEnergy?.apiValue,
                    mood = currentState.selectedMood?.apiValue,
                    flow = currentState.selectedFlow?.apiValue,
                    note = currentState.unusualNotes.ifBlank { null }
                )
            }
            
            result
                .onSuccess { savedLog ->
                    _state.update { 
                        it.copy(
                            isSaving = false, 
                            showConfirmation = true,
                            existingLogId = savedLog.id
                        ) 
                    }
                    kotlinx.coroutines.delay(2000)
                    onComplete()
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(isSaving = false, error = error.message) 
                    }
                }
        }
    }

    fun dismissConfirmation() {
        _state.update { it.copy(showConfirmation = false) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
