package com.pm.foodscanner.data.repository

import android.util.Log
import com.pm.foodscanner.data.local.LocalFoodClassifier
import com.pm.foodscanner.data.model.MealAnalysis
import com.pm.foodscanner.data.model.ScanHistoryItem
import com.pm.foodscanner.data.remote.HuggingFaceApi
import com.pm.foodscanner.data.remote.RoboflowApi
import com.pm.foodscanner.util.NutritionDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MealRepository @Inject constructor(
    private val localFoodClassifier: LocalFoodClassifier,
    private val roboflowApi: RoboflowApi,
    private val huggingFaceApi: HuggingFaceApi,
    private val supabaseClient: SupabaseClient
) {
    suspend fun analyzeMeal(imageBytes: ByteArray): MealAnalysis {
        Log.d(TAG, "=== analyzeMeal: ${imageBytes.size} bytes ===")

        try {
            Log.d(TAG, "Trying Roboflow...")
            val predictions = withContext(Dispatchers.IO) {
                roboflowApi.classifyImage(imageBytes)
            }
            val top = predictions.firstOrNull()
            if (top != null) {
                val threshold = if (top.label == "doner") DONER_CONFIDENCE_THRESHOLD else CONFIDENCE_THRESHOLD
                if (top.score >= threshold) {
                    Log.d(TAG, "Roboflow OK: ${top.label} (${top.score})")
                    return buildAnalysisFromPredictions(predictions, "Roboflow AI")
                }
            }
            Log.d(TAG, "Roboflow confidence too low: ${top?.label}=${top?.score}, trying HuggingFace...")
        } catch (e: Exception) {
            Log.w(TAG, "Roboflow failed: ${e.message}")
        }

        try {
            Log.d(TAG, "Trying HuggingFace...")
            val predictions = withContext(Dispatchers.IO) {
                huggingFaceApi.classifyImage(imageBytes)
            }
            Log.d(TAG, "HuggingFace OK: ${predictions.firstOrNull()?.label}")
            return buildAnalysisFromPredictions(predictions, "HuggingFace AI")
        } catch (e: Exception) {
            Log.w(TAG, "HuggingFace failed: ${e.message}")
        }

        Log.d(TAG, "Trying local model...")
        val predictions = if (localFoodClassifier.isAvailable()) {
            withContext(Dispatchers.Default) {
                localFoodClassifier.classify(imageBytes)
            }
        } else {
            throw Exception("No recognition engines available")
        }
        return buildAnalysisFromPredictions(predictions, "Local model")
    }

    private fun buildAnalysisFromPredictions(
        predictions: List<com.pm.foodscanner.data.model.FoodPrediction>,
        source: String
    ): MealAnalysis {
        val topPredictions = predictions.take(5)
        val topFood = topPredictions.firstOrNull()
            ?: throw Exception("No food detected")

        val nutrition = NutritionDatabase.getNutrition(topFood.label)

        return MealAnalysis(
            predictions = topPredictions,
            topFood = topFood.label.replace("_", " ").replaceFirstChar { it.uppercase() },
            confidence = topFood.score,
            estimatedCalories = nutrition.calories,
            estimatedProtein = nutrition.protein,
            estimatedFat = nutrition.fat,
            estimatedCarbs = nutrition.carbs,
            source = source
        )
    }

    suspend fun saveScanToHistory(analysis: MealAnalysis) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val item = ScanHistoryItem(
            userId = userId,
            scanType = "meal",
            productName = analysis.topFood,
            calories = analysis.estimatedCalories,
            protein = analysis.estimatedProtein,
            fat = analysis.estimatedFat,
            carbs = analysis.estimatedCarbs,
            isCompatible = null
        )
        try {
            supabaseClient.from("scan_history").insert(item)
        } catch (_: Exception) { }
    }

    companion object {
        private const val TAG = "MealRepository"
        private const val CONFIDENCE_THRESHOLD = 0.75f
        private const val DONER_CONFIDENCE_THRESHOLD = 0.90f
    }
}
