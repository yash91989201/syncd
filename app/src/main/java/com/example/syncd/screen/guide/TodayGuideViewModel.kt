package com.example.syncd.screen.guide

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncd.R
import com.example.syncd.data.repository.UserProfileRepository
import com.example.syncd.screen.home.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GuideSection(
    @StringRes val titleResId: Int,
    val itemResIds: List<Int>,
    @StringRes val footerResId: Int? = null
)

data class DosDonts(
    val dosResIds: List<Int>,
    val dontsResIds: List<Int>
)

data class TodayGuideState(
    @StringRes val phaseNameResId: Int,
    val cycleDay: Int = 23,
    @StringRes val phaseInsightResId: Int = 0,
    val nutritionSection: GuideSection = GuideSection(0, emptyList()),
    val movementSection: GuideSection = GuideSection(0, emptyList()),
    val dosDonts: DosDonts = DosDonts(emptyList(), emptyList()),
    val gentleTipsResIds: List<Int> = emptyList(),
    val isAthlete: Boolean = false,
    @StringRes val athleteNoteResId: Int? = null
)

class TodayGuideViewModel(
    private val homeRepository: HomeRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(createGuideForPhase("luteal", 23, isAthlete = false))
    val state: StateFlow<TodayGuideState> = _state.asStateFlow()

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
                phaseNameResId = R.string.phase_menstrual,
                cycleDay = cycleDay,
                phaseInsightResId = R.string.menstrual_insight,
                nutritionSection = GuideSection(
                    titleResId = R.string.menstrual_nutrition_title,
                    itemResIds = listOf(
                        R.string.menstrual_nutrition_1,
                        R.string.menstrual_nutrition_2,
                        R.string.menstrual_nutrition_3,
                        R.string.menstrual_nutrition_4,
                        R.string.menstrual_nutrition_5,
                        R.string.menstrual_nutrition_6
                    ),
                    footerResId = R.string.menstrual_nutrition_footer
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        titleResId = R.string.menstrual_movement_title,
                        itemResIds = listOf(
                            R.string.menstrual_movement_athlete_1,
                            R.string.menstrual_movement_athlete_2,
                            R.string.menstrual_movement_athlete_3,
                            R.string.menstrual_movement_athlete_4,
                            R.string.menstrual_movement_athlete_5
                        )
                    )
                } else {
                    GuideSection(
                        titleResId = R.string.menstrual_movement_title,
                        itemResIds = listOf(
                            R.string.menstrual_movement_regular_1,
                            R.string.menstrual_movement_regular_2,
                            R.string.menstrual_movement_regular_3,
                            R.string.menstrual_movement_regular_4,
                            R.string.menstrual_movement_regular_5
                        )
                    )
                },
                dosDonts = DosDonts(
                    dosResIds = listOf(
                        R.string.menstrual_do_1,
                        R.string.menstrual_do_2,
                        R.string.menstrual_do_3,
                        R.string.menstrual_do_4,
                        R.string.menstrual_do_5
                    ),
                    dontsResIds = listOf(
                        R.string.menstrual_dont_1,
                        R.string.menstrual_dont_2,
                        R.string.menstrual_dont_3,
                        R.string.menstrual_dont_4,
                        R.string.menstrual_dont_5
                    )
                ),
                gentleTipsResIds = listOf(
                    R.string.menstrual_tip_regular_1,
                    R.string.menstrual_tip_regular_2,
                    R.string.menstrual_tip_regular_3,
                    R.string.menstrual_tip_regular_4,
                    R.string.menstrual_tip_regular_5
                ),
                isAthlete = isAthlete,
                athleteNoteResId = if (isAthlete) R.string.menstrual_athlete_note else null
            )
            
            "follicular" -> TodayGuideState(
                phaseNameResId = R.string.phase_follicular,
                cycleDay = cycleDay,
                phaseInsightResId = R.string.follicular_insight,
                nutritionSection = GuideSection(
                    titleResId = R.string.follicular_nutrition_title,
                    itemResIds = listOf(
                        R.string.follicular_nutrition_1,
                        R.string.follicular_nutrition_2,
                        R.string.follicular_nutrition_3,
                        R.string.follicular_nutrition_4,
                        R.string.follicular_nutrition_5,
                        R.string.follicular_nutrition_6
                    ),
                    footerResId = R.string.follicular_nutrition_footer
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        titleResId = R.string.follicular_movement_title,
                        itemResIds = listOf(
                            R.string.follicular_movement_athlete_1,
                            R.string.follicular_movement_athlete_2,
                            R.string.follicular_movement_athlete_3,
                            R.string.follicular_movement_athlete_4,
                            R.string.follicular_movement_athlete_5
                        )
                    )
                } else {
                    GuideSection(
                        titleResId = R.string.follicular_movement_title,
                        itemResIds = listOf(
                            R.string.follicular_movement_regular_1,
                            R.string.follicular_movement_regular_2,
                            R.string.follicular_movement_regular_3,
                            R.string.follicular_movement_regular_4,
                            R.string.follicular_movement_regular_5
                        )
                    )
                },
                dosDonts = DosDonts(
                    dosResIds = listOf(
                        R.string.follicular_do_1,
                        R.string.follicular_do_2,
                        R.string.follicular_do_3,
                        R.string.follicular_do_4,
                        R.string.follicular_do_5
                    ),
                    dontsResIds = listOf(
                        R.string.follicular_dont_1,
                        R.string.follicular_dont_2,
                        R.string.follicular_dont_3,
                        R.string.follicular_dont_4,
                        R.string.follicular_dont_5
                    )
                ),
                gentleTipsResIds = listOf(
                    R.string.follicular_tip_regular_1,
                    R.string.follicular_tip_regular_2,
                    R.string.follicular_tip_regular_3,
                    R.string.follicular_tip_regular_4,
                    R.string.follicular_tip_regular_5
                ),
                isAthlete = isAthlete,
                athleteNoteResId = if (isAthlete) R.string.follicular_athlete_note else null
            )
            
            "ovulation" -> TodayGuideState(
                phaseNameResId = R.string.phase_ovulation,
                cycleDay = cycleDay,
                phaseInsightResId = R.string.ovulation_insight,
                nutritionSection = GuideSection(
                    titleResId = R.string.ovulation_nutrition_title,
                    itemResIds = listOf(
                        R.string.ovulation_nutrition_1,
                        R.string.ovulation_nutrition_2,
                        R.string.ovulation_nutrition_3,
                        R.string.ovulation_nutrition_4,
                        R.string.ovulation_nutrition_5,
                        R.string.ovulation_nutrition_6
                    ),
                    footerResId = R.string.ovulation_nutrition_footer
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        titleResId = R.string.ovulation_movement_title,
                        itemResIds = listOf(
                            R.string.ovulation_movement_athlete_1,
                            R.string.ovulation_movement_athlete_2,
                            R.string.ovulation_movement_athlete_3,
                            R.string.ovulation_movement_athlete_4,
                            R.string.ovulation_movement_athlete_5
                        )
                    )
                } else {
                    GuideSection(
                        titleResId = R.string.ovulation_movement_title,
                        itemResIds = listOf(
                            R.string.ovulation_movement_regular_1,
                            R.string.ovulation_movement_regular_2,
                            R.string.ovulation_movement_regular_3,
                            R.string.ovulation_movement_regular_4,
                            R.string.ovulation_movement_regular_5
                        )
                    )
                },
                dosDonts = DosDonts(
                    dosResIds = listOf(
                        R.string.ovulation_do_1,
                        R.string.ovulation_do_2,
                        R.string.ovulation_do_3,
                        R.string.ovulation_do_4,
                        R.string.ovulation_do_5
                    ),
                    dontsResIds = listOf(
                        R.string.ovulation_dont_1,
                        R.string.ovulation_dont_2,
                        R.string.ovulation_dont_3,
                        R.string.ovulation_dont_4,
                        R.string.ovulation_dont_5
                    )
                ),
                gentleTipsResIds = listOf(
                    R.string.ovulation_tip_regular_1,
                    R.string.ovulation_tip_regular_2,
                    R.string.ovulation_tip_regular_3,
                    R.string.ovulation_tip_regular_4,
                    R.string.ovulation_tip_regular_5,
                    R.string.ovulation_tip_regular_6
                ),
                isAthlete = isAthlete,
                athleteNoteResId = if (isAthlete) R.string.ovulation_athlete_note else null
            )
            
            else -> TodayGuideState(
                phaseNameResId = R.string.phase_luteal,
                cycleDay = cycleDay,
                phaseInsightResId = R.string.luteal_insight,
                nutritionSection = GuideSection(
                    titleResId = R.string.luteal_nutrition_title,
                    itemResIds = listOf(
                        R.string.luteal_nutrition_1,
                        R.string.luteal_nutrition_2,
                        R.string.luteal_nutrition_3,
                        R.string.luteal_nutrition_4,
                        R.string.luteal_nutrition_5,
                        R.string.luteal_nutrition_6
                    ),
                    footerResId = R.string.luteal_nutrition_footer
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        titleResId = R.string.luteal_movement_title,
                        itemResIds = listOf(
                            R.string.luteal_movement_athlete_1,
                            R.string.luteal_movement_athlete_2,
                            R.string.luteal_movement_athlete_3,
                            R.string.luteal_movement_athlete_4,
                            R.string.luteal_movement_athlete_5
                        )
                    )
                } else {
                    GuideSection(
                        titleResId = R.string.luteal_movement_title,
                        itemResIds = listOf(
                            R.string.luteal_movement_regular_1,
                            R.string.luteal_movement_regular_2,
                            R.string.luteal_movement_regular_3,
                            R.string.luteal_movement_regular_4,
                            R.string.luteal_movement_regular_5
                        )
                    )
                },
                dosDonts = DosDonts(
                    dosResIds = listOf(
                        R.string.luteal_do_1,
                        R.string.luteal_do_2,
                        R.string.luteal_do_3,
                        R.string.luteal_do_4,
                        R.string.luteal_do_5
                    ),
                    dontsResIds = listOf(
                        R.string.luteal_dont_1,
                        R.string.luteal_dont_2,
                        R.string.luteal_dont_3,
                        R.string.luteal_dont_4,
                        R.string.luteal_dont_5
                    )
                ),
                gentleTipsResIds = listOf(
                    R.string.luteal_tip_regular_1,
                    R.string.luteal_tip_regular_2,
                    R.string.luteal_tip_regular_3,
                    R.string.luteal_tip_regular_4,
                    R.string.luteal_tip_regular_5,
                    R.string.luteal_tip_regular_6
                ),
                isAthlete = isAthlete,
                athleteNoteResId = if (isAthlete) R.string.luteal_athlete_note else null
            )
        }
    }
}
