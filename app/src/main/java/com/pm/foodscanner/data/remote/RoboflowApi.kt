package com.pm.foodscanner.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.pm.foodscanner.BuildConfig
import com.pm.foodscanner.data.model.FoodPrediction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream as BAOS
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class RoboflowApi @Inject constructor() {

    companion object {
        private const val TAG = "RoboflowApi"
        private const val MODEL_URL =
            "https://serverless.roboflow.com/kazakh-food/1"
        private const val CONNECT_TIMEOUT = 30_000
        private const val READ_TIMEOUT = 60_000
        private const val MAX_IMAGE_SIZE = 800
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun classifyImage(imageBytes: ByteArray): List<FoodPrediction> {
        Log.d(TAG, "=== Roboflow: classifyImage ===")

        val resizedBytes = resizeImage(imageBytes)
        Log.d(TAG, "Image: ${imageBytes.size} -> ${resizedBytes.size} bytes")

        val apiKey = BuildConfig.ROBOFLOW_API_KEY
        Log.d(TAG, "API key length: ${apiKey.length}, image bytes: ${resizedBytes.size}")

        val boundary = "----RoboflowBoundary${System.currentTimeMillis()}"
        val url = URL("$MODEL_URL?api_key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            connection.doOutput = true

            val body = buildMultipart(boundary, resizedBytes)
            Log.d(TAG, "Sending ${body.size} bytes to Roboflow")
            connection.outputStream.use { it.write(body) }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            if (responseCode !in 200..299) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: ""
                Log.e(TAG, "HTTP Error $responseCode: $error")
                throw Exception("Roboflow error (HTTP $responseCode): $error")
            }

            val responseText = connection.inputStream.bufferedReader().readText()
            Log.d(TAG, "Response length: ${responseText.length}")
            Log.d(TAG, "Response preview: ${responseText.take(300)}")
            return parseResponse(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseResponse(responseText: String): List<FoodPrediction> {
        val root = json.parseToJsonElement(responseText).jsonObject

        val predictions = mutableListOf<FoodPrediction>()

        val predsArray = root["predictions"]?.jsonArray
        if (predsArray != null) {
            for (pred in predsArray) {
                val obj = pred.jsonObject
                val cls = obj["class"]?.jsonPrimitive?.contentOrNull ?: continue
                val conf = obj["confidence"]?.jsonPrimitive?.float ?: 0f
                predictions.add(FoodPrediction(label = cls, score = conf))
            }
        }

        if (predictions.isEmpty()) {
            val top = root["top"]?.jsonPrimitive?.contentOrNull
            val conf = root["confidence"]?.jsonPrimitive?.float
            if (top != null && conf != null) {
                predictions.add(FoodPrediction(label = top, score = conf))
            }
        }

        Log.d(TAG, "Parsed predictions: ${predictions.map { "${it.label}=${it.score}" }}")

        if (predictions.isEmpty()) {
            throw Exception("Roboflow: no food detected")
        }

        return predictions.sortedByDescending { it.score }
    }

    private fun buildMultipart(boundary: String, imageBytes: ByteArray): ByteArray {
        val out = BAOS()
        val crlf = "\r\n"
        out.write("--$boundary$crlf".toByteArray())
        out.write("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"$crlf".toByteArray())
        out.write("Content-Type: image/jpeg$crlf$crlf".toByteArray())
        out.write(imageBytes)
        out.write("$crlf--$boundary--$crlf".toByteArray())
        return out.toByteArray()
    }

    private fun resizeImage(imageBytes: ByteArray): ByteArray {
        val original = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: return imageBytes

        val w = original.width
        val h = original.height

        if (w <= MAX_IMAGE_SIZE && h <= MAX_IMAGE_SIZE) {
            original.recycle()
            return imageBytes
        }

        val scale = MAX_IMAGE_SIZE.toFloat() / maxOf(w, h)
        val newW = (w * scale).toInt()
        val newH = (h * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(original, newW, newH, true)
        original.recycle()

        val stream = BAOS()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        scaled.recycle()

        return stream.toByteArray()
    }
}
