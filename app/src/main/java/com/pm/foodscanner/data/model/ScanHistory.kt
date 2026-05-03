package com.pm.foodscanner.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScanHistoryItem(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("scan_type")
    val scanType: String = "",
    @SerialName("product_name")
    val productName: String? = null,
    val calories: Float? = null,
    val protein: Float? = null,
    val fat: Float? = null,
    val carbs: Float? = null,
    @SerialName("is_compatible")
    val isCompatible: Boolean? = null
)
