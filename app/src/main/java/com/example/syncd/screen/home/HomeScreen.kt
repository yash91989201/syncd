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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
        baseColor.copy(alpha = 0.3f),
        baseColor.copy(alpha = 0.1f),
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
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "TODAY'S INSIGHTS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        CycleProgressVisual(
            dayOfCycle = phaseInfo.dayOfCycle,
            cycleLength = phaseInfo.cycleLength,
            phaseColor = phaseColor
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            color = phaseColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = phaseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TodayGuideSection(
            phaseName = phaseName,
            phaseColor = phaseColor,
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
                color = Color.White.copy(alpha = 0.5f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 25.dp.toPx(), cap = StrokeCap.Round)
            )
            
            drawArc(
                color = phaseColor,
                startAngle = -90f,
                sweepAngle = 360f * currentPercent.value,
                useCenter = false,
                style = Stroke(width = 25.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAY",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Text(
                text = "$dayOfCycle",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 80.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 80.sp
            )
            Text(
                text = "OF $cycleLength",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.7f)
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(iconColor)
            )
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
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
    onViewFullGuide: () -> Unit
) {
    val guideContent = getGuideContentForPhase(phaseName)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Guide",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Button(
                onClick = onViewFullGuide,
                colors = ButtonDefaults.buttonColors(
                    containerColor = phaseColor.copy(alpha = 0.25f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Full Guide",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = phaseColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = guideContent.phaseInsight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp
            )
        }
        
        GuidePreviewCard(
            emoji = "🍲",
            title = "Nutrition",
            items = guideContent.nutritionTips,
            phaseColor = phaseColor
        )
        
        GuidePreviewCard(
            emoji = "🏃",
            title = "Movement",
            items = guideContent.movementTips,
            phaseColor = phaseColor
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚖️", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "Do's & Don'ts",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        guideContent.dos.forEach { item ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "✓",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = phaseColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        guideContent.donts.forEach { item ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "✗",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        GuidePreviewCard(
            emoji = "💡",
            title = "Gentle Tips",
            items = guideContent.tips,
            phaseColor = phaseColor
        )
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(phaseColor)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

private fun getGuideContentForPhase(phaseName: String): GuidePreviewContent {
    return when {
        phaseName.contains("Menstrual", ignoreCase = true) -> GuidePreviewContent(
            phaseInsight = "Your body is renewing itself. Gentle care and warm, nourishing foods can help you feel more comfortable today.",
            nutritionTips = listOf(
                "Warm meals like dal, khichdi, or soup",
                "Iron-rich foods like spinach, dates"
            ),
            movementTips = listOf(
                "Gentle walking or light stretching",
                "Rest is perfectly okay today"
            ),
            dos = listOf("Prioritize rest", "Stay hydrated"),
            donts = listOf("Push too hard", "Skip meals"),
            tips = listOf(
                "A warm shower may ease tension",
                "Slowing down is part of your cycle"
            )
        )
        phaseName.contains("Follicular", ignoreCase = true) -> GuidePreviewContent(
            phaseInsight = "Energy is building up. This is a great time to try new things and enjoy activities that feel fun.",
            nutritionTips = listOf(
                "Fresh, light meals with vegetables",
                "Protein-rich foods like eggs, paneer"
            ),
            movementTips = listOf(
                "Try a new workout or activity",
                "Strength exercises can feel good"
            ),
            dos = listOf("Start new projects", "Try new activities"),
            donts = listOf("Overcommit", "Neglect sleep"),
            tips = listOf(
                "Good time for planning ahead",
                "Your focus may be sharper"
            )
        )
        phaseName.contains("Ovulation", ignoreCase = true) -> GuidePreviewContent(
            phaseInsight = "Energy is at its peak. You may feel more social and confident. Enjoy this vibrant phase.",
            nutritionTips = listOf(
                "Light, fresh meals with raw veggies",
                "Antioxidant-rich foods like berries"
            ),
            movementTips = listOf(
                "Higher intensity workouts feel great",
                "Group fitness or team sports"
            ),
            dos = listOf("Take on challenges", "Socialize"),
            donts = listOf("Skip warm-ups", "Overextend"),
            tips = listOf(
                "Communication comes easier now",
                "Confidence naturally rises"
            )
        )
        else -> GuidePreviewContent(
            phaseInsight = "This phase often comes with lower energy. Gentle routines and nourishing food can help you feel steadier today.",
            nutritionTips = listOf(
                "Warm meals like dal, khichdi, curry",
                "Complex carbs like roti, rice"
            ),
            movementTips = listOf(
                "Gentle walking or stretching",
                "Light yoga or mobility exercises"
            ),
            dos = listOf("Take breaks", "Prioritize sleep"),
            donts = listOf("Push too hard", "Overload schedule"),
            tips = listOf(
                "A warm shower may ease tension",
                "Cravings are normal - honor them"
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
            modifier = Modifier.size(48.dp)
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
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
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
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Try Again")
        }
    }
}
