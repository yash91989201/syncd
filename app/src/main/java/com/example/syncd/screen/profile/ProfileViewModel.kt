package com.example.syncd.screen.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.R
import com.example.syncd.data.model.PhysicalActivity
import com.example.syncd.data.model.TrainingFrequency
import com.example.syncd.screen.onboarding.OnboardingOption
import com.example.syncd.screen.onboarding.OnboardingStep
import com.example.syncd.screen.onboarding.StepIds
import com.example.syncd.screen.onboarding.StepType
import com.example.syncd.screen.onboarding.data.model.AthleteProfile
import com.example.syncd.screen.onboarding.data.model.AthleteProfileUpdate
import com.example.syncd.screen.onboarding.data.model.CycleProfileUpdate
import com.example.syncd.screen.onboarding.data.model.HealthConditionUpdate
import com.example.syncd.screen.onboarding.data.model.OnboardingGetPayload
import com.example.syncd.screen.onboarding.data.model.OnboardingGetResponse
import com.example.syncd.screen.onboarding.data.model.OnboardingUpdateInput
import com.example.syncd.screen.onboarding.data.model.OnboardingUpdateRequest
import com.example.syncd.screen.onboarding.data.model.UserProfileUpdate
import com.example.syncd.screen.onboarding.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class ProfileState(
    val name: String = "",
    val email: String = "",
    val answers: Map<Int, String> = emptyMap(),
    val customSport: String = "",
    val lastPeriodDate: Long? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val errorResId: Int? = null,
    val errorMessage: String? = null,
    val baseline: OnboardingSnapshot? = null,
    val hasChanges: Boolean = false
) {
    val isAthlete: Boolean get() = answers[StepIds.IS_ATHLETE] == "yes"
}

data class OnboardingSnapshot(
    val answers: Map<Int, String>,
    val customSport: String,
    val lastPeriodDate: Long?
)

class ProfileViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val allSteps = listOf(
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
        ),
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
        ),
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

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    fun getOnboardingSteps(): List<OnboardingStep> = allSteps

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name, isSaved = false, errorResId = null, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, isSaved = false, errorResId = null, errorMessage = null) }
    }

    fun onOptionSelected(stepId: Int, optionId: String) {
        val newAnswers = _state.value.answers.toMutableMap().apply {
            put(stepId, optionId)
        }
        _state.update {
            it.copy(
                answers = newAnswers,
                isSaved = false,
                errorResId = null,
                errorMessage = null
            ).withHasChanges()
        }
    }

    fun onCustomSportChanged(sport: String) {
        _state.update { it.copy(customSport = sport, isSaved = false).withHasChanges() }
    }

    fun onLastPeriodDateSelected(dateMillis: Long) {
        _state.update { it.copy(lastPeriodDate = dateMillis, isSaved = false).withHasChanges() }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorResId = null, errorMessage = null) }

            val request = buildOnboardingUpdateRequest()

            onboardingRepository.updateOnboarding(request)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSaved = true
                        ).withBaselineFromCurrent()
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message,
                            errorResId = if (throwable.message == null) R.string.error_update_profile_failed else null
                        )
                    }
                }
        }
    }

    private fun buildOnboardingUpdateRequest(): OnboardingUpdateRequest {
        val answers = _state.value.answers
        val isAthlete = answers[StepIds.IS_ATHLETE] == "yes"

        val physicalActivity = if (!isAthlete) {
            answers[StepIds.PHYSICAL_ACTIVITY]
        } else {
            null
        }

        val userProfile = UserProfileUpdate(
            ageGroup = answers[StepIds.AGE_GROUP],
            cycleStage = answers[StepIds.CYCLE_STAGE],
            isAthlete = isAthlete,
            physicalActivity = physicalActivity
        )

        val healthCondition = HealthConditionUpdate(
            condition = answers[StepIds.HEALTH_CONDITION],
            medication = answers[StepIds.HORMONAL_MEDICATION]
        )

        val lastPeriodDate = _state.value.lastPeriodDate?.let { millis ->
            java.time.Instant.ofEpochMilli(millis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }

        val cycleProfile = CycleProfileUpdate(
            cycleLength = answers[StepIds.CYCLE_LENGTH],
            bleedingDays = answers[StepIds.BLEEDING_DAYS],
            flowIntensity = answers[StepIds.FLOW_INTENSITY],
            painLevel = answers[StepIds.PAIN_LEVEL],
            lastPeriod = lastPeriodDate
        )

        val athleteProfile = if (isAthlete) {
            val sportAnswer = answers[StepIds.SPORT]
            val sport = if (sportAnswer == "not_listed") {
                _state.value.customSport
            } else {
                sportAnswer
            }

            AthleteProfileUpdate(
                trainingFrequency = answers[StepIds.TRAINING_FREQUENCY],
                sport = sport
            )
        } else {
            null
        }

        return OnboardingUpdateRequest(
            json = OnboardingUpdateInput(
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

    fun dismissSavedMessage() {
        _state.update { it.copy(isSaved = false) }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorResId = null, errorMessage = null) }

            onboardingRepository.getOnboardingData()
                .onSuccess { response ->
                    val prefilled = buildPrefilledState(response.json)
                    _state.update {
                        it.copy(
                            answers = prefilled.answers,
                            customSport = prefilled.customSport,
                            lastPeriodDate = prefilled.lastPeriodDate,
                            isLoading = false,
                            errorResId = null,
                            errorMessage = null
                        ).withBaselineFromCurrent()
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message,
                            errorResId = if (throwable.message == null) R.string.error_load_profile_failed else null
                        )
                    }
                }
        }
    }

    private fun ProfileState.withBaselineFromCurrent(): ProfileState {
        val snapshot = OnboardingSnapshot(
            answers = answers,
            customSport = customSport,
            lastPeriodDate = lastPeriodDate
        )
        return copy(
            baseline = snapshot,
            hasChanges = false
        )
    }

    private fun ProfileState.withHasChanges(): ProfileState {
        val base = baseline ?: return copy(hasChanges = true)
        val current = OnboardingSnapshot(
            answers = answers,
            customSport = customSport,
            lastPeriodDate = lastPeriodDate
        )
        return copy(hasChanges = current != base)
    }

    private fun buildPrefilledState(payload: OnboardingGetPayload): ProfileState {
        val answers = mutableMapOf<Int, String>()

        payload.userProfile?.let { userProfile ->
            answers[StepIds.AGE_GROUP] = normalizeOptionId(userProfile.ageGroup)
            answers[StepIds.CYCLE_STAGE] = normalizeOptionId(userProfile.cycleStage)
            answers[StepIds.IS_ATHLETE] = if (userProfile.isAthlete) "yes" else "no"
            if (!userProfile.isAthlete) {
                userProfile.physicalActivity
                    ?.let { normalizePhysicalActivityOptionId(it) }
                    ?.let { answers[StepIds.PHYSICAL_ACTIVITY] = it }
            }
        }

        payload.healthCondition?.let { healthCondition ->
            answers[StepIds.HEALTH_CONDITION] = normalizeOptionId(healthCondition.condition)
            answers[StepIds.HORMONAL_MEDICATION] = normalizeOptionId(healthCondition.medication)
        }

        var lastPeriodDateMillis: Long? = null
        payload.cycleProfile?.let { cycleProfile ->
            answers[StepIds.CYCLE_LENGTH] = normalizeOptionId(cycleProfile.cycleLength)
            answers[StepIds.BLEEDING_DAYS] = normalizeOptionId(cycleProfile.bleedingDays)
            answers[StepIds.FLOW_INTENSITY] = normalizeOptionId(cycleProfile.flowIntensity)
            answers[StepIds.PAIN_LEVEL] = normalizeOptionId(cycleProfile.painLevel)

            lastPeriodDateMillis = parseLastPeriodToMillis(cycleProfile.lastPeriod.trim())
        }

        var customSport = ""
        payload.athleteProfile?.let { athleteProfile ->
            answers[StepIds.TRAINING_FREQUENCY] = normalizeOptionId(athleteProfile.trainingFrequency)

            val sport = athleteProfile.sport.trim()
            val knownSportOptionIds = allSteps
                .firstOrNull { it.id == StepIds.SPORT }
                ?.options
                ?.map { it.id }
                ?.toSet()
                .orEmpty()

            val sportOptionId = if (sport in knownSportOptionIds) sport else "not_listed"
            answers[StepIds.SPORT] = sportOptionId
            if (sportOptionId == "not_listed") {
                customSport = sport
            }

            answers[StepIds.IS_ATHLETE] = "yes"
        }

        return ProfileState(
            answers = answers,
            customSport = customSport,
            lastPeriodDate = lastPeriodDateMillis
        )
    }

    private fun normalizePhysicalActivityOptionId(raw: String): String {
        val normalized = normalizeOptionId(raw)
        return if (normalized == "yoga") "yoga_stretching" else normalized
    }

    private fun normalizeOptionId(raw: String): String {
        return raw.trim().lowercase()
    }

    private fun parseLastPeriodToMillis(lastPeriod: String): Long? {
        if (lastPeriod.isBlank()) return null
        return runCatching {
            val localDate = LocalDate.parse(lastPeriod)
            localDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    private fun com.example.syncd.data.model.AgeGroup.toApiValue(): String = when (this) {
        com.example.syncd.data.model.AgeGroup.UNDER_18 -> "under_18"
        com.example.syncd.data.model.AgeGroup.AGE_18_24 -> "18_24"
        com.example.syncd.data.model.AgeGroup.AGE_25_34 -> "25_34"
    }

    private fun com.example.syncd.data.model.CycleStage.toApiValue(): String = when (this) {
        com.example.syncd.data.model.CycleStage.REGULAR -> "regular"
        com.example.syncd.data.model.CycleStage.IRREGULAR -> "irregular"
        com.example.syncd.data.model.CycleStage.PREGNANT -> "pregnant"
        com.example.syncd.data.model.CycleStage.TRYING_TO_CONCEIVE -> "trying_to_conceive"
        com.example.syncd.data.model.CycleStage.PERIMENOPAUSE -> "perimenopause"
        com.example.syncd.data.model.CycleStage.POSTPARTUM -> "postpartum"
    }

    private fun PhysicalActivity.toApiValue(): String = when (this) {
        PhysicalActivity.DAILY_RUNNING -> "daily_running"
        PhysicalActivity.GYM_FITNESS -> "gym_fitness"
        PhysicalActivity.WALKING -> "walking"
        PhysicalActivity.YOGA -> "yoga_stretching"
        PhysicalActivity.NONE -> "none"
    }

    private fun TrainingFrequency.toApiValue(): String = when (this) {
        TrainingFrequency.ONE_TWO_PER_WEEK -> "1_2_per_week"
        TrainingFrequency.THREE_FOUR_PER_WEEK -> "3_4_per_week"
        TrainingFrequency.FIVE_SIX_PER_WEEK -> "5_6_per_week"
        TrainingFrequency.DAILY -> "daily"
        TrainingFrequency.TWICE_DAILY -> "twice_daily"
    }
}
