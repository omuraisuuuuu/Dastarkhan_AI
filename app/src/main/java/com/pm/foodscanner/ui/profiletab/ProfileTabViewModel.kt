package com.pm.foodscanner.ui.profiletab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pm.foodscanner.data.model.UserProfile
import com.pm.foodscanner.data.repository.AuthRepository
import com.pm.foodscanner.data.repository.FoodHistoryRepository
import com.pm.foodscanner.data.repository.ProfileRepository
import com.pm.foodscanner.ui.theme.ThemeMode
import com.pm.foodscanner.util.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

data class ProfileTabUiState(
    val profile: UserProfile? = null,
    val recommendedCalories: Int = 2000,
    val consumedCalories: Float = 0f,
    val consumedProtein: Float = 0f,
    val consumedFat: Float = 0f,
    val consumedCarbs: Float = 0f,
    val currentLanguage: String = "en",
    val currentTheme: ThemeMode = ThemeMode.System,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileTabViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val historyRepository: FoodHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileTabUiState())
    val uiState: StateFlow<ProfileTabUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setLanguage(language: String) {
        _uiState.value = _uiState.value.copy(currentLanguage = language)
    }

    fun setTheme(theme: ThemeMode) {
        _uiState.value = _uiState.value.copy(currentTheme = theme)
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = authRepository.currentUserId()
            if (userId != null) {
                val profile = profileRepository.getProfile(userId)
                Log.d("ProfileTabViewModel", "Loaded profile - Gender: ${profile?.gender}, Age: ${profile?.age}, Weight: ${profile?.weight}")
                val todayEntries = historyRepository.getTodayEntries()
                val recommended = if (profile != null) CalorieCalculator.calculateDailyCalories(profile) else 2000
                val consumed = todayEntries.sumOf { it.calories.toDouble() }.toFloat()
                val protein = todayEntries.sumOf { (it.protein ?: 0f).toDouble() }.toFloat()
                val fat = todayEntries.sumOf { (it.fat ?: 0f).toDouble() }.toFloat()
                val carbs = todayEntries.sumOf { (it.carbs ?: 0f).toDouble() }.toFloat()
                _uiState.value = ProfileTabUiState(
                    profile = profile,
                    recommendedCalories = recommended,
                    consumedCalories = consumed,
                    consumedProtein = protein,
                    consumedFat = fat,
                    consumedCarbs = carbs,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
