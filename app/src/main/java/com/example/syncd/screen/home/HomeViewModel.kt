package com.example.syncd.screen.home

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.R
import com.example.syncd.data.repository.UserProfileRepository
import com.example.syncd.screen.home.data.model.PhaseInfo
import com.example.syncd.screen.home.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    @StringRes val errorResId: Int? = null,
    val errorMessage: String? = null,
    val phaseInfo: PhaseInfo? = null,
    val isAthlete: Boolean = false,
    val phase: String = "",  // Raw phase ID like "menstrual", "follicular", etc.
    @StringRes val phaseNameResId: Int = R.string.loading,
    val phaseColor: Color = Color(0xFFF5F5F5)
)

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
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorResId = null,
                errorMessage = null,
                phaseNameResId = R.string.loading
            )
            
            val userProfile = userProfileRepository.getUserProfile().getOrNull()
            val isAthlete = userProfile?.userProfile?.isAthlete ?: false
            
            homeRepository.getPhaseInfo()
                .onSuccess { phaseInfo ->
                    val phase = phaseInfo?.phase ?: ""
                    val phaseNameResId = when (phase) {
                        "menstrual" -> R.string.phase_menstrual
                        "follicular" -> R.string.phase_follicular
                        "ovulation" -> R.string.phase_ovulation
                        "luteal" -> R.string.phase_luteal
                        else -> R.string.loading
                    }
                    
                    val phaseColor = when (phase) {
                        "menstrual" -> Color(0xFFFFCDD2)
                        "follicular" -> Color(0xFFE8F5E9)
                        "ovulation" -> Color(0xFFFFF9C4)
                        "luteal" -> Color(0xFFE1BEE7)
                        else -> Color(0xFFF5F5F5)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        phaseInfo = phaseInfo,
                        isAthlete = isAthlete,
                        phase = phase,
                        phaseNameResId = phaseNameResId,
                        phaseColor = phaseColor,
                        errorResId = null,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message,
                        errorResId = if (exception.message == null) R.string.error_load_phase_info else null
                    )
                }
        }
    }
}
