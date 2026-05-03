package com.pm.foodscanner.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pm.foodscanner.data.model.FoodEntry
import com.pm.foodscanner.data.repository.FoodHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<FoodEntry> = emptyList(),
    val isLoading: Boolean = false,
    val addedToHistoryId: String? = null,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val historyRepository: FoodHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val favs = historyRepository.getFavorites()
                _uiState.value = _uiState.value.copy(favorites = favs, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun addToHistoryNow(entry: FoodEntry) {
        viewModelScope.launch {
            try {
                historyRepository.addFavoriteToHistory(entry)
                _uiState.value = _uiState.value.copy(addedToHistoryId = entry.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeFavorite(entryId: String) {
        viewModelScope.launch {
            try {
                historyRepository.removeFavorite(entryId)
                loadFavorites()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearAddedSignal() { _uiState.value = _uiState.value.copy(addedToHistoryId = null) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
