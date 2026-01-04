package com.example.syncd.screen.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.syncd.R
import com.example.syncd.navigation.Navigator
import com.example.syncd.navigation.Screen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen() {
    val navigator = koinInject<Navigator>()
    val viewModel = koinViewModel<OnboardingViewModel>()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            navigator.setRoot(Screen.Home)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(
                                R.string.onboarding_step_indicator,
                                state.currentStepIndex + 1,
                                state.totalSteps
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.onboarding_progress_percent,
                                    ((state.currentStepIndex + 1) / state.totalSteps.toFloat() * 100).toInt()
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = (state.currentStepIndex + 1) / state.totalSteps.toFloat(),
                        animationSpec = tween(durationMillis = 400),
                        label = "progress"
                    )

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val currentStep = state.currentStep

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState.id > initialState.id) {
                                (slideInHorizontally { width -> width } + fadeIn(
                                    animationSpec = tween(300)
                                )) togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut(
                                    animationSpec = tween(300)
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn(
                                    animationSpec = tween(300)
                                )) togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut(
                                    animationSpec = tween(300)
                                )
                            }.using(SizeTransform(clip = false))
                        },
                        label = "step_transition"
                    ) { step ->
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = step.question,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight
                            )

                            if (step.helperText != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    onClick = { },
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = stringResource(R.string.onboarding_info),
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.onboarding_why_ask),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            when (step.stepType) {
                                StepType.DATE_PICKER -> {
                                    LastPeriodDatePicker(
                                        selectedDateMillis = state.lastPeriodDate,
                                        onDateSelected = { viewModel.onLastPeriodDateSelected(it) }
                                    )
                                }

                                StepType.OPTIONS -> {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(step.options, key = { it.id }) { option ->
                                            val isSelected = state.selectedOptionId == option.id
                                            OptionCard(
                                                text = option.text,
                                                isSelected = isSelected,
                                                onClick = { viewModel.onOptionSelected(option.id) }
                                            )
                                        }

                                        item {
                                            AnimatedVisibility(
                                                visible = state.showCustomSportInput,
                                                enter = expandVertically() + fadeIn(),
                                                exit = shrinkVertically() + fadeOut()
                                            ) {
                                                Column {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    OutlinedTextField(
                                                        value = state.customSport,
                                                        onValueChange = {
                                                            viewModel.onCustomSportChanged(
                                                                it
                                                            )
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        placeholder = {
                                                            Text(stringResource(R.string.onboarding_custom_sport_placeholder))
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(
                                                            capitalization = KeyboardCapitalization.Sentences,
                                                            imeAction = ImeAction.Done
                                                        ),
                                                        keyboardActions = KeyboardActions(
                                                            onDone = { viewModel.onNext() }
                                                        ),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = state.currentStepIndex > 0,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                onClick = { viewModel.onBack() },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.onboarding_back),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (state.currentStepIndex == 0) {
                            Spacer(modifier = Modifier.width(44.dp))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = viewModel::onNext,
                            modifier = Modifier
                                .height(40.dp)
                                .width(120.dp),
                            shape = RoundedCornerShape(24.dp),
                            enabled = state.canProceed && !state.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.4f
                                ),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(
                                    alpha = 0.6f
                                )
                            ),
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (state.currentStepIndex == state.totalSteps - 1)
                                        stringResource(R.string.onboarding_finish)
                                    else
                                        stringResource(R.string.onboarding_next),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = if (isSelected)
            BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        else
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + androidx.compose.animation.scaleIn(),
                exit = fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.onboarding_selected),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LastPeriodDatePicker(
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit
) {
    val currentDate = remember { java.time.LocalDate.now() }
    val selectedDate = selectedDateMillis?.let {
        java.time.Instant.ofEpochMilli(it)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    } ?: currentDate

    var isDialogOpen by remember { mutableStateOf(false) }
    var tempYear by remember { mutableIntStateOf(selectedDate.year) }
    var tempMonth by remember { mutableIntStateOf(selectedDate.monthValue) }
    var tempDay by remember { mutableIntStateOf(selectedDate.dayOfMonth) }

    val years = remember { listOf(currentDate.year - 1, currentDate.year, currentDate.year + 1) }
    val months = remember { (1..12).toList() }
    val daysInMonth = java.time.YearMonth.of(tempYear, tempMonth).lengthOfMonth()
    val days = (1..daysInMonth).toList()

    fun formatSelectedDate(): String {
        val monthName = java.time.Month.of(selectedDate.monthValue).getDisplayName(
            java.time.format.TextStyle.SHORT,
            java.util.Locale.getDefault()
        )
        return "$monthName ${selectedDate.dayOfMonth}, ${selectedDate.year}"
    }

    fun confirmSelection() {
        val adjustedDay =
            tempDay.coerceAtMost(java.time.YearMonth.of(tempYear, tempMonth).lengthOfMonth())
        val date = java.time.LocalDate.of(tempYear, tempMonth, adjustedDay)
        val millis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        onDateSelected(millis)
        isDialogOpen = false
    }

    Surface(
        onClick = {
            tempYear = selectedDate.year
            tempMonth = selectedDate.monthValue
            tempDay = selectedDate.dayOfMonth
            isDialogOpen = true
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_last_period),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatSelectedDate(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (isDialogOpen) {
        Dialog(onDismissRequest = { isDialogOpen = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_select_last_period),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_day),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            WheelPicker(
                                items = days.map { it.toString().padStart(2, '0') },
                                selectedIndex = days.indexOf(tempDay).coerceAtLeast(0),
                                onItemSelected = { tempDay = days.getOrNull(it) ?: tempDay },
                                infiniteScroll = true
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_month),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            WheelPicker(
                                items = months.map {
                                    java.time.Month.of(it).getDisplayName(
                                        java.time.format.TextStyle.SHORT,
                                        java.util.Locale.getDefault()
                                    )
                                },
                                selectedIndex = months.indexOf(tempMonth).coerceAtLeast(0),
                                onItemSelected = { index ->
                                    tempMonth = months.getOrNull(index) ?: tempMonth
                                    val maxDay =
                                        java.time.YearMonth.of(tempYear, tempMonth).lengthOfMonth()
                                    if (tempDay > maxDay) tempDay = maxDay
                                },
                                infiniteScroll = true
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_year),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            WheelPicker(
                                items = years.map { it.toString() },
                                selectedIndex = years.indexOf(tempYear).coerceAtLeast(0),
                                onItemSelected = { index ->
                                    tempYear = years.getOrNull(index) ?: tempYear
                                    val maxDay =
                                        java.time.YearMonth.of(tempYear, tempMonth).lengthOfMonth()
                                    if (tempDay > maxDay) tempDay = maxDay
                                },
                                infiniteScroll = false
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isDialogOpen = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.action_cancel))
                        }
                        Button(
                            onClick = { confirmSelection() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.action_confirm))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 5,
    infiniteScroll: Boolean = true
) {
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val totalHeight = itemHeight * visibleItemsCount
    val listState = rememberLazyListState()
    val paddingItemCount = visibleItemsCount / 2

    val virtualListSize = if (infiniteScroll) Int.MAX_VALUE else items.size
    val virtualMiddle = if (infiniteScroll) Int.MAX_VALUE / 2 else 0

    val initialIndex = if (infiniteScroll) {
        virtualMiddle - (virtualMiddle % items.size) + selectedIndex
    } else {
        selectedIndex
    }

    LaunchedEffect(items.size, selectedIndex) {
        if (items.isNotEmpty()) {
            val scrollIndex = if (infiniteScroll) {
                initialIndex - paddingItemCount
            } else {
                selectedIndex
            }
            listState.scrollToItem(scrollIndex.coerceAtLeast(0))
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (!isScrolling && items.isNotEmpty()) {
                    val centerOffset = paddingItemCount * itemHeightPx
                    val firstVisibleIndex = listState.firstVisibleItemIndex
                    val firstVisibleOffset = listState.firstVisibleItemScrollOffset

                    val centerItemIndex = firstVisibleIndex + 
                        ((centerOffset + firstVisibleOffset) / itemHeightPx).toInt()
                    
                    val actualIndex = if (infiniteScroll) {
                        centerItemIndex % items.size
                    } else {
                        (centerItemIndex - paddingItemCount).coerceIn(0, items.size - 1)
                    }
                    
                    onItemSelected(actualIndex)
                }
            }
    }

    Box(
        modifier = modifier.height(totalHeight),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * (visibleItemsCount / 2))
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * (visibleItemsCount / 2))
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!infiniteScroll) {
                items(paddingItemCount) {
                    Spacer(modifier = Modifier.height(itemHeight))
                }
            }
            
            items(
                count = virtualListSize,
                key = { if (infiniteScroll) it else it + paddingItemCount }
            ) { virtualIndex ->
                val actualIndex = if (infiniteScroll) {
                    virtualIndex % items.size
                } else {
                    virtualIndex
                }

                if (actualIndex in items.indices) {
                    val item = items[actualIndex]
                    val adjustedVirtualIndex = if (infiniteScroll) virtualIndex else virtualIndex + paddingItemCount

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .graphicsLayer {
                                val itemCenter = (adjustedVirtualIndex - listState.firstVisibleItemIndex) * itemHeightPx - 
                                    listState.firstVisibleItemScrollOffset + itemHeightPx / 2
                                val viewportCenter = (visibleItemsCount * itemHeightPx) / 2
                                val distanceFromCenter = kotlin.math.abs(itemCenter - viewportCenter)
                                val maxDistance = viewportCenter

                                val scale = 1f - (distanceFromCenter / maxDistance * 0.3f).coerceIn(0f, 0.3f)
                                scaleX = scale
                                scaleY = scale

                                alpha = 1f - (distanceFromCenter / maxDistance * 0.6f).coerceIn(0f, 0.6f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            if (!infiniteScroll) {
                items(paddingItemCount) {
                    Spacer(modifier = Modifier.height(itemHeight))
                }
            }
        }
    }
}
