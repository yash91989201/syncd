package com.example.syncd.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

val mainModules = module {
}

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null,
) {
    startKoin {
        config?.invoke(this)
        modules(mainModules, navigationModule)
    }
}