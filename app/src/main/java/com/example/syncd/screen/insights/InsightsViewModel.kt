package com.example.syncd.screen.insights

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
    val averageCycleLength: String = "",
    val averageBleedingDays: String = "",
    val currentPhase: String = ""
)

data class PatternInsight(
    val id: String,
    val text: String,
    val emoji: String = "🔮"
)

data class CycleReflection(
    val title: String,
    val text: String,
    val encouragement: String
)

data class EducationalArticle(
    val id: String,
    val title: String,
    val emoji: String
)

data class SafetyInsight(
    val text: String,
    val suggestion: String
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
    application: Application,
    private val homeRepository: HomeRepository,
    private val logRepository: LogRepository,
    private val userProfileRepository: UserProfileRepository
) : AndroidViewModel(application) {

    private val context = application.applicationContext

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
                val currentPhase = when (phaseInfo.phase) {
                    "menstrual" -> context.getString(R.string.phase_menstrual)
                    "follicular" -> context.getString(R.string.phase_follicular)
                    "ovulation" -> context.getString(R.string.phase_ovulation)
                    "luteal" -> context.getString(R.string.phase_luteal)
                    else -> context.getString(R.string.insights_day_of_cycle, phaseInfo.dayOfCycle)
                }

                        logRepository.listLogs()
                            .onSuccess { logs ->
                                val hasData = logs.isNotEmpty()
                                _state.value = if (hasData) {
                                    createInsightsFromLogs(currentPhase, logs.size, phaseInfo.phase, isAthlete)
                                } else {
                                    createLowDataState(currentPhase)
                                }
                            }
                            .onFailure {
                                _state.value = createMockInsightsState(currentPhase, isAthlete)
                            }
                    } else {
                    _state.value = createMockInsightsState(context.getString(R.string.phase_luteal), isAthlete)
                    }
                }
                .onFailure {
                    _state.value = createMockInsightsState(context.getString(R.string.phase_luteal), isAthlete)
                }
        }
    }

    private fun createInsightsFromLogs(
        currentPhase: String,
        logCount: Int,
        phase: String,
        isAthlete: Boolean
    ): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLength = context.getString(R.string.insights_cycle_avg_length),
                averageBleedingDays = context.getString(R.string.insights_cycle_avg_bleeding),
                currentPhase = currentPhase
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
                            text = context.getString(R.string.insight_menstrual_pain_first_days),
                            emoji = "💫"
                        ),
                        PatternInsight(
                            id = "energy_low_menstrual",
                            text = context.getString(R.string.insight_menstrual_energy_low),
                            emoji = "🔋"
                        ),
                        PatternInsight(
                            id = "mood_improves_post",
                            text = context.getString(R.string.insight_menstrual_mood_improves),
                            emoji = "🌱"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_recovery",
                            text = context.getString(R.string.insight_menstrual_athlete_recovery),
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
                            text = context.getString(R.string.insight_follicular_energy_rise),
                            emoji = "✨"
                        ),
                        PatternInsight(
                            id = "mood_positive",
                            text = context.getString(R.string.insight_follicular_mood_positive),
                            emoji = "🌸"
                        ),
                        PatternInsight(
                            id = "sleep_better",
                            text = context.getString(R.string.insight_follicular_sleep_better),
                            emoji = "😴"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_strength",
                            text = context.getString(R.string.insight_follicular_athlete_strength),
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
                            text = context.getString(R.string.insight_ovulation_peak_energy),
                            emoji = "⚡"
                        ),
                        PatternInsight(
                            id = "social_ease",
                            text = context.getString(R.string.insight_ovulation_social_ease),
                            emoji = "💬"
                        ),
                        PatternInsight(
                            id = "skin_glow",
                            text = context.getString(R.string.insight_ovulation_skin_glow),
                            emoji = "✨"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_peak_warning",
                            text = context.getString(R.string.insight_ovulation_athlete_peak_warning),
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
                            text = context.getString(R.string.insight_luteal_energy_decrease),
                            emoji = "🔋"
                        ),
                        PatternInsight(
                            id = "cravings_luteal",
                            text = context.getString(R.string.insight_luteal_cravings),
                            emoji = "🍫"
                        ),
                        PatternInsight(
                            id = "mood_sensitive",
                            text = context.getString(R.string.insight_luteal_mood_sensitive),
                            emoji = "💭"
                        ),
                        PatternInsight(
                            id = "bloating_common",
                            text = context.getString(R.string.insight_luteal_bloating),
                            emoji = "💧"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_endurance",
                            text = context.getString(R.string.insight_luteal_athlete_endurance),
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
                title = context.getString(R.string.reflection_menstrual_title),
                text = context.getString(R.string.reflection_menstrual_text),
                encouragement = context.getString(R.string.reflection_menstrual_encouragement)
            )
            "follicular" -> CycleReflection(
                title = context.getString(R.string.reflection_follicular_title),
                text = context.getString(R.string.reflection_follicular_text),
                encouragement = context.getString(R.string.reflection_follicular_encouragement)
            )
            "ovulation" -> CycleReflection(
                title = context.getString(R.string.reflection_ovulation_title),
                text = context.getString(R.string.reflection_ovulation_text),
                encouragement = context.getString(R.string.reflection_ovulation_encouragement)
            )
            else -> CycleReflection(
                title = context.getString(R.string.reflection_luteal_title),
                text = context.getString(R.string.reflection_luteal_text),
                encouragement = context.getString(R.string.reflection_luteal_encouragement)
            )
        }
    }

    private fun getEducationalArticles(isAthlete: Boolean = false): List<EducationalArticle> {
        val baseArticles = listOf(
            EducationalArticle(
                id = "menstrual_phase",
                title = context.getString(R.string.article_menstrual_phase),
                emoji = "🩸"
            ),
            EducationalArticle(
                id = "luteal_phase",
                title = context.getString(R.string.article_luteal_phase),
                emoji = "🌙"
            ),
            EducationalArticle(
                id = "energy_changes",
                title = context.getString(R.string.article_energy_changes),
                emoji = "⚡"
            ),
            EducationalArticle(
                id = "movement_cycle",
                title = context.getString(R.string.article_movement_cycle),
                emoji = "🏃"
            ),
            EducationalArticle(
                id = "nutrition_phases",
                title = context.getString(R.string.article_nutrition_phases),
                emoji = "🍲"
            ),
            EducationalArticle(
                id = "pcos_awareness",
                title = context.getString(R.string.article_pcos_awareness),
                emoji = "⚕️"
            )
        )
        
        val athleteArticles = if (isAthlete) {
            listOf(
                EducationalArticle(
                    id = "athlete_periodization",
                    title = context.getString(R.string.article_athlete_periodization),
                    emoji = "💪"
                ),
                EducationalArticle(
                    id = "injury_prevention",
                    title = context.getString(R.string.article_injury_prevention),
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
            text = context.getString(R.string.safety_insight_severe_pain),
            suggestion = context.getString(R.string.safety_insight_suggestion)
        )
    }

    private fun createLowDataState(currentPhase: String): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLength = context.getString(R.string.insights_cycle_still_learning),
                averageBleedingDays = context.getString(R.string.insights_cycle_still_learning),
                currentPhase = currentPhase
            ),
            patterns = emptyList(),
            hasEnoughData = false,
            lastCycleReflection = null,
            educationalArticles = getEducationalArticles().take(3),
            safetyInsight = null
        )
    }

    private fun createMockInsightsState(currentPhase: String, isAthlete: Boolean = false): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLength = context.getString(R.string.insights_cycle_avg_length),
                averageBleedingDays = context.getString(R.string.insights_cycle_avg_bleeding),
                currentPhase = currentPhase
            ),
            patterns = listOf(
                PatternInsight(
                    id = "energy_luteal",
                    text = context.getString(R.string.insight_mock_energy_luteal),
                    emoji = "🔋"
                ),
                PatternInsight(
                    id = "pain_period",
                    text = context.getString(R.string.insight_mock_pain_period),
                    emoji = "💫"
                ),
                PatternInsight(
                    id = "energy_post_period",
                    text = context.getString(R.string.insight_mock_energy_post_period),
                    emoji = "✨"
                )
            ),
            hasEnoughData = true,
            lastCycleReflection = CycleReflection(
                title = context.getString(R.string.reflection_mock_title),
                text = context.getString(R.string.reflection_mock_text),
                encouragement = context.getString(R.string.reflection_mock_encouragement)
            ),
            educationalArticles = getEducationalArticles(isAthlete),
            safetyInsight = null
        )
    }
}
