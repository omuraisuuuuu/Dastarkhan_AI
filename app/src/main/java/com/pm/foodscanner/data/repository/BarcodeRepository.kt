package com.pm.foodscanner.data.repository

import com.pm.foodscanner.data.model.OpenFoodFactsResponse
import com.pm.foodscanner.data.model.ScanHistoryItem
import com.pm.foodscanner.data.remote.OpenFoodFactsApi
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class BarcodeRepository @Inject constructor(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val supabaseClient: SupabaseClient
) {
    suspend fun lookupBarcode(barcode: String): OpenFoodFactsResponse {
        return openFoodFactsApi.getProduct(barcode)
    }

    suspend fun saveScanToHistory(
        productName: String?,
        calories: Float?,
        protein: Float?,
        fat: Float?,
        carbs: Float?,
        isCompatible: Boolean?
    ) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val item = ScanHistoryItem(
            userId = userId,
            scanType = "barcode",
            productName = productName,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            isCompatible = isCompatible
        )
        try {
            supabaseClient.from("scan_history").insert(item)
        } catch (_: Exception) { }
    }
}
