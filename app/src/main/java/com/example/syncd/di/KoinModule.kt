package com.example.syncd.di

import com.example.syncd.auth.data.repository.AuthRepository
import com.example.syncd.auth.presentation.AuthViewModel
import com.example.syncd.data.UserPreferences
import com.example.syncd.screen.guide.TodayGuideViewModel
import com.example.syncd.screen.home.HomeViewModel
import com.example.syncd.screen.insights.InsightsViewModel
import com.example.syncd.screen.log.LogViewModel
import com.example.syncd.screen.home.data.repository.HomeRepository
import com.example.syncd.screen.log.data.repository.LogRepository
import com.example.syncd.screen.onboarding.OnboardingViewModel
import com.example.syncd.screen.onboarding.data.repository.OnboardingRepository
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { UserPreferences(get()) }
    single { AuthRepository(get()) }
    single { OnboardingRepository(get()) }
    single { AuthViewModel(get(), get(), get(), get()) }
}

val onboardingModule = module {
    viewModel { OnboardingViewModel(get(), get()) }
}

val logModule = module {
    single { LogRepository(get()) }
    viewModel { LogViewModel(get()) }
}

val guideModule = module {
    viewModel { TodayGuideViewModel() }
}

val insightsModule = module {
    viewModel { InsightsViewModel() }
}

val homeModule = module {
    single { HomeRepository(get()) }
    viewModel { HomeViewModel(get()) }
}

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null,
) {
    startKoin {
        config?.invoke(this)
        modules(
            networkModule,
            authModule,
            navigationModule,
            onboardingModule,
            logModule,
            guideModule,
            insightsModule,
            homeModule
        )
    }
}