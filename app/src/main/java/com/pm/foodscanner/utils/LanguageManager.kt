package com.pm.foodscanner.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LanguageManager {
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_RUSSIAN = "ru"
    const val LANGUAGE_KAZAKH = "kk"

    fun setLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
        }

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getCurrentLanguage(context: Context): String {
        return context.resources.configuration.locale.language
    }

    fun getLanguageName(code: String): String = when (code) {
        LANGUAGE_ENGLISH -> "English"
        LANGUAGE_RUSSIAN -> "Русский"
        LANGUAGE_KAZAKH -> "Қазақша"
        else -> "English"
    }
}
