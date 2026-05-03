package com.pm.foodscanner.utils

import android.content.Context

fun Context.getString(foodName: String, languageCode: String): String {
    return FoodTranslator.translateFoodName(foodName, languageCode)
}

fun Context.getCurrentLanguage(): String {
    return LanguageManager.getCurrentLanguage(this)
}

fun Context.setAppLanguage(languageCode: String) {
    LanguageManager.setLanguage(this, languageCode)
}
