package com.pm.foodscanner.ui.barcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pm.foodscanner.data.model.Product
import com.pm.foodscanner.data.repository.AuthRepository
import com.pm.foodscanner.data.repository.BarcodeRepository
import com.pm.foodscanner.data.repository.FoodHistoryRepository
import com.pm.foodscanner.data.repository.ProfileRepository
import com.pm.foodscanner.util.EvaluationResult
import com.pm.foodscanner.util.PreferenceEvaluator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BarcodeUiState(
    val isScanning: Boolean = true,
    val isLoading: Boolean = false,
    val barcode: String? = null,
    val product: Product? = null,
    val evaluation: EvaluationResult? = null,
    val error: String? = null,
    val addedToHistory: Boolean = false,
    val addedToFavorites: Boolean = false
)

@HiltViewModel
class BarcodeViewModel @Inject constructor(
    private val barcodeRepository: BarcodeRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val historyRepository: FoodHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeUiState())
    val uiState: StateFlow<BarcodeUiState> = _uiState.asStateFlow()

    private var hasProcessedBarcode = false

    fun onBarcodeDetected(barcode: String) {
        if (hasProcessedBarcode) return
        hasProcessedBarcode = true

        _uiState.value = BarcodeUiState(isScanning = false, isLoading = true, barcode = barcode)

        viewModelScope.launch {
            try {
                val response = barcodeRepository.lookupBarcode(barcode)

                if (response.status == 0 || response.product == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Product not found for barcode: $barcode\n\n" +
                            "This product may not yet be in the global database. " +
                            "This is common for products sold in Central Asia.\n\n" +
                            "Try using Meal Scan to analyze the food by photo instead."
                    )
                    return@launch
                }

                val product = response.product
                val userId = authRepository.currentUserId()
                val profile = userId?.let { profileRepository.getProfile(it) }
                val evaluation = profile?.let { PreferenceEvaluator.evaluate(it, product) }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    product = product,
                    evaluation = evaluation
                )

                barcodeRepository.saveScanToHistory(
                    productName = product.productName,
                    calories = product.nutriments?.energyKcal100g,
                    protein = product.nutriments?.proteins100g,
                    fat = product.nutriments?.fat100g,
                    carbs = product.nutriments?.carbohydrates100g,
                    isCompatible = evaluation?.isCompatible
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to look up product"
                )
            }
        }
    }

    fun addToHistory() {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            try {
                historyRepository.addEntry(
                    name = product.productName ?: "Unknown product",
                    calories = product.nutriments?.energyKcal100g ?: 0f,
                    protein = product.nutriments?.proteins100g,
                    fat = product.nutriments?.fat100g,
                    carbs = product.nutriments?.carbohydrates100g
                )
                _uiState.value = _uiState.value.copy(addedToHistory = true)
            } catch (_: Exception) {}
        }
    }

    fun addToFavorites() {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            try {
                historyRepository.addToFavorites(
                    name = product.productName ?: "Unknown product",
                    calories = product.nutriments?.energyKcal100g ?: 0f,
                    protein = product.nutriments?.proteins100g,
                    fat = product.nutriments?.fat100g,
                    carbs = product.nutriments?.carbohydrates100g
                )
                _uiState.value = _uiState.value.copy(addedToFavorites = true)
            } catch (_: Exception) {}
        }
    }

    fun resetScan() {
        hasProcessedBarcode = false
        _uiState.value = BarcodeUiState()
    }
}
