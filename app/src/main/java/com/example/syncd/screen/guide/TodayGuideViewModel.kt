package com.example.syncd.screen.guide

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GuideSection(
    val title: String,
    val items: List<String>,
    val footer: String? = null
)

data class DosDonts(
    val dos: List<String>,
    val donts: List<String>
)

data class TodayGuideState(
    val phaseName: String = "Luteal Phase",
    val cycleDay: Int = 23,
    val phaseInsight: String = "",
    val nutritionSection: GuideSection = GuideSection("", emptyList()),
    val movementSection: GuideSection = GuideSection("", emptyList()),
    val dosDonts: DosDonts = DosDonts(emptyList(), emptyList()),
    val gentleTips: List<String> = emptyList(),
    val isAthlete: Boolean = false,
    val athleteNote: String? = null
)

class TodayGuideViewModel : ViewModel() {

    private val _state = MutableStateFlow(createGuideForPhase("luteal", isAthlete = true))
    val state: StateFlow<TodayGuideState> = _state.asStateFlow()

    private fun createGuideForPhase(phase: String, isAthlete: Boolean): TodayGuideState {
        return when (phase.lowercase()) {
            "menstrual" -> TodayGuideState(
                phaseName = "Menstrual Phase",
                cycleDay = 3,
                phaseInsight = "Your body is renewing itself. Gentle care and warm, nourishing foods can help you feel more comfortable today.",
                nutritionSection = GuideSection(
                    title = "What to Eat Today",
                    items = listOf(
                        "Warm meals like dal, khichdi, or vegetable soup",
                        "Iron-rich foods like spinach, dates, or jaggery",
                        "Plenty of water or warm herbal teas",
                        "Comfort foods that feel soothing"
                    ),
                    footer = "Eat what feels comforting for your body today."
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Light movement if it feels good",
                            "Focus on gentle stretching or mobility",
                            "Recovery work supports long-term performance",
                            "Listen to your body's signals"
                        )
                    )
                } else {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Gentle walking or light stretching",
                            "Restorative yoga or slow movements",
                            "Rest is perfectly okay today",
                            "Move only if it feels good"
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        "Take short breaks throughout the day",
                        "Prioritize sleep and rest",
                        "Use a heating pad for comfort",
                        "Stay hydrated with warm fluids"
                    ),
                    donts = listOf(
                        "Push through strong discomfort",
                        "Skip meals even if appetite is low",
                        "Over-schedule yourself",
                        "Ignore what your body needs"
                    )
                ),
                gentleTips = listOf(
                    "A warm shower may ease body tension",
                    "Slowing down is part of your cycle, not a failure",
                    "Wearing comfortable clothes can make a difference",
                    "It's okay to say no to extra commitments"
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) "This phase supports recovery. Rest today helps you perform better later." else null
            )
            
            "follicular" -> TodayGuideState(
                phaseName = "Follicular Phase",
                cycleDay = 8,
                phaseInsight = "Energy is building up. This is a great time to try new things and enjoy activities that feel fun.",
                nutritionSection = GuideSection(
                    title = "What to Eat Today",
                    items = listOf(
                        "Fresh, light meals with plenty of vegetables",
                        "Protein-rich foods like eggs, paneer, or lentils",
                        "Fermented foods like curd or idli",
                        "Fresh fruits for natural energy"
                    ),
                    footer = "Your body is ready to absorb nutrients well."
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Good time for skill work or learning new techniques",
                            "Strength training can feel easier now",
                            "Try something challenging if you feel ready",
                            "Energy is building - use it wisely"
                        )
                    )
                } else {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Try a new workout or activity",
                            "Dance, swim, or go for a longer walk",
                            "Strength exercises can feel good",
                            "Enjoy movement that feels fun"
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        "Start new projects or goals",
                        "Schedule important meetings or tasks",
                        "Try activities that excite you",
                        "Connect with friends and socialize"
                    ),
                    donts = listOf(
                        "Overcommit just because energy is high",
                        "Neglect sleep in favor of productivity",
                        "Skip meals to save time",
                        "Forget to pace yourself"
                    )
                ),
                gentleTips = listOf(
                    "This is a good time for planning ahead",
                    "Your focus may be sharper these days",
                    "Creativity often flows easier now",
                    "Enjoy the rising energy while it lasts"
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) "Your body responds well to training stimulus. Good time for progressive overload." else null
            )
            
            "ovulatory" -> TodayGuideState(
                phaseName = "Ovulatory Phase",
                cycleDay = 14,
                phaseInsight = "Energy is at its peak. You may feel more social and confident. Enjoy this vibrant phase.",
                nutritionSection = GuideSection(
                    title = "What to Eat Today",
                    items = listOf(
                        "Light, fresh meals with raw vegetables",
                        "Antioxidant-rich foods like berries and greens",
                        "Lean proteins and whole grains",
                        "Stay well hydrated"
                    ),
                    footer = "Your metabolism is active - fuel it well."
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Peak performance window for many",
                            "High-intensity work can feel achievable",
                            "Good time for competitions or testing",
                            "Note: injury risk slightly higher - warm up well"
                        )
                    )
                } else {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Higher intensity workouts may feel great",
                            "Group fitness or team sports",
                            "Challenge yourself if you feel ready",
                            "Warm up properly before activity"
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        "Take on challenging tasks",
                        "Have important conversations",
                        "Enjoy social activities",
                        "Make the most of high energy"
                    ),
                    donts = listOf(
                        "Skip warm-ups before exercise",
                        "Ignore hydration needs",
                        "Overextend without recovery plans",
                        "Forget that this phase is temporary"
                    )
                ),
                gentleTips = listOf(
                    "Communication may come easier now",
                    "Your confidence may naturally rise",
                    "This is often a feel-good phase",
                    "Plan lighter days for next week"
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) "Peak energy phase. Great for performance, but warm up thoroughly - ligaments need extra care." else null
            )
            
            else -> TodayGuideState(
                phaseName = "Luteal Phase",
                cycleDay = 23,
                phaseInsight = "This phase often comes with lower energy. Gentle routines and nourishing food can help you feel steadier today.",
                nutritionSection = GuideSection(
                    title = "What to Eat Today",
                    items = listOf(
                        "Warm meals like dal, khichdi, or vegetable curry",
                        "Complex carbs like roti, rice, or sweet potato",
                        "Magnesium-rich foods like nuts and dark chocolate",
                        "Avoid excessive salt and caffeine"
                    ),
                    footer = "Eat what feels comforting for your body today."
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Reduce intensity if energy feels low",
                            "Focus on technique or mobility work",
                            "Recovery supports long-term performance",
                            "Moderate steady-state work can help mood"
                        )
                    )
                } else {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Gentle walking or stretching",
                            "Light yoga or mobility exercises",
                            "Swimming or cycling at easy pace",
                            "Rest is okay if you need it"
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        "Take short breaks throughout the day",
                        "Prioritize sleep and wind-down time",
                        "Prepare for your upcoming period",
                        "Be patient with yourself"
                    ),
                    donts = listOf(
                        "Push through strong discomfort",
                        "Skip meals even if cravings arise",
                        "Overload your schedule",
                        "Criticize yourself for lower energy"
                    )
                ),
                gentleTips = listOf(
                    "A warm shower may ease body tension",
                    "Slowing down is part of your cycle, not a failure",
                    "Planning lighter days ahead can help",
                    "Cravings are normal - honor them gently"
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) "This phase supports recovery. Reducing intensity now helps you perform better in your next cycle." else null
            )
        }
    }
}
