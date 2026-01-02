package com.example.syncd.screen.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.data.repository.UserProfileRepository
import com.example.syncd.screen.home.data.repository.HomeRepository
import com.example.syncd.screen.log.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CycleOverview(
    val averageCycleLength: String = "around 28 days",
    val averageBleedingDays: String = "about 4–5 days",
    val currentPhase: String = "Luteal Phase"
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
                        val currentPhase = when (phaseInfo.phase) {
                            "menstrual" -> "Menstrual Phase"
                            "follicular" -> "Follicular Phase"
                            "ovulation" -> "Ovulation Phase"
                            "luteal" -> "Luteal Phase"
                            else -> "Day ${phaseInfo.dayOfCycle}"
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
                        _state.value = createMockInsightsState("Luteal Phase", isAthlete)
                    }
                }
                .onFailure {
                    _state.value = createMockInsightsState("Luteal Phase", isAthlete)
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
                averageCycleLength = "around 28 days",
                averageBleedingDays = "about 4–5 days",
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
                            text = "Pain levels are usually highest on Day 1-2 of your period, then gradually reduce.",
                            emoji = "💫"
                        ),
                        PatternInsight(
                            id = "energy_low_menstrual",
                            text = "Your energy typically dips during menstruation. This is completely normal.",
                            emoji = "🔋"
                        ),
                        PatternInsight(
                            id = "mood_improves_post",
                            text = "Mood and energy usually lift noticeably after your period ends.",
                            emoji = "🌱"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_recovery",
                            text = "For athletes: Menstrual phase is your active recovery window. Muscles repair best when you rest.",
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
                            text = "Energy levels rise steadily during your follicular phase. You're more productive these days.",
                            emoji = "✨"
                        ),
                        PatternInsight(
                            id = "mood_positive",
                            text = "Your mood tends to be more positive and stable in the follicular phase.",
                            emoji = "🌸"
                        ),
                        PatternInsight(
                            id = "sleep_better",
                            text = "Sleep quality is often better in this phase. You wake up feeling more refreshed.",
                            emoji = "😴"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_strength",
                            text = "For athletes: Follicular phase is prime time for strength gains and skill-building. Your body adapts best to training now.",
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
                            text = "You hit peak energy and confidence around ovulation. Use this window strategically.",
                            emoji = "⚡"
                        ),
                        PatternInsight(
                            id = "social_ease",
                            text = "Social interactions feel easier during ovulation. You're more outgoing these days.",
                            emoji = "💬"
                        ),
                        PatternInsight(
                            id = "skin_glow",
                            text = "Your skin often looks clearer and brighter during the ovulation phase.",
                            emoji = "✨"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_peak_warning",
                            text = "For athletes: Peak performance window BUT higher injury risk. Warm up thoroughly—ligaments are more lax now.",
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
                            text = "Energy gradually decreases through the luteal phase. Rest becomes more important.",
                            emoji = "🔋"
                        ),
                        PatternInsight(
                            id = "cravings_luteal",
                            text = "Cravings for sweets or carbs peak in late luteal phase. This is hormonal, not lack of willpower.",
                            emoji = "🍫"
                        ),
                        PatternInsight(
                            id = "mood_sensitive",
                            text = "You're more emotionally sensitive in the luteal phase. Extra self-care helps.",
                            emoji = "💭"
                        ),
                        PatternInsight(
                            id = "bloating_common",
                            text = "Bloating and water retention often appear 5-7 days before your period.",
                            emoji = "💧"
                        )
                    )
                )
                
                if (isAthlete) {
                    basePatterns.add(
                        PatternInsight(
                            id = "athlete_endurance",
                            text = "For athletes: Shift to endurance work and technique drills. Recovery takes longer—reduce training volume by 20-30%.",
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
                title = "Your Last Cycle",
                text = "You navigated your last cycle with awareness and self-care. The days you rested when needed made a difference.",
                encouragement = "Every cycle teaches you something about your body 🌸"
            )
            "follicular" -> CycleReflection(
                title = "Observing Your Patterns",
                text = "Your follicular phase energy has been consistent. You're learning to time important tasks with this natural rhythm.",
                encouragement = "Understanding your cycle is empowering 🌱"
            )
            "ovulation" -> CycleReflection(
                title = "Peak Phase Insights",
                text = "Your ovulation phase brought clarity and energy this cycle. You made the most of your high-performance window.",
                encouragement = "You're learning to work with your body, not against it ✨"
            )
            else -> CycleReflection(
                title = "Your Last Cycle",
                text = "The luteal phase had some challenging days, especially toward the end. You showed up for yourself anyway—that matters.",
                encouragement = "Every cycle is different, and that's completely normal 🌸"
            )
        }
    }

    private fun getEducationalArticles(isAthlete: Boolean = false): List<EducationalArticle> {
        val baseArticles = listOf(
            EducationalArticle(
                id = "menstrual_phase",
                title = "Understanding Menstruation in India",
                emoji = "🩸"
            ),
            EducationalArticle(
                id = "luteal_phase",
                title = "Why PMS Happens & How to Manage It",
                emoji = "🌙"
            ),
            EducationalArticle(
                id = "energy_changes",
                title = "Energy Through Your Cycle",
                emoji = "⚡"
            ),
            EducationalArticle(
                id = "movement_cycle",
                title = "Exercise & Your Menstrual Cycle",
                emoji = "🏃"
            ),
            EducationalArticle(
                id = "nutrition_phases",
                title = "Indian Foods for Each Phase",
                emoji = "🍲"
            ),
            EducationalArticle(
                id = "pcos_awareness",
                title = "PCOS: Signs & When to See a Doctor",
                emoji = "⚕️"
            )
        )
        
        val athleteArticles = if (isAthlete) {
            listOf(
                EducationalArticle(
                    id = "athlete_periodization",
                    title = "Cycle-Based Training for Athletes",
                    emoji = "💪"
                ),
                EducationalArticle(
                    id = "injury_prevention",
                    title = "Preventing Injuries Across Your Cycle",
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
            text = "You've logged severe pain or very heavy flow multiple times recently. This can be normal, but persistent symptoms deserve attention.",
            suggestion = "Consider discussing this with a gynecologist to rule out conditions like PCOS, endometriosis or fibroids."
        )
    }

    private fun createLowDataState(currentPhase: String): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLength = "still learning",
                averageBleedingDays = "still learning",
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
                averageCycleLength = "around 28 days",
                averageBleedingDays = "about 4–5 days",
                currentPhase = currentPhase
            ),
            patterns = listOf(
                PatternInsight(
                    id = "energy_luteal",
                    text = "Energy levels tend to be lower during your luteal phase. This is hormonal and completely normal.",
                    emoji = "🔋"
                ),
                PatternInsight(
                    id = "pain_period",
                    text = "Pain is usually stronger in the first 1–2 days of your period, then eases up.",
                    emoji = "💫"
                ),
                PatternInsight(
                    id = "energy_post_period",
                    text = "Your energy improves noticeably after your period ends. Plan accordingly.",
                    emoji = "✨"
                )
            ),
            hasEnoughData = true,
            lastCycleReflection = CycleReflection(
                title = "Your Last Cycle",
                text = "Your last cycle had a few tough days, especially around your period. You listened to your body and that matters.",
                encouragement = "Every cycle teaches us something about ourselves 🌸"
            ),
            educationalArticles = getEducationalArticles(isAthlete),
            safetyInsight = null
        )
    }
}
