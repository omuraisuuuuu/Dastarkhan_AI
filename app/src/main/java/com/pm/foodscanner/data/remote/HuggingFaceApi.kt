package com.pm.foodscanner.data.remote

import com.pm.foodscanner.BuildConfig
import com.pm.foodscanner.data.model.FoodPrediction
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import javax.inject.Inject

class HuggingFaceApi @Inject constructor(
    private val httpClient: HttpClient
) {
    companion object {
        private const val MODEL_URL =
            "https://router.huggingface.co/hf-inference/models/nateraw/food"
        private const val MAX_RETRIES = 2
        private const val RETRY_DELAY_MS = 5_000L
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun classifyImage(imageBytes: ByteArray): List<FoodPrediction> {
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                val response: HttpResponse = httpClient.post(MODEL_URL) {
                    header(HttpHeaders.Authorization, "Bearer ${BuildConfig.HF_API_TOKEN}")
                    contentType(ContentType.Application.OctetStream)
                    setBody(imageBytes)
                }

                val status = response.status.value
                val bodyText = response.bodyAsText()

                if (status == 503) {
                    delay(RETRY_DELAY_MS)
                    return@repeat
                }
                if (status !in 200..299) {
                    throw Exception("Food recognition failed (HTTP $status). Try again later.")
                }

                return json.decodeFromString<List<FoodPrediction>>(bodyText)
            } catch (e: kotlinx.serialization.SerializationException) {
                lastException = Exception("Invalid response from server. Please try again.")
                if (attempt < MAX_RETRIES - 1) delay(RETRY_DELAY_MS)
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRIES - 1) {
                    delay(RETRY_DELAY_MS)
                }
            }
        }

        throw lastException ?: Exception("Failed to classify image after $MAX_RETRIES attempts")
    }
}
