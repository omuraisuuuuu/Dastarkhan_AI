package com.pm.foodscanner.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LanguageManager {
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_RUSSIAN = "ru"
    const val LANGUAGE_KAZAKH = "kk"

    private const val PREFS_NAME = "app_language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        }

        return context.createConfigurationContext(config)
    }

    fun setLanguage(context: Context, languageCode: String) {
        saveLanguage(context, languageCode)
        applyLanguage(context, languageCode)
    }

    fun getCurrentLanguage(context: Context): String {
        return getSavedLanguage(context)
    }

    fun getLanguageName(code: String): String = when (code) {
        LANGUAGE_ENGLISH -> "English"
        LANGUAGE_RUSSIAN -> "Русский"
        LANGUAGE_KAZAKH -> "Қазақша"
        else -> "English"
    }
}
