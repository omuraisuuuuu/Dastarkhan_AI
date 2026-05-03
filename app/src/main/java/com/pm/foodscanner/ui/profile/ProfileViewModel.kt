package com.pm.foodscanner.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pm.foodscanner.data.model.UserProfile
import com.pm.foodscanner.data.repository.AuthRepository
import com.pm.foodscanner.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val weight: String = "",
    val height: String = "",
    val targetWeight: String = "",
    val gender: String = "male",
    val targetDateMonths: String = "",
    val isHalal: Boolean = false,
    val isLactoseFree: Boolean = false,
    val isVegan: Boolean = false,
    val allergies: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId() ?: return@launch
            val profile = profileRepository.getProfile(userId) ?: return@launch
            _uiState.value = ProfileUiState(
                weight = profile.weight?.toString() ?: "",
                height = profile.height?.toString() ?: "",
                targetWeight = profile.targetWeight?.toString() ?: "",
                gender = profile.gender,
                targetDateMonths = profile.targetDateMonths?.toString() ?: "",
                isHalal = profile.isHalal,
                isLactoseFree = profile.isLactoseFree,
                isVegan = profile.isVegan,
                allergies = profile.allergies.joinToString(", ")
            )
        }
    }

    fun updateWeight(value: String) { _uiState.value = _uiState.value.copy(weight = value) }
    fun updateHeight(value: String) { _uiState.value = _uiState.value.copy(height = value) }
    fun updateTargetWeight(value: String) { _uiState.value = _uiState.value.copy(targetWeight = value) }
    fun updateGender(value: String) { _uiState.value = _uiState.value.copy(gender = value) }
    fun updateTargetDateMonths(value: String) { _uiState.value = _uiState.value.copy(targetDateMonths = value) }
    fun updateHalal(value: Boolean) { _uiState.value = _uiState.value.copy(isHalal = value) }
    fun updateLactoseFree(value: Boolean) { _uiState.value = _uiState.value.copy(isLactoseFree = value) }
    fun updateVegan(value: Boolean) { _uiState.value = _uiState.value.copy(isVegan = value) }
    fun updateAllergies(value: String) { _uiState.value = _uiState.value.copy(allergies = value) }

    fun saveProfile() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId() ?: return@launch
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val profile = UserProfile(
                    id = userId,
                    weight = state.weight.toFloatOrNull(),
                    height = state.height.toFloatOrNull(),
                    targetWeight = state.targetWeight.toFloatOrNull(),
                    gender = state.gender,
                    targetDateMonths = state.targetDateMonths.toIntOrNull(),
                    isHalal = state.isHalal,
                    isLactoseFree = state.isLactoseFree,
                    isVegan = state.isVegan,
                    allergies = state.allergies
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                )
                profileRepository.upsertProfile(profile)
                _uiState.value = state.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save profile"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
