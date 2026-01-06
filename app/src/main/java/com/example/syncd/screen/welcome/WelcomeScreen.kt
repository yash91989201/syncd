package com.example.syncd.screen.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.syncd.MainActivity
import com.example.syncd.R
import com.example.syncd.navigation.Navigator
import com.example.syncd.navigation.Screen
import com.example.syncd.utils.LocaleManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun WelcomeScreen() {
    val navigator = koinInject<Navigator>()
    val context = LocalContext.current
    val localeManager = koinInject<LocaleManager>()
    val scope = rememberCoroutineScope()
    
    val currentLanguage by localeManager.currentLanguage.collectAsState(initial = localeManager.getCurrentLanguageSync())
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, delayMillis = 200)
        )
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.surface
        )
    )

    // Get screen configuration for responsive sizing
    val configuration = LocalWindowInfo.current
    val screenHeight = configuration.containerDpSize.height
    val isCompactScreen = screenHeight < 600.dp
    
    // Calculate responsive values
    val horizontalContentPadding = 24.dp
    val imageHorizontalPadding = if (isCompactScreen) 12.dp else 20.dp
    val imageMinHeight = screenHeight * 0.22f
    val imageMaxHeight = screenHeight * 0.45f // Max 45% of screen height
    val topSpacing = if (isCompactScreen) 24.dp else 40.dp
    val sectionSpacing = if (isCompactScreen) 24.dp else 40.dp
    val bottomSpacing = if (isCompactScreen) 16.dp else 32.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Language selector button at top-right
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(1f)
                .padding(16.dp)
                .clickable { showLanguageDialog = true }
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = stringResource(R.string.welcome_select_language),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = LocaleManager.supportedLanguages().find { it.code == currentLanguage }?.nativeName
                    ?: stringResource(R.string.language_english),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 56.dp) // Space for language selector
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topSpacing))

            Text(
                text = stringResource(R.string.welcome_app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (isCompactScreen) 36.sp else 45.sp
                ),
                modifier = Modifier.padding(horizontal = horizontalContentPadding),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.welcome_tagline),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = if (isCompactScreen) 14.sp else 16.sp
                ),
                modifier = Modifier.padding(horizontal = horizontalContentPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(sectionSpacing))

            // Responsive image with dedicated padding and min/max height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = imageHorizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.welcome_image),
                    contentDescription = stringResource(R.string.welcome_app_name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = imageMinHeight, max = imageMaxHeight)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(sectionSpacing))

            Text(
                text = stringResource(R.string.welcome_headline),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = if (isCompactScreen) 18.sp else 22.sp,
                    lineHeight = if (isCompactScreen) 26.sp else 30.sp
                ),
                modifier = Modifier.padding(horizontal = horizontalContentPadding),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.welcome_description),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = if (isCompactScreen) 13.sp else 14.sp,
                    lineHeight = if (isCompactScreen) 18.sp else 20.sp
                ),
                modifier = Modifier.padding(horizontal = horizontalContentPadding),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(sectionSpacing))

            Button(
                onClick = { navigator.navigateTo(Screen.Login) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalContentPadding)
                    .height(if (isCompactScreen) 52.dp else 60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.welcome_button_get_started),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isCompactScreen) 14.sp else 16.sp
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.welcome_time_estimate),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = if (isCompactScreen) 11.sp else 12.sp
                ),
                modifier = Modifier.padding(horizontal = horizontalContentPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(bottomSpacing))
        }
        
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                onLanguageSelected = { languageCode ->
                    scope.launch {
                        localeManager.setLanguage(languageCode)
                        showLanguageDialog = false
                        (context as? MainActivity)?.restartActivity()
                    }
                },
                onDismiss = { showLanguageDialog = false }
            )
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.language_selector_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LocaleManager.supportedLanguages().forEach { language ->
                    LanguageOption(
                        language = language,
                        isSelected = language.code == currentLanguage,
                        onClick = { onLanguageSelected(language.code) }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.language_selector_cancel))
                }
            }
        }
    }
}

@Composable
fun LanguageOption(
    language: LocaleManager.Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
