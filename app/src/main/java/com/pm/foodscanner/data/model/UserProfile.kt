package com.pm.foodscanner.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val weight: Float? = null,
    val height: Float? = null,
    val age: Int? = null,
    @SerialName("target_weight")
    val targetWeight: Float? = null,
    val gender: String = "male",
    @SerialName("target_date_months")
    val targetDateMonths: Int? = null,
    @SerialName("is_halal")
    val isHalal: Boolean = false,
    @SerialName("is_lactose_free")
    val isLactoseFree: Boolean = false,
    @SerialName("is_vegan")
    val isVegan: Boolean = false,
    val allergies: List<String> = emptyList()
)
