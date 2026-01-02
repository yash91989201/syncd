package com.example.syncd.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syncd.navigation.Navigator
import com.example.syncd.navigation.Screen
import com.example.syncd.screen.home.data.model.PhaseInfo
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navigator: Navigator = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val baseColor = uiState.phaseColor
    val gradientColors = listOf(
        baseColor.copy(alpha = 0.25f),
        baseColor.copy(alpha = 0.08f),
        MaterialTheme.colorScheme.surface
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        when {
            uiState.isLoading -> {
                LoadingState(baseColor)
            }

            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: "Something went wrong",
                    onRetry = { viewModel.loadPhaseInfo() }
                )
            }

            uiState.phaseInfo != null -> {
                SuccessContent(
                    phaseInfo = uiState.phaseInfo!!,
                    phaseName = uiState.phaseName,
                    phaseColor = uiState.phaseColor,
                    isAthlete = uiState.isAthlete,
                    onViewFullGuide = { navigator.navigateTo(Screen.TodayGuide) }
                )
            }

            else -> {
                ErrorState(
                    message = "No cycle data available",
                    onRetry = { viewModel.loadPhaseInfo() }
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    phaseInfo: PhaseInfo,
    phaseName: String,
    phaseColor: Color,
    isAthlete: Boolean,
    onViewFullGuide: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
        ) {
            Text(
                text = "TODAY'S INSIGHTS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        CycleProgressVisual(
            dayOfCycle = phaseInfo.dayOfCycle,
            cycleLength = phaseInfo.cycleLength,
            phaseColor = phaseColor
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            color = phaseColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = phaseColor.copy(alpha = 0.3f),
                    spotColor = phaseColor.copy(alpha = 0.3f)
                )
        ) {
            Text(
                text = phaseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "Cycle Length",
                value = "${phaseInfo.cycleLength}",
                unit = "DAYS",
                iconColor = phaseColor
            )
            
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "Next Period",
                value = "${phaseInfo.daysUntilNextPeriod}",
                unit = "DAYS LEFT",
                iconColor = phaseColor
            )
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        TodayGuideSection(
            phaseName = phaseName,
            phaseColor = phaseColor,
            isAthlete = isAthlete,
            onViewFullGuide = onViewFullGuide
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CycleProgressVisual(
    dayOfCycle: Int,
    cycleLength: Int,
    phaseColor: Color
) {
    val progress = dayOfCycle.toFloat() / cycleLength.toFloat()
    
    var animationPlayed by remember { mutableStateOf(false) }
    val currentPercent = animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 200),
        label = "progress"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            drawArc(
                color = Color.White.copy(alpha = 0.6f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Round)
            )
            
            drawArc(
                color = phaseColor,
                startAngle = -90f,
                sweepAngle = 360f * currentPercent.value,
                useCenter = false,
                style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAY",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$dayOfCycle",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 76.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 76.sp
            )
            Text(
                text = "OF $cycleLength",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    iconColor: Color
) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(iconColor)
            )
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodayGuideSection(
    phaseName: String,
    phaseColor: Color,
    isAthlete: Boolean,
    onViewFullGuide: () -> Unit
) {
    val guideContent = getGuideContentForPhase(phaseName, isAthlete)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today's Guide",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Personalized for your phase",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            FilledTonalButton(
                onClick = onViewFullGuide,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = phaseColor.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = phaseColor.copy(alpha = 0.3f),
                    ) {}
                    Text(
                        text = "Phase Insight",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = guideContent.phaseInsight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactGuideCard(
                modifier = Modifier.weight(1f),
                emoji = "🍲",
                title = "Nutrition",
                items = guideContent.nutritionTips.take(2),
                accentColor = phaseColor
            )
            
            CompactGuideCard(
                modifier = Modifier.weight(1f),
                emoji = "🏃",
                title = "Movement",
                items = guideContent.movementTips.take(2),
                accentColor = phaseColor
            )
        }
        
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        phaseColor.copy(alpha = 0.3f),
                        phaseColor.copy(alpha = 0.1f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "⚖️",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Column {
                        Text(
                            text = "Quick Do's & Don'ts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Essential reminders for today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        guideContent.dos.take(2).forEach { item ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = phaseColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "✓",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = phaseColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .height(60.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        guideContent.donts.take(2).forEach { item ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "✗",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = phaseColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "💡",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Text(
                        text = "Gentle Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                guideContent.tips.take(2).forEach { tip ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(phaseColor)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactGuideCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    items: List<String>,
    accentColor: Color
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidePreviewCard(
    emoji: String,
    title: String,
    items: List<String>,
    phaseColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(phaseColor)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

private data class GuidePreviewContent(
    val phaseInsight: String,
    val nutritionTips: List<String>,
    val movementTips: List<String>,
    val dos: List<String>,
    val donts: List<String>,
    val tips: List<String>
)

private fun getGuideContentForPhase(phaseName: String, isAthlete: Boolean = false): GuidePreviewContent {
    return when {
        phaseName.contains("Menstrual", ignoreCase = true) -> GuidePreviewContent(
            phaseInsight = "Your body is shedding and renewing. This phase asks for rest and gentle nourishment. Listen to what feels right for you today.",
            nutritionTips = listOf(
                "Warm dal-chawal, khichdi with ghee",
                "Iron-rich: palak paneer, beetroot, dates",
                "Ginger-jaggery chai or jeera water",
                "Sesame (til) ladoo for warmth"
            ),
            movementTips = if (isAthlete) listOf(
                "Light stretching or mobility drills only",
                "This is your recovery phase",
                "Muscles repair best when you rest",
                "Return gradually in 2-3 days"
            ) else listOf(
                "Gentle 10-15 minute walks only",
                "Supta Baddha Konasana eases cramps",
                "Complete rest is perfectly fine",
                "Move only if body says yes"
            ),
            dos = listOf(
                "Keep heating pad handy",
                "Rest when tired"
            ),
            donts = listOf(
                "Force through pain",
                "Skip meals"
            ),
            tips = if (isAthlete) listOf(
                "Recovery today = stronger performance ahead",
                "Overtraining now increases injury risk",
                "Low energy is biological, not laziness"
            ) else listOf(
                "Warm oil massage on lower belly helps",
                "Low energy is biological, not laziness",
                "This phase lasts 3-5 days typically"
            )
        )
        phaseName.contains("Follicular", ignoreCase = true) -> GuidePreviewContent(
            phaseInsight = "Energy and motivation are rising naturally. Your body is building up strength. Great time for starting new habits or projects.",
            nutritionTips = listOf(
                "Moong dal chilla, paneer bhurji",
                "Fresh methi, palak, bhindi sabzi",
                "Probiotic: curd, buttermilk, dosa",
                "Sprouts salad with chat masala"
            ),
            movementTips = if (isAthlete) listOf(
                "Great for skill-building & technique",
                "Strength training feels easier",
                "High energy for speed work, drills",
                "Recovery is faster - make the most of it"
            ) else listOf(
                "Try new yoga or dance class",
                "Brisk 30-40 minute walks, cycling",
                "Strength training feels manageable",
                "Energy is high - enjoy movement"
            ),
            dos = listOf(
                "Start postponed projects",
                "Schedule important tasks"
            ),
            donts = listOf(
                "Overcommit capabilities",
                "Sacrifice sleep"
            ),
            tips = if (isAthlete) listOf(
                "Best phase for strength gains",
                "Body adapts well to training stimulus",
                "Use strategically for hard blocks"
            ) else listOf(
                "Prime time for tackling challenges",
                "Creativity peaks during this phase",
                "Skin may look clearer naturally"
            )
        )
        phaseName.contains("Ovulation", ignoreCase = true) -> GuidePreviewContent(
            phaseInsight = "You're at your peak. Energy, confidence and physical strength are highest. Social connections feel easier. Make the most of this vibrant window.",
            nutritionTips = listOf(
                "Light salads with sprouted moong",
                "Pomegranate, berries, green leafy",
                "Coconut water, cucumber raita",
                "Quinoa pulao, multi-grain roti"
            ),
            movementTips = if (isAthlete) listOf(
                "Peak performance window for PRs",
                "High-intensity, power work feel great",
                "Coordination is optimal",
                "⚠️ Warm up thoroughly - injury risk up"
            ) else listOf(
                "HIIT, challenging sessions feel great",
                "Group: Zumba, aerobics, team sports",
                "Advanced yoga, challenging hikes",
                "Warm up properly - prevent injuries"
            ),
            dos = listOf(
                "Have important conversations",
                "Take on challenges"
            ),
            donts = listOf(
                "Skip warm-ups (injury risk)",
                "Overextend socially"
            ),
            tips = if (isAthlete) listOf(
                "Best for competitions/testing PRs",
                "Ligaments more lax - warm up well",
                "Many injuries happen now"
            ) else listOf(
                "Communication skills peak now",
                "Skin often glows naturally",
                "This phase lasts only 2-3 days"
            )
        )
        else -> GuidePreviewContent(
            phaseInsight = "Energy may feel lower than before. Your body is preparing for menstruation. Gentle routines, comforting food and self-compassion help you navigate this phase.",
            nutritionTips = listOf(
                "Dal-roti, rajma-chawal, khichdi",
                "Sweet potato, oats for serotonin",
                "Almonds, dark chocolate (70%+)",
                "Jeera water, fennel tea for bloating"
            ),
            movementTips = if (isAthlete) listOf(
                "Shift to moderate intensity work",
                "Endurance training, steady cardio",
                "Avoid pushing for PRs",
                "Recovery takes longer - reduce 20-30%"
            ) else listOf(
                "Gentle 20-30 min walks in nature",
                "Slow flowing yoga or pilates",
                "Light cycling, swimming at own pace",
                "Rest if energy is very low"
            ),
            dos = listOf(
                "Schedule lighter days",
                "Prioritize 7-8 hours sleep"
            ),
            donts = listOf(
                "Force high productivity",
                "Blame yourself for PMS"
            ),
            tips = if (isAthlete) listOf(
                "This isn't weakness - it's smart periodization",
                "Progesterone increases body temp & fatigue",
                "Recovery longer - plan accordingly"
            ) else listOf(
                "Warm Epsom salt baths ease tension",
                "Lower energy is hormonal, not a flaw",
                "Cravings peak 3-5 days before period"
            )
        )
    }
}

@Composable
private fun LoadingState(color: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp),
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "oops",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp
            )
        ) {
            Text(
                "Try Again",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
