package com.pm.foodscanner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodPrediction(
    val label: String,
    val score: Float
)

data class MealAnalysis(
    val predictions: List<FoodPrediction>,
    val topFood: String,
    val confidence: Float,
    val estimatedCalories: Float,
    val estimatedProtein: Float,
    val estimatedFat: Float,
    val estimatedCarbs: Float,
    val source: String = "local"
)
