package com.example.syncd.screen.onboarding

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.R
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
    const val LAST_PERIOD = 3
    const val CYCLE_LENGTH = 4
    const val BLEEDING_DAYS = 5
    const val FLOW_INTENSITY = 6
    const val PAIN_LEVEL = 7
    const val HEALTH_CONDITION = 8
    const val HORMONAL_MEDICATION = 9
    const val IS_ATHLETE = 10
    const val TRAINING_FREQUENCY = 11
    const val SPORT = 12
    const val PHYSICAL_ACTIVITY = 13
}

enum class StepType {
    OPTIONS,
    DATE_PICKER
}

data class OnboardingStep(
    val id: Int,
    @StringRes val questionResId: Int,
    @StringRes val helperTextResId: Int? = null,
    val options: List<OnboardingOption> = emptyList(),
    val allowCustomInput: Boolean = false,
    val stepType: StepType = StepType.OPTIONS
)

data class OnboardingOption(
    val id: String,
    @StringRes val textResId: Int
)

data class OnboardingState(
    val steps: List<OnboardingStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val answers: Map<Int, String> = emptyMap(),
    val customSport: String = "",
    val lastPeriodDate: Long? = null,
    val isComplete: Boolean = false,
    val isLoading: Boolean = false,
    @StringRes val errorResId: Int? = null,
    val errorMessage: String? = null // For API error messages
) {
    val totalSteps: Int get() = steps.size
    val currentStep: OnboardingStep get() = steps[currentStepIndex]

    val selectedOptionId: String? get() = answers[currentStep.id]

    val canProceed: Boolean
        get() {
            if (currentStep.stepType == StepType.DATE_PICKER) {
                return lastPeriodDate != null
            }
            val currentAnswer = selectedOptionId ?: return false
            if (currentStep.id == StepIds.SPORT && currentAnswer == "not_listed") {
                return customSport.isNotBlank()
            }
            return true
        }

    val showCustomSportInput: Boolean
        get() =
            currentStep.id == StepIds.SPORT && selectedOptionId == "not_listed"
}

class OnboardingViewModel(
    private val userPreferences: UserPreferences,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val baseSteps = listOf(
        OnboardingStep(
            id = StepIds.AGE_GROUP,
            questionResId = R.string.qa_age_group_question,
            options = listOf(
                OnboardingOption("under_18", R.string.qa_age_under_18),
                OnboardingOption("18_24", R.string.qa_age_18_24),
                OnboardingOption("25_34", R.string.qa_age_25_34),
                OnboardingOption("35_44", R.string.qa_age_35_44),
                OnboardingOption("45_plus", R.string.qa_age_45_plus)
            )
        ),
        OnboardingStep(
            id = StepIds.CYCLE_STAGE,
            questionResId = R.string.qa_cycle_stage_question,
            helperTextResId = R.string.qa_cycle_stage_helper,
            options = listOf(
                OnboardingOption("regular", R.string.qa_cycle_stage_regular),
                OnboardingOption("irregular", R.string.qa_cycle_stage_irregular),
                OnboardingOption("trying_to_conceive", R.string.qa_cycle_stage_trying_to_conceive),
                OnboardingOption("pregnant", R.string.qa_cycle_stage_pregnant),
                OnboardingOption("postpartum", R.string.qa_cycle_stage_postpartum),
                OnboardingOption("perimenopause", R.string.qa_cycle_stage_perimenopause)
            )
        ),
        OnboardingStep(
            id = StepIds.LAST_PERIOD,
            questionResId = R.string.qa_last_period_question,
            helperTextResId = R.string.qa_last_period_helper,
            stepType = StepType.DATE_PICKER
        ),
        OnboardingStep(
            id = StepIds.CYCLE_LENGTH,
            questionResId = R.string.qa_cycle_length_question,
            helperTextResId = R.string.qa_cycle_length_helper,
            options = listOf(
                OnboardingOption("unknown", R.string.qa_cycle_length_unknown),
                OnboardingOption("21_24", R.string.qa_cycle_length_short),
                OnboardingOption("25_28", R.string.qa_cycle_length_average),
                OnboardingOption("29_32", R.string.qa_cycle_length_long),
                OnboardingOption("33_plus", R.string.qa_cycle_length_very_long)
            )
        ),
        OnboardingStep(
            id = StepIds.BLEEDING_DAYS,
            questionResId = R.string.qa_bleeding_days_question,
            options = listOf(
                OnboardingOption("1_2", R.string.qa_bleeding_days_short),
                OnboardingOption("3_4", R.string.qa_bleeding_days_medium),
                OnboardingOption("5_6", R.string.qa_bleeding_days_long),
                OnboardingOption("7_plus", R.string.qa_bleeding_days_very_long)
            )
        ),
        OnboardingStep(
            id = StepIds.FLOW_INTENSITY,
            questionResId = R.string.qa_flow_intensity_question,
            options = listOf(
                OnboardingOption("light", R.string.qa_flow_light),
                OnboardingOption("medium", R.string.qa_flow_medium),
                OnboardingOption("heavy", R.string.qa_flow_heavy),
                OnboardingOption("variable", R.string.qa_flow_variable)
            )
        ),
        OnboardingStep(
            id = StepIds.PAIN_LEVEL,
            questionResId = R.string.qa_pain_level_question,
            options = listOf(
                OnboardingOption("none", R.string.qa_pain_none),
                OnboardingOption("mild", R.string.qa_pain_mild),
                OnboardingOption("moderate", R.string.qa_pain_moderate),
                OnboardingOption("severe", R.string.qa_pain_severe)
            )
        ),
        OnboardingStep(
            id = StepIds.HEALTH_CONDITION,
            questionResId = R.string.qa_health_condition_question,
            helperTextResId = R.string.qa_health_condition_helper,
            options = listOf(
                OnboardingOption("none", R.string.qa_health_condition_none),
                OnboardingOption("pcos", R.string.qa_health_condition_pcos),
                OnboardingOption("endometriosis", R.string.qa_health_condition_endometriosis),
                OnboardingOption("thyroid", R.string.qa_health_condition_thyroid),
                OnboardingOption("fibroids", R.string.qa_health_condition_fibroids),
                OnboardingOption("anemia", R.string.qa_health_condition_anemia),
                OnboardingOption("diabetes", R.string.qa_health_condition_diabetes)
            )
        ),
        OnboardingStep(
            id = StepIds.HORMONAL_MEDICATION,
            questionResId = R.string.qa_hormonal_med_question,
            options = listOf(
                OnboardingOption("none", R.string.qa_hormonal_med_none),
                OnboardingOption("pill", R.string.qa_hormonal_med_pill),
                OnboardingOption("iud", R.string.qa_hormonal_med_iud),
                OnboardingOption("implant", R.string.qa_hormonal_med_implant)
            )
        ),
        OnboardingStep(
            id = StepIds.IS_ATHLETE,
            questionResId = R.string.qa_is_athlete_question,
            options = listOf(
                OnboardingOption("yes", R.string.qa_yes),
                OnboardingOption("no", R.string.qa_no)
            )
        )
    )

    private val athleteSteps = listOf(
        OnboardingStep(
            id = StepIds.TRAINING_FREQUENCY,
            questionResId = R.string.qa_training_frequency_question,
            options = listOf(
                OnboardingOption("1_2_per_week", R.string.qa_training_1_2),
                OnboardingOption("3_4_per_week", R.string.qa_training_3_4),
                OnboardingOption("5_6_per_week", R.string.qa_training_5_6),
                OnboardingOption("daily", R.string.qa_training_daily),
                OnboardingOption("twice_daily", R.string.qa_training_twice_daily)
            )
        ),
        OnboardingStep(
            id = StepIds.SPORT,
            questionResId = R.string.qa_sport_question,
            helperTextResId = R.string.qa_sport_helper,
            options = listOf(
                OnboardingOption("cricket", R.string.qa_sport_cricket),
                OnboardingOption("badminton", R.string.qa_sport_badminton),
                OnboardingOption("kabaddi", R.string.qa_sport_kabaddi),
                OnboardingOption("hockey", R.string.qa_sport_hockey),
                OnboardingOption("football", R.string.qa_sport_football),
                OnboardingOption("tennis", R.string.qa_sport_tennis),
                OnboardingOption("volleyball", R.string.qa_sport_volleyball),
                OnboardingOption("basketball", R.string.qa_sport_basketball),
                OnboardingOption("athletics", R.string.qa_sport_athletics),
                OnboardingOption("swimming", R.string.qa_sport_swimming),
                OnboardingOption("wrestling", R.string.qa_sport_wrestling),
                OnboardingOption("boxing", R.string.qa_sport_boxing),
                OnboardingOption("weightlifting", R.string.qa_sport_weightlifting),
                OnboardingOption("yoga", R.string.qa_sport_yoga),
                OnboardingOption("running", R.string.qa_sport_running),
                OnboardingOption("cycling", R.string.qa_sport_cycling),
                OnboardingOption("gym", R.string.qa_sport_gym),
                OnboardingOption("not_listed", R.string.qa_sport_not_listed)
            ),
            allowCustomInput = true
        )
    )

    private val nonAthleteSteps = listOf(
        OnboardingStep(
            id = StepIds.PHYSICAL_ACTIVITY,
            questionResId = R.string.qa_physical_activity_question,
            helperTextResId = R.string.qa_physical_activity_helper,
            options = listOf(
                OnboardingOption("daily_running", R.string.qa_physical_activity_daily_running),
                OnboardingOption("gym_fitness", R.string.qa_physical_activity_gym),
                OnboardingOption("walking", R.string.qa_physical_activity_walking),
                OnboardingOption("yoga_stretching", R.string.qa_physical_activity_yoga),
                OnboardingOption("none", R.string.qa_physical_activity_none)
            )
        )
    )

    private val _state = MutableStateFlow(OnboardingState(steps = baseSteps))
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun onOptionSelected(optionId: String) {
        val currentStepId = _state.value.currentStep.id
        val newAnswers = _state.value.answers.toMutableMap().apply {
            put(currentStepId, optionId)
        }
        _state.update { it.copy(answers = newAnswers, errorResId = null, errorMessage = null) }

        if (currentStepId == StepIds.IS_ATHLETE) {
            rebuildStepsBasedOnAthleteAnswer(optionId)
        }
    }

    fun onCustomSportChanged(sport: String) {
        _state.update { it.copy(customSport = sport) }
    }

    fun onLastPeriodDateSelected(dateMillis: Long) {
        _state.update { it.copy(lastPeriodDate = dateMillis) }
    }

    private fun rebuildStepsBasedOnAthleteAnswer(answer: String) {
        val newSteps = when (answer) {
            "yes" -> baseSteps + athleteSteps
            "no" -> baseSteps + nonAthleteSteps
            else -> baseSteps
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
            _state.update { it.copy(isLoading = true, errorResId = null, errorMessage = null) }

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
                            errorMessage = throwable.message,
                            errorResId = if (throwable.message == null) R.string.error_complete_onboarding_failed else null
                        )
                    }
                }
        }
    }

    private fun buildOnboardingRequest(): OnboardingRequest {
        val answers = _state.value.answers

        val isAthlete = answers[StepIds.IS_ATHLETE] == "yes"

        val physicalActivity = if (!isAthlete) {
            answers[StepIds.PHYSICAL_ACTIVITY]
        } else {
            "none"
        }

        val userProfile = UserProfile(
            ageGroup = answers[StepIds.AGE_GROUP] ?: "unknown",
            cycleStage = answers[StepIds.CYCLE_STAGE] ?: "regular",
            isAthlete = isAthlete,
            physicalActivity = physicalActivity
        )

        val healthCondition = HealthCondition(
            condition = answers[StepIds.HEALTH_CONDITION] ?: "none",
            medication = answers[StepIds.HORMONAL_MEDICATION] ?: "none"
        )

        val lastPeriodDate = _state.value.lastPeriodDate!!.let { millis ->
            java.time.Instant.ofEpochMilli(millis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }

        val cycleProfile = CycleProfile(
            cycleLength = answers[StepIds.CYCLE_LENGTH] ?: "unknown",
            bleedingDays = answers[StepIds.BLEEDING_DAYS] ?: "3_4",
            flowIntensity = answers[StepIds.FLOW_INTENSITY] ?: "medium",
            painLevel = answers[StepIds.PAIN_LEVEL] ?: "none",
            lastPeriod = lastPeriodDate
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
        _state.update { it.copy(errorResId = null, errorMessage = null) }
    }
}
