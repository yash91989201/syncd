package com.example.syncd.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.auth.data.model.User
import com.example.syncd.auth.data.repository.AuthRepository
import com.example.syncd.data.UserPreferences
import com.example.syncd.navigation.Navigator
import com.example.syncd.navigation.Screen
import com.example.syncd.screen.onboarding.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val isCheckingSession: Boolean = true,
    val error: String? = null,
    val currentUser: User? = null,
    val isAuthenticated: Boolean = false,
    val hasCompletedOnboarding: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val navigator: Navigator,
    private val userPreferences: UserPreferences,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkSession()
    }
    
    fun updatePhoneNumber(phone: String) {
        val numericOnly = phone.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phoneNumber = numericOnly, error = null) }
    }
    
    fun updateOtpCode(code: String) {
        _uiState.update { it.copy(otpCode = code, error = null) }
    }
    
    fun sendOtp() {
        val phoneNumber = _uiState.value.phoneNumber
        if (phoneNumber.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a phone number") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.sendOtp(phoneNumber)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    navigator.navigateTo(Screen.OTP(phoneNumber))
                }
                .onFailure { throwable ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = throwable.message ?: "Failed to send OTP"
                        )
                    }
                }
        }
    }
    
    fun verifyOtp() {
        val phoneNumber = _uiState.value.phoneNumber
        val code = _uiState.value.otpCode
        
        if (code.isBlank()) {
            _uiState.update { it.copy(error = "Please enter the OTP code") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.verifyOtp(phoneNumber, code)
                .onSuccess { response ->
                    if (response.user != null) {
                        checkOnboardingStatus(response.user)
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                currentUser = null,
                                isAuthenticated = false
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = throwable.message ?: "Failed to verify OTP"
                        )
                    }
                }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            authRepository.signOut()
                .onSuccess {
                    _uiState.update { 
                        AuthUiState(isCheckingSession = false, isAuthenticated = false)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = throwable.message ?: "Failed to sign out"
                        )
                    }
                }
        }
    }
    
    private fun checkSession() {
        viewModelScope.launch {
            authRepository.getSession()
                .onSuccess { response ->
                    val isAuthenticated = response.user != null
                    if (isAuthenticated) {
                        checkOnboardingStatus(response.user)
                    } else {
                        _uiState.update { 
                            it.copy(
                                currentUser = null,
                                isAuthenticated = false,
                                isCheckingSession = false,
                                hasCompletedOnboarding = false
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update { 
                        it.copy(
                            isCheckingSession = false,
                            hasCompletedOnboarding = false
                        ) 
                    }
                }
        }
    }

    private suspend fun checkOnboardingStatus(user: User?) {
        val localStatus = userPreferences.hasCompletedOnboarding.first()
        
        if (localStatus) {
            _uiState.update { 
                it.copy(
                    currentUser = user,
                    isAuthenticated = true,
                    isCheckingSession = false,
                    isLoading = false,
                    hasCompletedOnboarding = true
                )
            }
            return
        }
        
        onboardingRepository.isOnboardingComplete()
            .onSuccess { statusResponse ->
                if (statusResponse.json.complete) {
                    userPreferences.setHasCompletedOnboarding(true)
                }
                _uiState.update { 
                    it.copy(
                        currentUser = user,
                        isAuthenticated = true,
                        isCheckingSession = false,
                        isLoading = false,
                        hasCompletedOnboarding = statusResponse.json.complete
                    )
                }
            }
            .onFailure {
                _uiState.update { 
                    it.copy(
                        currentUser = user,
                        isAuthenticated = true,
                        isCheckingSession = false,
                        isLoading = false,
                        hasCompletedOnboarding = false
                    )
                }
            }
    }
    
    fun setOnboardingCompleted() {
        viewModelScope.launch {
            userPreferences.setHasCompletedOnboarding(true)
            _uiState.update { it.copy(hasCompletedOnboarding = true) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun setPhoneNumberForOtp(phoneNumber: String) {
        _uiState.update { it.copy(phoneNumber = phoneNumber, otpCode = "") }
    }
}
