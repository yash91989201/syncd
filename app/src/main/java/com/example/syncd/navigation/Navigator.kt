package com.example.syncd.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator(startDestination: Any) {

    val backStack: SnapshotStateList<Any> = mutableStateListOf(startDestination)

    fun navigateTo(destination: Any) {
        backStack.add(destination)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }

    fun setRoot(destination: Any) {
        backStack.clear()
        backStack.add(destination)
    }
}