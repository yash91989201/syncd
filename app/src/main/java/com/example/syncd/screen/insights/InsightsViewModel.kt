package com.example.syncd.screen.insights

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.R
import com.example.syncd.data.repository.UserProfileRepository
import com.example.syncd.screen.home.data.repository.HomeRepository
import com.example.syncd.screen.log.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CycleOverview(
    @StringRes val averageCycleLengthResId: Int = 0,
    @StringRes val averageBleedingDaysResId: Int = 0,
    @StringRes val currentPhaseResId: Int = 0,
    val currentPhaseArgs: Array<Any>? = null // For formatted strings like "Day X of cycle"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CycleOverview
        if (averageCycleLengthResId != other.averageCycleLengthResId) return false
        if (averageBleedingDaysResId != other.averageBleedingDaysResId) return false
        if (currentPhaseResId != other.currentPhaseResId) return false
        if (currentPhaseArgs != null) {
            if (other.currentPhaseArgs == null) return false
            if (!currentPhaseArgs.contentEquals(other.currentPhaseArgs)) return false
        } else if (other.currentPhaseArgs != null) return false
        return true
    }
    override fun hashCode(): Int {
        var result = averageCycleLengthResId
        result = 31 * result + averageBleedingDaysResId
        result = 31 * result + currentPhaseResId
        result = 31 * result + (currentPhaseArgs?.contentHashCode() ?: 0)
        return result
    }
}

data class PatternInsight(
    val id: String,
    @StringRes val textResId: Int,
    val emoji: String = "🔮"
)

data class CycleReflection(
    @StringRes val titleResId: Int,
    @StringRes val textResId: Int,
    @StringRes val encouragementResId: Int
)

data class EducationalArticle(
    val id: String,
    @StringRes val titleResId: Int,
    val emoji: String
)

data class SafetyInsight(
    @StringRes val textResId: Int,
    @StringRes val suggestionResId: Int
)

data class InsightsState(
    val cycleOverview: CycleOverview = CycleOverview(),
    val patterns: List<PatternInsight> = emptyList(),
    val hasEnoughData: Boolean = true,
    val lastCycleReflection: CycleReflection? = null,
    val educationalArticles: List<EducationalArticle> = emptyList(),
    val safetyInsight: SafetyInsight? = null
)

class InsightsViewModel(
    private val homeRepository: HomeRepository,
    private val logRepository: LogRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            val userProfile = userProfileRepository.getUserProfile().getOrNull()
            val isAthlete = userProfile?.userProfile?.isAthlete ?: false
            
            homeRepository.getPhaseInfo()
                .onSuccess { phaseInfo ->
                    if (phaseInfo != null) {
                        val (currentPhaseResId, phaseArgs) = when (phaseInfo.phase) {
                            "menstrual" -> R.string.phase_menstrual to null
                            "follicular" -> R.string.phase_follicular to null
                            "ovulation" -> R.string.phase_ovulation to null
                            "luteal" -> R.string.phase_luteal to null
                            else -> R.string.insights_day_of_cycle to arrayOf<Any>(phaseInfo.dayOfCycle)
                        }

                        logRepository.listLogs()
                            .onSuccess { logs ->
                                val hasData = logs.isNotEmpty()
                                _state.value = if (hasData) {
                                    createInsightsFromLogs(currentPhaseResId, phaseArgs, logs.size, phaseInfo.phase, isAthlete)
                                } else {
                                    createLowDataState(currentPhaseResId, phaseArgs)
                                }
                            }
                            .onFailure {
                                _state.value = createMockInsightsState(currentPhaseResId, phaseArgs, isAthlete)
                            }
                    } else {
                        _state.value = createMockInsightsState(R.string.phase_luteal, null, isAthlete)
                    }
                }
                .onFailure {
                    _state.value = createMockInsightsState(R.string.phase_luteal, null, isAthlete)
                }
        }
    }

    private fun createInsightsFromLogs(
        @StringRes currentPhaseResId: Int,
        phaseArgs: Array<Any>?,
        logCount: Int,
        phase: String,
        isAthlete: Boolean
    ): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLengthResId = R.string.insights_cycle_avg_length,
                averageBleedingDaysResId = R.string.insights_cycle_avg_bleeding,
                currentPhaseResId = currentPhaseResId,
                currentPhaseArgs = phaseArgs
            ),
            patterns = generatePatternsForPhase(phase, logCount, isAthlete),
            hasEnoughData = logCount >= 5,
            lastCycleReflection = generateReflectionForPhase(phase),
            educationalArticles = getEducationalArticles(isAthlete),
            safetyInsight = if (shouldShowSafetyInsight(logCount)) generateSafetyInsight() else null
        )
    }

    private fun generatePatternsForPhase(phase: String, logCount: Int, isAthlete: Boolean): List<PatternInsight> {
        val basePatterns = mutableListOf<PatternInsight>()

        when (phase) {
            "menstrual" -> {
                basePatterns.addAll(
                    listOf(
                        PatternInsight(
                            id = "pain_first_days",
                            textResId = R.string.insight_menstrual_pain_first_days,
                            emoji = "💫"
                        ),
                        PatternInsight(
                            id = "energy_low_menstrual",
                            textResId = R.string.insight_menstrual_energy_low,
                            emoji = "🔋"
                        ),
                        PatternInsight(
                            id = "mood_improves_post",
                            textResId = R.string.insight_menstrual_mood_improves,
                            emoji = "🌱"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_recovery",
                            textResId = R.string.insight_menstrual_athlete_recovery,
                            emoji = "🏃‍♀️"
                        )
                    )
                }
            }
            "follicular" -> {
                basePatterns.addAll(
                    listOf(
                        PatternInsight(
                            id = "energy_follicular",
                            textResId = R.string.insight_follicular_energy_rise,
                            emoji = "✨"
                        ),
                        PatternInsight(
                            id = "mood_positive",
                            textResId = R.string.insight_follicular_mood_positive,
                            emoji = "🌸"
                        ),
                        PatternInsight(
                            id = "sleep_better",
                            textResId = R.string.insight_follicular_sleep_better,
                            emoji = "😴"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_strength",
                            textResId = R.string.insight_follicular_athlete_strength,
                            emoji = "💪"
                        )
                    )
                }
            }
            "ovulation" -> {
                basePatterns.addAll(
                    listOf(
                        PatternInsight(
                            id = "peak_energy",
                            textResId = R.string.insight_ovulation_peak_energy,
                            emoji = "⚡"
                        ),
                        PatternInsight(
                            id = "social_ease",
                            textResId = R.string.insight_ovulation_social_ease,
                            emoji = "💬"
                        ),
                        PatternInsight(
                            id = "skin_glow",
                            textResId = R.string.insight_ovulation_skin_glow,
                            emoji = "✨"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_peak_warning",
                            textResId = R.string.insight_ovulation_athlete_peak_warning,
                            emoji = "⚠️"
                        )
                    )
                }
            }
            else -> {
                basePatterns.addAll(
                    listOf(
                        PatternInsight(
                            id = "energy_luteal",
                            textResId = R.string.insight_luteal_energy_decrease,
                            emoji = "🔋"
                        ),
                        PatternInsight(
                            id = "cravings_luteal",
                            textResId = R.string.insight_luteal_cravings,
                            emoji = "🍫"
                        ),
                        PatternInsight(
                            id = "mood_sensitive",
                            textResId = R.string.insight_luteal_mood_sensitive,
                            emoji = "💭"
                        ),
                        PatternInsight(
                            id = "bloating_common",
                            textResId = R.string.insight_luteal_bloating,
                            emoji = "💧"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_endurance",
                            textResId = R.string.insight_luteal_athlete_endurance,
                            emoji = "🏃‍♀️"
                        )
                    )
                }
            }
        }

        return basePatterns.take(if (logCount < 10) 2 else if (logCount < 20) 3 else if (isAthlete) 5 else 4)
    }

    private fun generateReflectionForPhase(phase: String): CycleReflection {
        return when (phase) {
            "menstrual" -> CycleReflection(
                titleResId = R.string.reflection_menstrual_title,
                textResId = R.string.reflection_menstrual_text,
                encouragementResId = R.string.reflection_menstrual_encouragement
            )
            "follicular" -> CycleReflection(
                titleResId = R.string.reflection_follicular_title,
                textResId = R.string.reflection_follicular_text,
                encouragementResId = R.string.reflection_follicular_encouragement
            )
            "ovulation" -> CycleReflection(
                titleResId = R.string.reflection_ovulation_title,
                textResId = R.string.reflection_ovulation_text,
                encouragementResId = R.string.reflection_ovulation_encouragement
            )
            else -> CycleReflection(
                titleResId = R.string.reflection_luteal_title,
                textResId = R.string.reflection_luteal_text,
                encouragementResId = R.string.reflection_luteal_encouragement
            )
        }
    }

    private fun getEducationalArticles(isAthlete: Boolean = false): List<EducationalArticle> {
        val baseArticles = listOf(
            EducationalArticle(
                id = "menstrual_phase",
                titleResId = R.string.article_menstrual_phase,
                emoji = "🩸"
            ),
            EducationalArticle(
                id = "luteal_phase",
                titleResId = R.string.article_luteal_phase,
                emoji = "🌙"
            ),
            EducationalArticle(
                id = "energy_changes",
                titleResId = R.string.article_energy_changes,
                emoji = "⚡"
            ),
            EducationalArticle(
                id = "movement_cycle",
                titleResId = R.string.article_movement_cycle,
                emoji = "🏃"
            ),
            EducationalArticle(
                id = "nutrition_phases",
                titleResId = R.string.article_nutrition_phases,
                emoji = "🍲"
            ),
            EducationalArticle(
                id = "pcos_awareness",
                titleResId = R.string.article_pcos_awareness,
                emoji = "⚕️"
            )
        )
        
        val athleteArticles = if (isAthlete) {
            listOf(
                EducationalArticle(
                    id = "athlete_periodization",
                    titleResId = R.string.article_athlete_periodization,
                    emoji = "💪"
                ),
                EducationalArticle(
                    id = "injury_prevention",
                    titleResId = R.string.article_injury_prevention,
                    emoji = "⚠️"
                )
            )
        } else {
            emptyList()
        }
        
        return baseArticles + athleteArticles
    }

    private fun shouldShowSafetyInsight(logCount: Int): Boolean {
        return logCount > 15 && (0..10).random() > 7
    }

    private fun generateSafetyInsight(): SafetyInsight {
        return SafetyInsight(
            textResId = R.string.safety_insight_severe_pain,
            suggestionResId = R.string.safety_insight_suggestion
        )
    }

    private fun createLowDataState(@StringRes currentPhaseResId: Int, phaseArgs: Array<Any>?): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLengthResId = R.string.insights_cycle_still_learning,
                averageBleedingDaysResId = R.string.insights_cycle_still_learning,
                currentPhaseResId = currentPhaseResId,
                currentPhaseArgs = phaseArgs
            ),
            patterns = emptyList(),
            hasEnoughData = false,
            lastCycleReflection = null,
            educationalArticles = getEducationalArticles().take(3),
            safetyInsight = null
        )
    }

    private fun createMockInsightsState(@StringRes currentPhaseResId: Int, phaseArgs: Array<Any>?, isAthlete: Boolean = false): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLengthResId = R.string.insights_cycle_avg_length,
                averageBleedingDaysResId = R.string.insights_cycle_avg_bleeding,
                currentPhaseResId = currentPhaseResId,
                currentPhaseArgs = phaseArgs
            ),
            patterns = listOf(
                PatternInsight(
                    id = "energy_luteal",
                    textResId = R.string.insight_mock_energy_luteal,
                    emoji = "🔋"
                ),
                PatternInsight(
                    id = "pain_period",
                    textResId = R.string.insight_mock_pain_period,
                    emoji = "💫"
                ),
                PatternInsight(
                    id = "energy_post_period",
                    textResId = R.string.insight_mock_energy_post_period,
                    emoji = "✨"
                )
            ),
            hasEnoughData = true,
            lastCycleReflection = CycleReflection(
                titleResId = R.string.reflection_mock_title,
                textResId = R.string.reflection_mock_text,
                encouragementResId = R.string.reflection_mock_encouragement
            ),
            educationalArticles = getEducationalArticles(isAthlete),
            safetyInsight = null
        )
    }
}
