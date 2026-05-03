package com.pm.foodscanner.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsResponse(
    val status: Int = 0,
    val product: Product? = null,
    @SerialName("status_verbose")
    val statusVerbose: String = ""
)

@Serializable
data class Product(
    @SerialName("product_name")
    val productName: String? = null,
    val nutriments: Nutriments? = null,
    @SerialName("allergens_tags")
    val allergensTags: List<String> = emptyList(),
    @SerialName("ingredients_text")
    val ingredientsText: String? = null,
    @SerialName("labels_tags")
    val labelsTags: List<String> = emptyList(),
    @SerialName("categories_tags")
    val categoriesTags: List<String> = emptyList()
)

@Serializable
data class Nutriments(
    @SerialName("energy-kcal_100g")
    val energyKcal100g: Float? = null,
    @SerialName("proteins_100g")
    val proteins100g: Float? = null,
    @SerialName("fat_100g")
    val fat100g: Float? = null,
    @SerialName("carbohydrates_100g")
    val carbohydrates100g: Float? = null
)
