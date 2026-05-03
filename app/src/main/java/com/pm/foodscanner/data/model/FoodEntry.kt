package com.pm.foodscanner.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class FoodEntry(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val name: String = "",
    val calories: Float = 0f,
    val protein: Float? = null,
    val fat: Float? = null,
    val carbs: Float? = null,
    @SerialName("eaten_at")
    val eatenAt: String = "",
    @SerialName("is_favorite")
    val isFavorite: Boolean = false
)

@Serializable
data class FoodEntryInsert(
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val calories: Float,
    val protein: Float? = null,
    val fat: Float? = null,
    val carbs: Float? = null,
    @SerialName("eaten_at")
    val eatenAt: String,
    @SerialName("is_favorite")
    val isFavorite: Boolean = false
)
