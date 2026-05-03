package com.pm.foodscanner.util

import com.pm.foodscanner.data.model.UserProfile
import kotlin.math.roundToInt

object CalorieCalculator {
    // Harris-Benedict BMR, assuming moderate activity (×1.55)
    fun calculateDailyCalories(profile: UserProfile): Int {
        val weight = profile.weight ?: return 2000
        val height = profile.height ?: return 2000

        val bmr = if (profile.gender == "female") {
            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * 30.0)
        } else {
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * 30.0)
        }

        val tdee = bmr * 1.55

        val targetWeight = profile.targetWeight
        return if (targetWeight != null && targetWeight < weight) {
            (tdee - 500).roundToInt().coerceAtLeast(1200)
        } else if (targetWeight != null && targetWeight > weight) {
            (tdee + 300).roundToInt()
        } else {
            tdee.roundToInt()
        }
    }
}
