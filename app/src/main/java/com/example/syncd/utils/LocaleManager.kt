package com.example.syncd.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.localeDataStore by preferencesDataStore(name = "locale_prefs")

class LocaleManager(private val context: Context) {
    
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private const val PREFS_NAME = "locale_prefs"
        private const val KEY_LANGUAGE = "app_language"
        const val ENGLISH = "en"
        const val HINDI = "hi"
        const val ORIYA = "or"
        
        val supportedLanguages = listOf(
            Language(ENGLISH, "English"),
            Language(HINDI, "हिंदी"),
            Language(ORIYA, "ଓଡ଼ିଆ")
        )
        
        fun getSavedLanguage(context: Context): String {
            val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPrefs.getString(KEY_LANGUAGE, ENGLISH) ?: ENGLISH
        }
        
        fun applyLocaleToContext(context: Context, languageCode: String): Context {
            val locale = Locale.forLanguageTag(languageCode)
            Locale.setDefault(locale)
            
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            
            return context.createConfigurationContext(configuration)
        }
    }
    
    data class Language(val code: String, val nativeName: String)
    
    val currentLanguage: Flow<String> = context.localeDataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: ENGLISH
        }
    
    suspend fun setLanguage(languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
        
        context.localeDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
    
    fun applyLocale(languageCode: String): Context {
        return applyLocaleToContext(context, languageCode)
    }
}
