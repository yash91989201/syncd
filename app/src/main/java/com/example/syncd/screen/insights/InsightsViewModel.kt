package com.example.syncd.screen.insights

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class InsightsViewModel : ViewModel() {

    private val _state = MutableStateFlow(createInsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    private fun createInsightsState(): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLength = "around 28 days",
                averageBleedingDays = "about 4–5 days",
                currentPhase = "Luteal Phase"
            ),
            patterns = listOf(
                PatternInsight(
                    id = "energy_luteal",
                    text = "You often feel lower energy during your luteal phase.",
                    emoji = "🔋"
                ),
                PatternInsight(
                    id = "pain_period",
                    text = "Pain is usually stronger in the first 1–2 days of your period.",
                    emoji = "💫"
                ),
                PatternInsight(
                    id = "energy_post_period",
                    text = "Your energy tends to improve after your period ends.",
                    emoji = "✨"
                ),
                PatternInsight(
                    id = "mood_follicular",
                    text = "Your mood often lifts during your follicular phase.",
                    emoji = "🌱"
                )
            ),
            hasEnoughData = true,
            lastCycleReflection = CycleReflection(
                title = "Your Last Cycle",
                text = "This cycle had a few challenging days, especially around your period. You showed up and listened to your body — that matters.",
                encouragement = "Every cycle teaches us something 🌸"
            ),
            educationalArticles = listOf(
                EducationalArticle(
                    id = "luteal_phase",
                    title = "Understanding the Luteal Phase",
                    emoji = "🌙"
                ),
                EducationalArticle(
                    id = "energy_changes",
                    title = "Why Energy Changes Through the Month",
                    emoji = "⚡"
                ),
                EducationalArticle(
                    id = "movement_cycle",
                    title = "How Your Cycle Affects Movement",
                    emoji = "🏃"
                ),
                EducationalArticle(
                    id = "nutrition_phases",
                    title = "Eating for Each Phase",
                    emoji = "🥗"
                )
            ),
            safetyInsight = null
        )
    }

    fun createStateWithLowData(): InsightsState {
        return InsightsState(
            cycleOverview = CycleOverview(
                averageCycleLength = "still learning",
                averageBleedingDays = "still learning",
                currentPhase = "Day 5"
            ),
            patterns = emptyList(),
            hasEnoughData = false,
            lastCycleReflection = null,
            educationalArticles = listOf(
                EducationalArticle(
                    id = "luteal_phase",
                    title = "Understanding the Luteal Phase",
                    emoji = "🌙"
                ),
                EducationalArticle(
                    id = "energy_changes",
                    title = "Why Energy Changes Through the Month",
                    emoji = "⚡"
                )
            ),
            safetyInsight = null
        )
    }

    fun createStateWithSafetyInsight(): InsightsState {
        return _state.value.copy(
            safetyInsight = SafetyInsight(
                text = "Some of the symptoms you've been logging can be difficult to manage alone.",
                suggestion = "It may help to talk to a doctor about this."
            )
        )
    }
}
