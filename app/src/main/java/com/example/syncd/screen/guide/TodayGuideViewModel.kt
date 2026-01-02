package com.example.syncd.screen.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                phaseName = "Menstrual Phase",
                cycleDay = cycleDay,
                phaseInsight = "Your body is shedding and renewing. This phase asks for rest and gentle nourishment. Listen to what feels right for you today.",
                nutritionSection = GuideSection(
                    title = "Nourishment for Today",
                    items = listOf(
                        "Warm dal-chawal, khichdi or moong dal soup with ghee",
                        "Iron-rich foods: spinach palak paneer, beetroot subzi, dates with warm milk",
                        "Ginger-jaggery chai or jeera water for warmth",
                        "Curd rice or light idli-sambar if you prefer simple meals",
                        "Sesame seeds (til) ladoo or chikki for iron and warmth",
                        "Avoid heavy, oily or very spicy food if it causes discomfort"
                    ),
                    footer = "Warm, simple meals digest easier and provide comfort during this phase."
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Light stretching, walking or mobility drills only",
                            "This is your recovery phase - muscles repair now",
                            "Gentle yoga or pranayama can help with cramps",
                            "Skip high-intensity training; return gradually in 2-3 days",
                            "Recovery today = stronger performance in follicular phase"
                        )
                    )
                } else {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Gentle 10-15 minute walks if energy permits",
                            "Slow stretching or restorative yoga poses",
                            "Supta Baddha Konasana (lying butterfly pose) eases cramps",
                            "Complete rest is perfectly fine and needed",
                            "Move only if your body says yes, not because you should"
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        "Keep a hot water bottle or heating pad handy",
                        "Rest when tired - naps are helpful",
                        "Wear loose, comfortable cotton clothes",
                        "Drink warm fluids through the day",
                        "Light self-massage with warm oil on lower belly and back"
                    ),
                    donts = listOf(
                        "Force yourself to push through severe pain",
                        "Skip meals thinking you'll feel better - blood sugar drops worsen mood",
                        "Overload your calendar with back-to-back commitments",
                        "Compare your flow or pain to others - every body is different",
                        "Feel guilty for needing rest or slowing down"
                    )
                ),
                gentleTips = listOf(
                    "A warm shower or bath with a few drops of lavender oil soothes muscles",
                    "This phase lasts 3-5 days typically - pace yourself accordingly",
                    "Low energy during menstruation is biological, not laziness",
                    "If pain is very severe or unusual, consult a gynecologist",
                    "Keep sanitary products, pain relief and snacks accessible at work/home"
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) "This is your active rest phase. Prioritize sleep, nutrition and light movement. Overtraining now increases injury risk and delays recovery. You'll feel strength return in the follicular phase." else null
            )
            
            "follicular" -> TodayGuideState(
                phaseName = "Follicular Phase",
                cycleDay = cycleDay,
                phaseInsight = "Energy and motivation are rising naturally. Your body is building up strength. Great time for starting new habits or projects.",
                nutritionSection = GuideSection(
                    title = "Nourishment for Today",
                    items = listOf(
                        "Protein-rich: moong dal chilla, paneer bhurji, egg bhurji with roti",
                        "Fresh vegetables: palak, methi, bhindi, or mixed sabzi",
                        "Probiotic foods: curd, buttermilk, fermented dosa/idli batter",
                        "Sprouts salad with lemon and chat masala",
                        "Light grains: ragi, jowar roti, brown rice upma",
                        "Fresh seasonal fruits: papaya, guava, oranges, pomegranate"
                    ),
                    footer = "Your metabolism is active now - nutrient-dense, lighter meals work best."
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Great time for skill-building and technique work",
                            "Strength training feels easier - progressive overload works well",
                            "High energy for drills, speed work or learning new movements",
                            "Recovery is faster in this phase - make the most of it",
                            "Balance intensity with rest days to avoid overtraining"
                        )
                    )
                } else {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Try that new yoga class or dance workout you've been curious about",
                            "Brisk 30-40 minute walks, cycling or swimming",
                            "Strength training or bodyweight exercises feel manageable",
                            "Group fitness or playing a sport with friends",
                            "Energy is high - enjoy movement that feels fun, not forced"
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        "Start that project or habit you've been postponing",
                        "Schedule important meetings or tasks in this window",
                        "Plan your week ahead while mental clarity is high",
                        "Socialize, network or reconnect with friends",
                        "Take advantage of higher focus for learning or work"
                    ),
                    donts = listOf(
                        "Overcommit just because you feel capable right now",
                        "Sacrifice sleep to pack in extra activities",
                        "Ignore hunger signals - fuel your active body properly",
                        "Forget that this high-energy phase won't last all month",
                        "Compare yourself to others who seem less energetic"
                    )
                ),
                gentleTips = listOf(
                    "This is prime time for tackling challenging tasks at work or studies",
                    "Creativity and problem-solving often peak during follicular phase",
                    "Good phase to set goals for the month ahead",
                    "Skin may look clearer and brighter naturally",
                    "If planning social events, this is your ideal window"
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) "Your body responds exceptionally well to training stimulus now. Strength gains, endurance building and skill acquisition work best in this phase. Use it strategically for your hardest training blocks." else null
            )
            
            "ovulation" -> TodayGuideState(
                phaseName = "Ovulation Phase",
                cycleDay = cycleDay,
                phaseInsight = "You're at your peak. Energy, confidence and physical strength are highest. Social connections feel easier. Make the most of this vibrant window.",
                nutritionSection = GuideSection(
                    title = "Nourishment for Today",
                    items = listOf(
                        "Light, fresh meals: salads with sprouted moong, chickpeas, paneer",
                        "Antioxidant-rich: berries, pomegranate, green leafy vegetables",
                        "Cooling foods: cucumber raita, coconut water, fresh fruit juice",
                        "Whole grains: quinoa pulao, brown rice, multi-grain roti",
                        "Lean proteins: grilled paneer, dal, egg whites, fish (if non-veg)",
                        "Avoid very heavy or deep-fried foods - keep it light and fresh"
                    ),
                    footer = "Metabolism is at its most efficient - lighter, nutrient-dense meals work best."
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Peak performance window - great for competitions or testing PRs",
                            "High-intensity training, plyometrics or power work feel achievable",
                            "Coordination and reaction time are optimal",
                            "Important: Ligaments are more lax - warm up thoroughly to prevent injuries",
                            "If competing, time events in this phase strategically"
                        )
                    )
                } else {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "High-intensity workouts, HIIT or challenging sessions feel great",
                            "Group classes: Zumba, aerobics, kickboxing or team sports",
                            "Running, cycling or swimming at higher intensity",
                            "Try that advanced yoga class or challenging hike",
                            "Warm up properly - flexibility is high but joint stability needs care"
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        "Have important conversations or difficult discussions",
                        "Attend social events, network or present ideas",
                        "Take on challenging projects or deadlines",
                        "Make decisions requiring confidence and clarity",
                        "Schedule job interviews, presentations or public speaking"
                    ),
                    donts = listOf(
                        "Skip warm-ups before intense exercise - injury risk is higher",
                        "Overextend socially or professionally - burnout comes later",
                        "Ignore hydration and electrolyte needs during activity",
                        "Assume this peak will last all month - plan for lower energy ahead",
                        "Push through joint pain - ligament laxity makes injuries easier"
                    )
                ),
                gentleTips = listOf(
                    "Communication skills, charm and persuasion peak during ovulation",
                    "Your voice may sound more confident and attractive naturally",
                    "Skin often glows and looks its best",
                    "This phase lasts only 2-3 days - plan your month around it",
                    "If trying to conceive, fertility is highest in this window",
                    "Enjoy the natural confidence boost but stay grounded"
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) "Peak strength, power and coordination. Best phase for competitions, PRs or high-stakes performance. BUT: Ligaments are more relaxed due to estrogen - warm up meticulously and avoid jerky movements to prevent ACL or joint injuries. Many female athletes report injuries during ovulation due to this." else null
            )
            
            else -> TodayGuideState(
                phaseName = "Luteal Phase",
                cycleDay = cycleDay,
                phaseInsight = "Energy may feel lower than before. Your body is preparing for either menstruation or pregnancy. Gentle routines, comforting food and self-compassion help you navigate this phase.",
                nutritionSection = GuideSection(
                    title = "Nourishment for Today",
                    items = listOf(
                        "Warm, grounding meals: dal-roti, khichdi with ghee, rajma-chawal",
                        "Complex carbs for serotonin: sweet potato, oats, whole wheat",
                        "Magnesium-rich: almonds, cashews, pumpkin seeds, dark chocolate (70%+)",
                        "Reduce bloating: jeera water, ajwain, fennel tea, ginger chai",
                        "Healthy fats: ghee, coconut chutney, til (sesame), avocado",
                        "Go easy on salt, caffeine and refined sugar if PMS symptoms are strong"
                    ),
                    footer = "Cravings for sweets or carbs are normal - choose wholesome versions like dates, jaggery or whole grains."
                ),
                movementSection = if (isAthlete) {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Shift focus to moderate intensity, longer duration work",
                            "Endurance training, steady-state cardio, technique drills",
                            "Avoid pushing for PRs - injury risk increases with fatigue",
                            "Listen to your body: low energy days need adjusted volume",
                            "Recovery sessions: yoga, stretching, swimming, light cycling"
                        )
                    )
                } else {
                    GuideSection(
                        title = "How to Move Today",
                        items = listOf(
                            "Gentle 20-30 minute walks, preferably in nature or parks",
                            "Slow, flowing yoga or pilates",
                            "Light cycling, swimming or dancing at your own pace",
                            "Avoid high-intensity or competitive workouts",
                            "Rest completely if energy is very low - that's completely okay"
                        )
                    )
                },
                dosDonts = DosDonts(
                    dos = listOf(
                        "Schedule lighter days and avoid back-to-back commitments",
                        "Prioritize 7-8 hours of sleep - it helps mood and energy",
                        "Keep comfort items ready: heating pad, loose clothes, snacks",
                        "Practice self-compassion if mood dips or irritability rises",
                        "Plan for upcoming period: stock supplies, prep easy meals"
                    ),
                    donts = listOf(
                        "Force yourself to maintain follicular-phase productivity",
                        "Skip meals hoping to reduce bloating - it makes symptoms worse",
                        "Over-schedule or commit to intense social obligations",
                        "Blame yourself for lower energy, mood swings or cravings",
                        "Ignore severe PMS symptoms - talk to a doctor if it's debilitating"
                    )
                ),
                gentleTips = listOf(
                    "Warm baths with Epsom salt ease muscle tension and bloating",
                    "Journaling or light creative work can help process emotions",
                    "Lower energy and inward focus are biological - not personal flaws",
                    "This phase typically lasts 10-14 days before menstruation",
                    "Cravings peak 3-5 days before period - prepare nourishing snacks in advance",
                    "If PMS is severe, consider tracking symptoms to discuss with your doctor"
                ),
                isAthlete = isAthlete,
                athleteNote = if (isAthlete) "Shift training focus to endurance and technique rather than strength or power. Progesterone dominance increases body temperature and fatigue. Recovery takes longer - reduce volume or intensity by 20-30% in late luteal phase. This isn't weakness; it's smart periodization for long-term performance." else null
            )
        }
    }
}
