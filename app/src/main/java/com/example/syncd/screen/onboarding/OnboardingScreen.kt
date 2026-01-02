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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
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
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
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
                            style = MaterialTheme.typography.labelLarge,
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
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = (state.currentStepIndex + 1) / state.totalSteps.toFloat(),
                        animationSpec = tween(durationMillis = 400),
                        label = "progress"
                    )

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

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
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.headlineMedium.lineHeight
                            )

                            if (step.helperText != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    onClick = { },
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = stringResource(R.string.onboarding_info),
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = stringResource(R.string.onboarding_why_ask),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            when (step.stepType) {
                                StepType.DATE_PICKER -> {
                                    LastPeriodDatePicker(
                                        selectedDateMillis = state.lastPeriodDate,
                                        onDateSelected = { viewModel.onLastPeriodDateSelected(it) }
                                    )
                                }
                                StepType.OPTIONS -> {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    OutlinedTextField(
                                                        value = state.customSport,
                                                        onValueChange = { viewModel.onCustomSportChanged(it) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        placeholder = { 
                                                            Text(stringResource(R.string.onboarding_custom_sport_placeholder)) 
                                                        },
                                                        shape = RoundedCornerShape(16.dp),
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
                                                    Spacer(modifier = Modifier.height(24.dp))
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
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
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
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.onboarding_back),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (state.currentStepIndex == 0) {
                            Spacer(modifier = Modifier.width(48.dp))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { viewModel.onNext() },
                            modifier = Modifier
                                .height(56.dp)
                                .width(150.dp),
                            shape = RoundedCornerShape(28.dp),
                            enabled = state.canProceed && !state.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            ),
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
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
                                    style = MaterialTheme.typography.titleMedium
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
        shape = RoundedCornerShape(16.dp),
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
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
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
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.onboarding_selected),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastPeriodDatePicker(
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit
) {
    val today = System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= today
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year <= java.time.Year.now().value
            }
        }
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            onDateSelected(millis)
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.fillMaxWidth(),
            showModeToggle = false,
            title = null,
            headline = {
                Text(
                    text = if (selectedDateMillis != null) {
                        java.time.Instant.ofEpochMilli(selectedDateMillis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    } else {
                        stringResource(R.string.onboarding_date_picker_select)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
