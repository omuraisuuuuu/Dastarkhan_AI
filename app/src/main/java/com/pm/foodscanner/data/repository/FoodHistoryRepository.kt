package com.pm.foodscanner.data.repository

import com.pm.foodscanner.data.model.FoodEntry
import com.pm.foodscanner.data.model.FoodEntryInsert
import com.pm.foodscanner.util.AppTimeZone
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class FoodHistoryRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun nowIso() = utcFormat.format(Date())

    private fun dayRangeUtc(localDateMillis: Long): Pair<String, String> {
        val localTz = AppTimeZone.timeZone

        val startCal = Calendar.getInstance(localTz).apply {
            timeInMillis = localDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMs = startCal.timeInMillis

        val endCal = Calendar.getInstance(localTz).apply {
            timeInMillis = localDateMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endMs = endCal.timeInMillis

        val startUtc = utcFormat.format(Date(startMs))
        val endUtc = utcFormat.format(Date(endMs))

        return startUtc to endUtc
    }

    suspend fun addEntry(name: String, calories: Float, protein: Float?, fat: Float?, carbs: Float?) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val entry = FoodEntryInsert(
            userId = userId,
            name = name,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            eatenAt = nowIso(),
            isFavorite = false
        )
        withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries").insert(entry)
        }
    }

    suspend fun getEntriesForDate(localDateMillis: Long): List<FoodEntry> {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return emptyList()
        val (start, end) = dayRangeUtc(localDateMillis)

        return withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_favorite", false)
                        gte("eaten_at", start)
                        lte("eaten_at", end)
                    }
                    order("eaten_at", Order.DESCENDING)
                }
                .decodeList<FoodEntry>()
        }
    }

    suspend fun getAllHistoryEntries(): List<FoodEntry> {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return emptyList()
        return withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_favorite", false)
                    }
                    order("eaten_at", Order.DESCENDING)
                }
                .decodeList<FoodEntry>()
        }
    }

    suspend fun getTodayEntries(): List<FoodEntry> =
        getEntriesForDate(System.currentTimeMillis())

    suspend fun clearAllHistory() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("is_favorite", false)
                    }
                }
        }
    }

    suspend fun deleteEntry(entryId: String) {
        withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries")
                .delete { filter { eq("id", entryId) } }
        }
    }

    suspend fun getFavorites(): List<FoodEntry> {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return emptyList()
        return withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_favorite", true)
                    }
                    order("name", Order.ASCENDING)
                }
                .decodeList<FoodEntry>()
        }
    }

    suspend fun addToFavorites(name: String, calories: Float, protein: Float?, fat: Float?, carbs: Float?) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val existing = withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("name", name)
                        eq("is_favorite", true)
                    }
                }
                .decodeList<FoodEntry>()
        }
        if (existing.isNotEmpty()) return
        val entry = FoodEntryInsert(
            userId = userId,
            name = name,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            eatenAt = nowIso(),
            isFavorite = true
        )
        withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries").insert(entry)
        }
    }

    suspend fun addFavoriteToHistory(entry: FoodEntry) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val insert = FoodEntryInsert(
            userId = userId,
            name = entry.name,
            calories = entry.calories,
            protein = entry.protein,
            fat = entry.fat,
            carbs = entry.carbs,
            eatenAt = nowIso(),
            isFavorite = false
        )
        withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries").insert(insert)
        }
    }

    suspend fun removeFavorite(entryId: String) {
        withContext(Dispatchers.IO) {
            supabaseClient.from("food_entries")
                .delete { filter { eq("id", entryId) } }
        }
    }
}
