package com.example.syncd.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Models
data class OnboardingStep(
    val id: Int,
    val question: String,
    val helperText: String? = null,
    val options: List<OnboardingOption>,
)

data class OnboardingOption(
    val id: String,
    val text: String
)

data class OnboardingState(
    val steps: List<OnboardingStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val answers: Map<Int, String> = emptyMap(), // Step ID -> Option ID
    val isComplete: Boolean = false
) {
    val totalSteps: Int get() = steps.size
    val currentStep: OnboardingStep get() = steps[currentStepIndex]
    
    val selectedOptionId: String? get() = answers[currentStep.id]
    val canProceed: Boolean get() = selectedOptionId != null
}

class OnboardingViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val steps = listOf(
        OnboardingStep(
            id = 1,
            question = "What is your age group?",
            options = listOf(
                OnboardingOption("u18", "Under 18"),
                OnboardingOption("18-24", "18 - 24"),
                OnboardingOption("25-34", "25 - 34"),
                OnboardingOption("35-44", "35 - 44"),
                OnboardingOption("45+", "45+")
            )
        ),
        OnboardingStep(
            id = 2,
            question = "Which best describes your cycle stage?",
            helperText = "This helps us predict your phases accurately.",
            options = listOf(
                OnboardingOption("regular", "Regular periods"),
                OnboardingOption("irregular", "Irregular periods"),
                OnboardingOption("pregnancy", "Trying to conceive"),
                OnboardingOption("perimenopause", "Perimenopause")
            )
        ),
         OnboardingStep(
            id = 3,
            question = "How long is your typical cycle?",
            helperText = "Count from the first day of your period to the day before your next period.",
            options = listOf(
                OnboardingOption("unknown", "I don't know"),
                OnboardingOption("short", "Short (< 25 days)"),
                OnboardingOption("avg", "Average (28 days)"),
                OnboardingOption("long", "Long (> 32 days)")
            )
        ),
         OnboardingStep(
            id = 4,
            question = "How many days does your period usually last?",
            options = listOf(
                OnboardingOption("short", "Short (1-3 days)"),
                OnboardingOption("medium", "Medium (4-5 days)"),
                OnboardingOption("long", "Long (6-7 days)"),
                OnboardingOption("very_long", "Very Long (8+ days)")
            )
        ),
        OnboardingStep(
            id = 5,
            question = "How would you describe your flow?",
            options = listOf(
                OnboardingOption("light", "Light"),
                OnboardingOption("medium", "Medium"),
                OnboardingOption("heavy", "Heavy"),
                OnboardingOption("variable", "It varies")
            )
        ),
        OnboardingStep(
            id = 6,
            question = "Do you experience pain during your cycle?",
            options = listOf(
                OnboardingOption("none", "No, rarely"),
                OnboardingOption("mild", "Yes, mild cramps"),
                OnboardingOption("moderate", "Yes, moderate pain"),
                OnboardingOption("severe", "Yes, severe pain")
            )
        ),
        OnboardingStep(
            id = 7,
            question = "Do you have any diagnosed medical conditions?",
            helperText = "We prioritize your privacy.",
            options = listOf(
                OnboardingOption("none", "None"),
                OnboardingOption("pcos", "PCOS"),
                OnboardingOption("endo", "Endometriosis"),
                OnboardingOption("thyroid", "Thyroid issues"),
                OnboardingOption("other", "Other")
            )
        ),
        OnboardingStep(
            id = 8,
            question = "Are you currently taking any hormonal medication?",
            options = listOf(
                OnboardingOption("no", "No"),
                OnboardingOption("pill", "The Pill"),
                OnboardingOption("iud", "Hormonal IUD"),
                OnboardingOption("implant", "Implant / Injection")
            )
        ),
        OnboardingStep(
            id = 9,
            question = "Do you identify as an athlete?",
            options = listOf(
                OnboardingOption("yes", "Yes"),
                OnboardingOption("no", "No"),
                OnboardingOption("occasional", "I exercise occasionally")
            )
        ),
        // Conditional logic can be handled by dynamically modifying the list or checking in onNext
        // For simplicity v1, we include it as the last step
        OnboardingStep(
            id = 10,
            question = "What is your primary activity?",
            options = listOf(
                OnboardingOption("running", "Running"),
                OnboardingOption("yoga", "Yoga / Pilates"),
                OnboardingOption("strength", "Strength Training"),
                OnboardingOption("team", "Team Sports"),
                OnboardingOption("mixed", "Mixed Activity")
            )
        )
    )

    private val _state = MutableStateFlow(OnboardingState(steps = steps))
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun onOptionSelected(optionId: String) {
        val currentStepId = _state.value.currentStep.id
        val newAnswers = _state.value.answers.toMutableMap().apply {
            put(currentStepId, optionId)
        }
        _state.update { it.copy(answers = newAnswers) }
        
        // Optional: Auto-advance for single-choice questions could go here
    }

    fun onNext() {
        if (_state.value.canProceed) {
            if (_state.value.currentStepIndex < _state.value.totalSteps - 1) {
                _state.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
            } else {
                completeOnboarding()
            }
        }
    }

    fun onBack() {
        if (_state.value.currentStepIndex > 0) {
            _state.update { it.copy(currentStepIndex = it.currentStepIndex - 1) }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setHasCompletedOnboarding(true)
            _state.update { it.copy(isComplete = true) }
        }
    }
}
