package com.pm.foodscanner.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pm.foodscanner.data.model.MealAnalysis
import com.pm.foodscanner.data.repository.FoodHistoryRepository
import com.pm.foodscanner.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MealUiState(
    val isCapturing: Boolean = true,
    val isAnalyzing: Boolean = false,
    val analysis: MealAnalysis? = null,
    val error: String? = null,
    val addedToHistory: Boolean = false,
    val addedToFavorites: Boolean = false
)

@HiltViewModel
class MealViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val historyRepository: FoodHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealUiState())
    val uiState: StateFlow<MealUiState> = _uiState.asStateFlow()

    fun analyzeImage(imageBytes: ByteArray) {
        _uiState.value = MealUiState(isCapturing = false, isAnalyzing = true)

        viewModelScope.launch {
            try {
                val analysis = mealRepository.analyzeMeal(imageBytes)
                mealRepository.saveScanToHistory(analysis)
                _uiState.value = MealUiState(isCapturing = false, isAnalyzing = false, analysis = analysis)
            } catch (e: Exception) {
                _uiState.value = MealUiState(
                    isCapturing = false,
                    isAnalyzing = false,
                    error = e.message ?: "Failed to analyze meal"
                )
            }
        }
    }

    fun addToHistory() {
        val analysis = _uiState.value.analysis ?: return
        viewModelScope.launch {
            try {
                historyRepository.addEntry(
                    name = analysis.topFood,
                    calories = analysis.estimatedCalories,
                    protein = analysis.estimatedProtein,
                    fat = analysis.estimatedFat,
                    carbs = analysis.estimatedCarbs
                )
                _uiState.value = _uiState.value.copy(addedToHistory = true)
            } catch (_: Exception) {}
        }
    }

    fun addToFavorites() {
        val analysis = _uiState.value.analysis ?: return
        viewModelScope.launch {
            try {
                historyRepository.addToFavorites(
                    name = analysis.topFood,
                    calories = analysis.estimatedCalories,
                    protein = analysis.estimatedProtein,
                    fat = analysis.estimatedFat,
                    carbs = analysis.estimatedCarbs
                )
                _uiState.value = _uiState.value.copy(addedToFavorites = true)
            } catch (_: Exception) {}
        }
    }

    fun resetScan() {
        _uiState.value = MealUiState()
    }
}
