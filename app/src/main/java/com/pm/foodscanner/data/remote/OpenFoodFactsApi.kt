package com.pm.foodscanner.data.remote

import com.pm.foodscanner.data.model.OpenFoodFactsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class OpenFoodFactsApi @Inject constructor(
    private val httpClient: HttpClient
) {
    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/api/v2/product"
    }

    suspend fun getProduct(barcode: String): OpenFoodFactsResponse {
        return httpClient.get("$BASE_URL/$barcode.json") {
            parameter("fields", "product_name,nutriments,allergens_tags,ingredients_text,labels_tags,categories_tags")
        }.body()
    }
}
