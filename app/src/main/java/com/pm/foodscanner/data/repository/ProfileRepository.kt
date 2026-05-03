package com.pm.foodscanner.data.repository

import com.pm.foodscanner.data.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getProfile(userId: String): UserProfile? {
        return try {
            supabaseClient.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun upsertProfile(profile: UserProfile) {
        try {
            val existing = getProfile(profile.id)
            if (existing != null) {
                supabaseClient.from("profiles").update({
                    set("weight", profile.weight)
                    set("height", profile.height)
                    set("age", profile.age)
                    set("target_weight", profile.targetWeight)
                    set("gender", profile.gender)
                    set("target_date_months", profile.targetDateMonths)
                    set("is_halal", profile.isHalal)
                    set("is_lactose_free", profile.isLactoseFree)
                    set("is_vegan", profile.isVegan)
                    set("allergies", profile.allergies)
                }) {
                    filter { eq("id", profile.id) }
                }
            } else {
                supabaseClient.from("profiles").insert(profile)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}
