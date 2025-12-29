package com.example.syncd.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val HAS_SEEN_INTRO = booleanPreferencesKey("has_seen_intro")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val HAS_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

        // Profile keys
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_AGE = intPreferencesKey("profile_age")
        val PROFILE_SPORT = stringPreferencesKey("profile_sport")
        val PROFILE_LANGUAGE = stringPreferencesKey("profile_language")
        val IS_PROFILE_COMPLETED = booleanPreferencesKey("is_profile_completed")

        // Cycle keys
        val LAST_PERIOD_START_DATE = longPreferencesKey("last_period_start_date")
        val IS_ON_HORMONAL_MEDICATION = booleanPreferencesKey("is_on_hormonal_medication")
        val IS_CYCLE_SETUP_COMPLETED = booleanPreferencesKey("is_cycle_setup_completed")
    }

    val hasSeenIntro: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAS_SEEN_INTRO] ?: false
        }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    val hasSeenTutorial: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAS_SEEN_TUTORIAL] ?: false
        }

    val isProfileCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_PROFILE_COMPLETED] ?: false
        }

    val isCycleSetupCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_CYCLE_SETUP_COMPLETED] ?: false
        }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] ?: false
        }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setHasSeenIntro(hasSeen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_INTRO] = hasSeen
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun setHasSeenTutorial(hasSeen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_TUTORIAL] = hasSeen
        }
    }

    suspend fun saveProfile(name: String, age: Int, sport: String, language: String) {
        context.dataStore.edit { preferences ->
            preferences[PROFILE_NAME] = name
            preferences[PROFILE_AGE] = age
            preferences[PROFILE_SPORT] = sport
            preferences[PROFILE_LANGUAGE] = language
            preferences[IS_PROFILE_COMPLETED] = true
        }
    }

    suspend fun saveCycleData(lastPeriodStart: Long, isOnMedication: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LAST_PERIOD_START_DATE] = lastPeriodStart
            preferences[IS_ON_HORMONAL_MEDICATION] = isOnMedication
            preferences[IS_CYCLE_SETUP_COMPLETED] = true
        }
    }

    val lastPeriodStartDate: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_PERIOD_START_DATE]
        }
}