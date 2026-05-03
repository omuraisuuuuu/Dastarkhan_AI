package com.pm.foodscanner.util

import com.pm.foodscanner.data.model.UserProfile
import kotlin.math.roundToInt

object CalorieCalculator {
    fun calculateDailyCalories(profile: UserProfile): Int {
        val weight = profile.weight ?: return 2000
        val height = profile.height ?: return 2000
        val age = profile.age ?: 30

        val bmr = if (profile.gender == "female") {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        }

        val tdee = bmr * 1.55

        val targetWeight = profile.targetWeight
        val targetDateMonths = profile.targetDateMonths

        return when {
            targetWeight == null || targetWeight == weight -> tdee.roundToInt()
            targetWeight < weight -> calculateDeficitCalories(tdee, weight, targetWeight, targetDateMonths, profile.gender)
            else -> calculateSurplusCalories(tdee, weight, targetWeight, targetDateMonths, profile.gender)
        }
    }

    private fun calculateDeficitCalories(tdee: Double, weight: Float, targetWeight: Float, monthsOrNull: Int?, gender: String): Int {
        val weightDelta = weight - targetWeight
        val totalCalorieDeficit = weightDelta * 7700

        val dailyAdjustment = if (monthsOrNull != null && monthsOrNull > 0) {
            val daysTotal = monthsOrNull * 30
            (totalCalorieDeficit / daysTotal).roundToInt().coerceIn(-1000, 0)
        } else {
            -500
        }

        val result = (tdee + dailyAdjustment).roundToInt()
        val minCalories = if (gender == "female") 1200 else 1500
        return result.coerceAtLeast(minCalories)
    }

    private fun calculateSurplusCalories(tdee: Double, weight: Float, targetWeight: Float, monthsOrNull: Int?, gender: String): Int {
        val weightDelta = targetWeight - weight
        val totalCalorieSurplus = weightDelta * 7700

        val dailyAdjustment = if (monthsOrNull != null && monthsOrNull > 0) {
            val daysTotal = monthsOrNull * 30
            (totalCalorieSurplus / daysTotal).roundToInt().coerceIn(0, 500)
        } else {
            300
        }

        return (tdee + dailyAdjustment).roundToInt()
    }
}
