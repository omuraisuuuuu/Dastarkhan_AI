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
        supabaseClient.from("profiles").upsert(profile)
    }
}
