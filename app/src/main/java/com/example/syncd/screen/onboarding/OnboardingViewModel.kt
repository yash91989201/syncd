package com.example.syncd.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.data.UserPreferences
import com.example.syncd.screen.onboarding.data.model.AthleteProfile
import com.example.syncd.screen.onboarding.data.model.CycleProfile
import com.example.syncd.screen.onboarding.data.model.HealthCondition
import com.example.syncd.screen.onboarding.data.model.OnboardingInput
import com.example.syncd.screen.onboarding.data.model.OnboardingRequest
import com.example.syncd.screen.onboarding.data.model.UserProfile
import com.example.syncd.screen.onboarding.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object StepIds {
    const val AGE_GROUP = 1
    const val CYCLE_STAGE = 2
    const val CYCLE_LENGTH = 3
    const val BLEEDING_DAYS = 4
    const val FLOW_INTENSITY = 5
    const val PAIN_LEVEL = 6
    const val HEALTH_CONDITION = 7
    const val HORMONAL_MEDICATION = 8
    const val IS_ATHLETE = 9
    const val TRAINING_FREQUENCY = 10
    const val SPORT = 11
}

data class OnboardingStep(
    val id: Int,
    val question: String,
    val helperText: String? = null,
    val options: List<OnboardingOption>,
    val allowCustomInput: Boolean = false
)

data class OnboardingOption(
    val id: String,
    val text: String
)

data class OnboardingState(
    val steps: List<OnboardingStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val answers: Map<Int, String> = emptyMap(),
    val customSport: String = "",
    val isComplete: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val totalSteps: Int get() = steps.size
    val currentStep: OnboardingStep get() = steps[currentStepIndex]

    val selectedOptionId: String? get() = answers[currentStep.id]
    
    val canProceed: Boolean get() {
        val currentAnswer = selectedOptionId ?: return false
        if (currentStep.id == StepIds.SPORT && currentAnswer == "not_listed") {
            return customSport.isNotBlank()
        }
        return true
    }
    
    val showCustomSportInput: Boolean get() = 
        currentStep.id == StepIds.SPORT && selectedOptionId == "not_listed"
}

class OnboardingViewModel(
    private val userPreferences: UserPreferences,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val baseSteps = listOf(
        OnboardingStep(
            id = StepIds.AGE_GROUP,
            question = "What is your age group?",
            options = listOf(
                OnboardingOption("under_18", "Under 18"),
                OnboardingOption("18_24", "18 - 24"),
                OnboardingOption("25_34", "25 - 34"),
                OnboardingOption("35_44", "35 - 44"),
                OnboardingOption("45_plus", "45+")
            )
        ),
        OnboardingStep(
            id = StepIds.CYCLE_STAGE,
            question = "Which best describes your cycle stage?",
            helperText = "This helps us predict your phases accurately.",
            options = listOf(
                OnboardingOption("regular", "Regular periods"),
                OnboardingOption("irregular", "Irregular periods"),
                OnboardingOption("trying_to_conceive", "Trying to conceive"),
                OnboardingOption("perimenopause", "Perimenopause")
            )
        ),
        OnboardingStep(
            id = StepIds.CYCLE_LENGTH,
            question = "How long is your typical cycle?",
            helperText = "Count from the first day of your period to the day before your next period.",
            options = listOf(
                OnboardingOption("unknown", "I don't know"),
                OnboardingOption("21_24", "Short (< 25 days)"),
                OnboardingOption("25_28", "Average (25-28 days)"),
                OnboardingOption("29_32", "Long (29-32 days)"),
                OnboardingOption("33_plus", "Very Long (> 32 days)")
            )
        ),
        OnboardingStep(
            id = StepIds.BLEEDING_DAYS,
            question = "How many days does your period usually last?",
            options = listOf(
                OnboardingOption("1_2", "Short (1-2 days)"),
                OnboardingOption("3_4", "Medium (3-4 days)"),
                OnboardingOption("5_6", "Long (5-6 days)"),
                OnboardingOption("7_plus", "Very Long (7+ days)")
            )
        ),
        OnboardingStep(
            id = StepIds.FLOW_INTENSITY,
            question = "How would you describe your flow?",
            options = listOf(
                OnboardingOption("light", "Light"),
                OnboardingOption("medium", "Medium"),
                OnboardingOption("heavy", "Heavy"),
                OnboardingOption("variable", "It varies")
            )
        ),
        OnboardingStep(
            id = StepIds.PAIN_LEVEL,
            question = "Do you experience pain during your cycle?",
            options = listOf(
                OnboardingOption("none", "No, rarely"),
                OnboardingOption("mild", "Yes, mild cramps"),
                OnboardingOption("moderate", "Yes, moderate pain"),
                OnboardingOption("severe", "Yes, severe pain")
            )
        ),
        OnboardingStep(
            id = StepIds.HEALTH_CONDITION,
            question = "Do you have any diagnosed medical conditions?",
            helperText = "We prioritize your privacy.",
            options = listOf(
                OnboardingOption("none", "None"),
                OnboardingOption("pcos", "PCOS"),
                OnboardingOption("endometriosis", "Endometriosis"),
                OnboardingOption("thyroid", "Thyroid issues"),
                OnboardingOption("other", "Other")
            )
        ),
        OnboardingStep(
            id = StepIds.HORMONAL_MEDICATION,
            question = "Are you currently taking any hormonal medication?",
            options = listOf(
                OnboardingOption("no", "No"),
                OnboardingOption("pill", "The Pill"),
                OnboardingOption("iud", "Hormonal IUD"),
                OnboardingOption("implant", "Implant / Injection")
            )
        ),
        OnboardingStep(
            id = StepIds.IS_ATHLETE,
            question = "Do you identify as an athlete?",
            options = listOf(
                OnboardingOption("yes", "Yes"),
                OnboardingOption("no", "No"),
                OnboardingOption("occasional", "I exercise occasionally")
            )
        )
    )

    private val athleteSteps = listOf(
        OnboardingStep(
            id = StepIds.TRAINING_FREQUENCY,
            question = "How often do you train?",
            options = listOf(
                OnboardingOption("1_2_per_week", "1-2 times per week"),
                OnboardingOption("3_4_per_week", "3-4 times per week"),
                OnboardingOption("5_6_per_week", "5-6 times per week"),
                OnboardingOption("daily", "Every day"),
                OnboardingOption("twice_daily", "Twice a day")
            )
        ),
        OnboardingStep(
            id = StepIds.SPORT,
            question = "What is your primary sport?",
            helperText = "Select the sport you practice most frequently.",
            options = listOf(
                OnboardingOption("cricket", "Cricket"),
                OnboardingOption("badminton", "Badminton"),
                OnboardingOption("kabaddi", "Kabaddi"),
                OnboardingOption("hockey", "Hockey"),
                OnboardingOption("football", "Football (Soccer)"),
                OnboardingOption("tennis", "Tennis"),
                OnboardingOption("volleyball", "Volleyball"),
                OnboardingOption("basketball", "Basketball"),
                OnboardingOption("athletics", "Athletics / Track & Field"),
                OnboardingOption("swimming", "Swimming"),
                OnboardingOption("wrestling", "Wrestling / Kushti"),
                OnboardingOption("boxing", "Boxing"),
                OnboardingOption("weightlifting", "Weightlifting"),
                OnboardingOption("yoga", "Yoga"),
                OnboardingOption("running", "Running / Marathon"),
                OnboardingOption("cycling", "Cycling"),
                OnboardingOption("gym", "Gym / Strength Training"),
                OnboardingOption("not_listed", "Not listed here")
            ),
            allowCustomInput = true
        )
    )

    private val _state = MutableStateFlow(OnboardingState(steps = baseSteps))
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun onOptionSelected(optionId: String) {
        val currentStepId = _state.value.currentStep.id
        val newAnswers = _state.value.answers.toMutableMap().apply {
            put(currentStepId, optionId)
        }
        _state.update { it.copy(answers = newAnswers, error = null) }

        if (currentStepId == StepIds.IS_ATHLETE) {
            rebuildStepsBasedOnAthleteAnswer(optionId)
        }
    }

    fun onCustomSportChanged(sport: String) {
        _state.update { it.copy(customSport = sport) }
    }

    private fun rebuildStepsBasedOnAthleteAnswer(answer: String) {
        val newSteps = if (answer == "yes") {
            baseSteps + athleteSteps
        } else {
            baseSteps
        }
        _state.update { it.copy(steps = newSteps) }
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
            _state.update { it.copy(isLoading = true, error = null) }
            
            val request = buildOnboardingRequest()
            
            onboardingRepository.completeOnboarding(request)
                .onSuccess { response ->
                    response.json.success?.let {
                        userPreferences.setHasCompletedOnboarding(it)
                    }

                    _state.update { it.copy(isComplete = true, isLoading = false) }
                }
                .onFailure { throwable ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = throwable.message ?: "Failed to complete onboarding"
                        ) 
                    }
                }
        }
    }

    private fun buildOnboardingRequest(): OnboardingRequest {
        val answers = _state.value.answers
        
        val isAthlete = answers[StepIds.IS_ATHLETE] == "yes"
        
        val userProfile = UserProfile(
            ageGroup = answers[StepIds.AGE_GROUP] ?: "unknown",
            cycleStage = answers[StepIds.CYCLE_STAGE] ?: "regular",
            isAthlete = isAthlete
        )
        
        val healthCondition = HealthCondition(
            condition = answers[StepIds.HEALTH_CONDITION] ?: "none"
        )
        
        val cycleProfile = CycleProfile(
            cycleLength = answers[StepIds.CYCLE_LENGTH] ?: "unknown",
            bleedingDays = answers[StepIds.BLEEDING_DAYS] ?: "3_4",
            flowIntensity = answers[StepIds.FLOW_INTENSITY] ?: "medium",
            painLevel = answers[StepIds.PAIN_LEVEL] ?: "none"
        )
        
        val athleteProfile = if (isAthlete) {
            val sportAnswer = answers[StepIds.SPORT]
            val sport = if (sportAnswer == "not_listed") {
                _state.value.customSport
            } else {
                sportAnswer ?: ""
            }
            
            AthleteProfile(
                trainingFrequency = answers[StepIds.TRAINING_FREQUENCY] ?: "3_4_per_week",
                sport = sport
            )
        } else {
            null
        }
        
        return OnboardingRequest(
            json = OnboardingInput(
                userProfile = userProfile,
                healthCondition = healthCondition,
                cycleProfile = cycleProfile,
                athleteProfile = athleteProfile
            )
        )
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
