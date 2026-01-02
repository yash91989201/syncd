package com.example.syncd.screen.home

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
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
    val error: String? = null,
    val phaseInfo: PhaseInfo? = null,
    val isAthlete: Boolean = false,
    val phaseName: String = "",
    val phaseColor: Color = Color(0xFFF5F5F5)
)

class HomeViewModel(
    application: Application,
    private val homeRepository: HomeRepository,
    private val userProfileRepository: UserProfileRepository
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPhaseInfo()
    }

    fun loadPhaseInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                phaseName = context.getString(R.string.loading)
            )
            
            val userProfile = userProfileRepository.getUserProfile().getOrNull()
            val isAthlete = userProfile?.userProfile?.isAthlete ?: false
            
            homeRepository.getPhaseInfo()
                .onSuccess { phaseInfo ->
                    val phaseName = when (phaseInfo?.phase) {
                        "menstrual" -> context.getString(R.string.phase_menstrual)
                        "follicular" -> context.getString(R.string.phase_follicular)
                        "ovulation" -> context.getString(R.string.phase_ovulation)
                        "luteal" -> context.getString(R.string.phase_luteal)
                        else -> context.getString(R.string.loading)
                    }
                    
                    val phaseColor = when (phaseInfo?.phase) {
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
                        phaseName = phaseName,
                        phaseColor = phaseColor,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: context.getString(R.string.error_load_phase_info)
                    )
                }
        }
    }
}
