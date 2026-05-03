package com.pm.foodscanner.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.pm.foodscanner.ui.theme.ThemeMode
import com.pm.foodscanner.utils.LanguageManager

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

object PreferencesDataStore {
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    fun getLanguage(context: Context): Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: LanguageManager.LANGUAGE_ENGLISH
        }

    suspend fun setLanguage(context: Context, language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    fun getThemeMode(context: Context): Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_MODE_KEY] ?: ThemeMode.System.name
            ThemeMode.valueOf(themeName)
        }

    suspend fun setThemeMode(context: Context, themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }
}
