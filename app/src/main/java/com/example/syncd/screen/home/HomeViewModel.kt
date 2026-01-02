package com.example.syncd.screen.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.data.repository.UserProfileRepository
import com.example.syncd.screen.home.data.model.PhaseInfo
import com.example.syncd.screen.home.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val phaseInfo: PhaseInfo? = null,
    val isAthlete: Boolean = false
) {
    val phaseName: String
        get() = when (phaseInfo?.phase) {
            "menstrual" -> "Menstrual Phase"
            "follicular" -> "Follicular Phase"
            "ovulation" -> "Ovulation Phase"
            "luteal" -> "Luteal Phase"
            else -> "Loading..."
        }

    val phaseColor: Color
        get() = when (phaseInfo?.phase) {
            "menstrual" -> Color(0xFFFFCDD2)
            "follicular" -> Color(0xFFE8F5E9)
            "ovulation" -> Color(0xFFFFF9C4)
            "luteal" -> Color(0xFFE1BEE7)
            else -> Color(0xFFF5F5F5)
        }
}

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPhaseInfo()
    }

    fun loadPhaseInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val userProfile = userProfileRepository.getUserProfile().getOrNull()
            val isAthlete = userProfile?.userProfile?.isAthlete ?: false
            
            homeRepository.getPhaseInfo()
                .onSuccess { phaseInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        phaseInfo = phaseInfo,
                        isAthlete = isAthlete,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load phase info"
                    )
                }
        }
    }
}
