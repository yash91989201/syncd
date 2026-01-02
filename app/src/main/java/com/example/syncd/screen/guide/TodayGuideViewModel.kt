package com.example.syncd.screen.guide

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.R
import com.example.syncd.data.repository.UserProfileRepository
import com.example.syncd.screen.home.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class TodayGuideViewModel(
    application: Application,
    private val homeRepository: HomeRepository,
    private val userProfileRepository: UserProfileRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(createGuideForPhase("luteal", 23, isAthlete = false))
    val state: StateFlow<TodayGuideState> = _state.asStateFlow()
    
    private val context = application.applicationContext

    init {
        loadPhaseData()
    }

    private fun loadPhaseData() {
        viewModelScope.launch {
            val userProfile = userProfileRepository.getUserProfile().getOrNull()
            val isAthlete = userProfile?.userProfile?.isAthlete ?: false
            
            homeRepository.getPhaseInfo()
                .onSuccess { phaseInfo ->
                    if (phaseInfo != null) {
                        _state.value = createGuideForPhase(
                            phase = phaseInfo.phase,
                            cycleDay = phaseInfo.dayOfCycle,
                            isAthlete = isAthlete
                        )
                    }
                }
                .onFailure {
                }
        }
    }

    private fun createGuideForPhase(phase: String, cycleDay: Int, isAthlete: Boolean): TodayGuideState {
        return when (phase.lowercase()) {
            "menstrual" -> TodayGuideState(
                phaseName = context.getString(R.string.phase_menstrual),
                cycleDay = cycleDay,
                phaseInsight = context.getString(R.string.menstrual_insight),
                nutritionSection = GuideSection(
                    title = context.getString(R.string.menstrual_nutrition_title),
                    items = listOf(
                        context.getString(R.string.menstrual_nutrition_1),
                        context.getString(R.string.menstrual_nutrition_2),
                        context.getString(R.string.menstrual_nutrition_3),
                        context.getString(R.string.menstrual_nutrition_4),
                        context.getString(R.string.menstrual_nutrition_5),
                        context.getString(R.string.menstrual_nutrition_6)
                    ),
                    footer = context.getString(R.string.menstrual_nutrition_footer)
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = context.getString(R.string.menstrual_movement_title),
                        items = listOf(
                            context.getString(R.string.menstrual_movement_athlete_1),
                            context.getString(R.string.menstrual_movement_athlete_2),
                            context.getString(R.string.menstrual_movement_athlete_3),
                            context.getString(R.string.menstrual_movement_athlete_4),
                            context.getString(R.string.menstrual_movement_athlete_5)
                        )
                    )
                } else {
                    GuideSection(
                        title = context.getString(R.string.menstrual_movement_title),
                        items = listOf(
                            context.getString(R.string.menstrual_movement_regular_1),
                            context.getString(R.string.menstrual_movement_regular_2),
                            context.getString(R.string.menstrual_movement_regular_3),
                            context.getString(R.string.menstrual_movement_regular_4),
                            context.getString(R.string.menstrual_movement_regular_5)
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        context.getString(R.string.menstrual_do_1),
                        context.getString(R.string.menstrual_do_2),
                        context.getString(R.string.menstrual_do_3),
                        context.getString(R.string.menstrual_do_4),
                        context.getString(R.string.menstrual_do_5)
                    ),
                    donts = listOf(
                        context.getString(R.string.menstrual_dont_1),
                        context.getString(R.string.menstrual_dont_2),
                        context.getString(R.string.menstrual_dont_3),
                        context.getString(R.string.menstrual_dont_4),
                        context.getString(R.string.menstrual_dont_5)
                    )
                ),
                gentleTips = listOf(
                    context.getString(R.string.menstrual_tip_regular_1),
                    context.getString(R.string.menstrual_tip_regular_2),
                    context.getString(R.string.menstrual_tip_regular_3),
                    context.getString(R.string.menstrual_tip_regular_4),
                    context.getString(R.string.menstrual_tip_regular_5)
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) context.getString(R.string.menstrual_athlete_note) else null
            )
            
            "follicular" -> TodayGuideState(
                phaseName = context.getString(R.string.phase_follicular),
                cycleDay = cycleDay,
                phaseInsight = context.getString(R.string.follicular_insight),
                nutritionSection = GuideSection(
                    title = context.getString(R.string.follicular_nutrition_title),
                    items = listOf(
                        context.getString(R.string.follicular_nutrition_1),
                        context.getString(R.string.follicular_nutrition_2),
                        context.getString(R.string.follicular_nutrition_3),
                        context.getString(R.string.follicular_nutrition_4),
                        context.getString(R.string.follicular_nutrition_5),
                        context.getString(R.string.follicular_nutrition_6)
                    ),
                    footer = context.getString(R.string.follicular_nutrition_footer)
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = context.getString(R.string.follicular_movement_title),
                        items = listOf(
                            context.getString(R.string.follicular_movement_athlete_1),
                            context.getString(R.string.follicular_movement_athlete_2),
                            context.getString(R.string.follicular_movement_athlete_3),
                            context.getString(R.string.follicular_movement_athlete_4),
                            context.getString(R.string.follicular_movement_athlete_5)
                        )
                    )
                } else {
                    GuideSection(
                        title = context.getString(R.string.follicular_movement_title),
                        items = listOf(
                            context.getString(R.string.follicular_movement_regular_1),
                            context.getString(R.string.follicular_movement_regular_2),
                            context.getString(R.string.follicular_movement_regular_3),
                            context.getString(R.string.follicular_movement_regular_4),
                            context.getString(R.string.follicular_movement_regular_5)
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        context.getString(R.string.follicular_do_1),
                        context.getString(R.string.follicular_do_2),
                        context.getString(R.string.follicular_do_3),
                        context.getString(R.string.follicular_do_4),
                        context.getString(R.string.follicular_do_5)
                    ),
                    donts = listOf(
                        context.getString(R.string.follicular_dont_1),
                        context.getString(R.string.follicular_dont_2),
                        context.getString(R.string.follicular_dont_3),
                        context.getString(R.string.follicular_dont_4),
                        context.getString(R.string.follicular_dont_5)
                    )
                ),
                gentleTips = listOf(
                    context.getString(R.string.follicular_tip_regular_1),
                    context.getString(R.string.follicular_tip_regular_2),
                    context.getString(R.string.follicular_tip_regular_3),
                    context.getString(R.string.follicular_tip_regular_4),
                    context.getString(R.string.follicular_tip_regular_5)
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) context.getString(R.string.follicular_athlete_note) else null
            )
            
            "ovulation" -> TodayGuideState(
                phaseName = context.getString(R.string.phase_ovulation),
                cycleDay = cycleDay,
                phaseInsight = context.getString(R.string.ovulation_insight),
                nutritionSection = GuideSection(
                    title = context.getString(R.string.ovulation_nutrition_title),
                    items = listOf(
                        context.getString(R.string.ovulation_nutrition_1),
                        context.getString(R.string.ovulation_nutrition_2),
                        context.getString(R.string.ovulation_nutrition_3),
                        context.getString(R.string.ovulation_nutrition_4),
                        context.getString(R.string.ovulation_nutrition_5),
                        context.getString(R.string.ovulation_nutrition_6)
                    ),
                    footer = context.getString(R.string.ovulation_nutrition_footer)
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = context.getString(R.string.ovulation_movement_title),
                        items = listOf(
                            context.getString(R.string.ovulation_movement_athlete_1),
                            context.getString(R.string.ovulation_movement_athlete_2),
                            context.getString(R.string.ovulation_movement_athlete_3),
                            context.getString(R.string.ovulation_movement_athlete_4),
                            context.getString(R.string.ovulation_movement_athlete_5)
                        )
                    )
                } else {
                    GuideSection(
                        title = context.getString(R.string.ovulation_movement_title),
                        items = listOf(
                            context.getString(R.string.ovulation_movement_regular_1),
                            context.getString(R.string.ovulation_movement_regular_2),
                            context.getString(R.string.ovulation_movement_regular_3),
                            context.getString(R.string.ovulation_movement_regular_4),
                            context.getString(R.string.ovulation_movement_regular_5)
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        context.getString(R.string.ovulation_do_1),
                        context.getString(R.string.ovulation_do_2),
                        context.getString(R.string.ovulation_do_3),
                        context.getString(R.string.ovulation_do_4),
                        context.getString(R.string.ovulation_do_5)
                    ),
                    donts = listOf(
                        context.getString(R.string.ovulation_dont_1),
                        context.getString(R.string.ovulation_dont_2),
                        context.getString(R.string.ovulation_dont_3),
                        context.getString(R.string.ovulation_dont_4),
                        context.getString(R.string.ovulation_dont_5)
                    )
                ),
                gentleTips = listOf(
                    context.getString(R.string.ovulation_tip_regular_1),
                    context.getString(R.string.ovulation_tip_regular_2),
                    context.getString(R.string.ovulation_tip_regular_3),
                    context.getString(R.string.ovulation_tip_regular_4),
                    context.getString(R.string.ovulation_tip_regular_5),
                    context.getString(R.string.ovulation_tip_regular_6)
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) context.getString(R.string.ovulation_athlete_note) else null
            )
            
            else -> TodayGuideState(
                phaseName = context.getString(R.string.phase_luteal),
                cycleDay = cycleDay,
                phaseInsight = context.getString(R.string.luteal_insight),
                nutritionSection = GuideSection(
                    title = context.getString(R.string.luteal_nutrition_title),
                    items = listOf(
                        context.getString(R.string.luteal_nutrition_1),
                        context.getString(R.string.luteal_nutrition_2),
                        context.getString(R.string.luteal_nutrition_3),
                        context.getString(R.string.luteal_nutrition_4),
                        context.getString(R.string.luteal_nutrition_5),
                        context.getString(R.string.luteal_nutrition_6)
                    ),
                    footer = context.getString(R.string.luteal_nutrition_footer)
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = context.getString(R.string.luteal_movement_title),
                        items = listOf(
                            context.getString(R.string.luteal_movement_athlete_1),
                            context.getString(R.string.luteal_movement_athlete_2),
                            context.getString(R.string.luteal_movement_athlete_3),
                            context.getString(R.string.luteal_movement_athlete_4),
                            context.getString(R.string.luteal_movement_athlete_5)
                        )
                    )
                } else {
                    GuideSection(
                        title = context.getString(R.string.luteal_movement_title),
                        items = listOf(
                            context.getString(R.string.luteal_movement_regular_1),
                            context.getString(R.string.luteal_movement_regular_2),
                            context.getString(R.string.luteal_movement_regular_3),
                            context.getString(R.string.luteal_movement_regular_4),
                            context.getString(R.string.luteal_movement_regular_5)
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        context.getString(R.string.luteal_do_1),
                        context.getString(R.string.luteal_do_2),
                        context.getString(R.string.luteal_do_3),
                        context.getString(R.string.luteal_do_4),
                        context.getString(R.string.luteal_do_5)
                    ),
                    donts = listOf(
                        context.getString(R.string.luteal_dont_1),
                        context.getString(R.string.luteal_dont_2),
                        context.getString(R.string.luteal_dont_3),
                        context.getString(R.string.luteal_dont_4),
                        context.getString(R.string.luteal_dont_5)
                    )
                ),
                gentleTips = listOf(
                    context.getString(R.string.luteal_tip_regular_1),
                    context.getString(R.string.luteal_tip_regular_2),
                    context.getString(R.string.luteal_tip_regular_3),
                    context.getString(R.string.luteal_tip_regular_4),
                    context.getString(R.string.luteal_tip_regular_5),
                    context.getString(R.string.luteal_tip_regular_6)
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) context.getString(R.string.luteal_athlete_note) else null
            )
        }
    }
}
