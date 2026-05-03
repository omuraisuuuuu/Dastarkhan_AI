package com.pm.foodscanner.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.pm.foodscanner.data.model.FoodPrediction
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp

@Singleton
class LocalFoodClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var numClasses: Int = 0
    private var inputWidth: Int = IMAGE_SIZE
    private var inputHeight: Int = IMAGE_SIZE

    fun isAvailable(): Boolean {
        return try {
            context.assets.open(MODEL_FILE).close()
            true
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun classify(imageBytes: ByteArray): List<FoodPrediction> {
        ensureInitialized()

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw Exception("Failed to decode image")

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val inputBuffer = bitmapToByteBuffer(scaledBitmap)

        if (scaledBitmap !== bitmap) scaledBitmap.recycle()
        bitmap.recycle()

        val outputArray = Array(1) { FloatArray(numClasses) }
        interpreter!!.run(inputBuffer, outputArray)

        val probabilities = softmax(outputArray[0])

        return probabilities.mapIndexed { index, score ->
            val label = labels.getOrElse(index) { "food_$index" }
            FoodPrediction(label = label, score = score)
        }.sortedByDescending { it.score }.take(5)
    }

    private fun ensureInitialized() {
        if (interpreter != null) return

        val model = loadModelFile()

        interpreter = try {
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseXNNPACK(false)
            }
            Interpreter(model, options)
        } catch (_: Exception) {
            val fallbackOptions = Interpreter.Options().apply {
                setNumThreads(2)
            }
            Interpreter(model, fallbackOptions)
        }

        val inputTensor = interpreter!!.getInputTensor(0)
        val inputShape = inputTensor.shape()
        if (inputShape.size == 4) {
            inputHeight = inputShape[1]
            inputWidth = inputShape[2]
        }

        val outputShape = interpreter!!.getOutputTensor(0).shape()
        numClasses = outputShape.last()

        labels = try {
            context.assets.open(LABELS_FILE).bufferedReader().readLines()
                .filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        return fileDescriptor.use { fd ->
            FileInputStream(fd.fileDescriptor).use { inputStream ->
                inputStream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength
                )
            }
        }
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * inputWidth * inputHeight * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            buffer.putFloat((r - 0.485f) / 0.229f)
            buffer.putFloat((g - 0.456f) / 0.224f)
            buffer.putFloat((b - 0.406f) / 0.225f)
        }

        buffer.rewind()
        return buffer
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.max()
        val exps = FloatArray(logits.size) { exp((logits[it] - maxLogit).toDouble()).toFloat() }
        val sum = exps.sum()
        return FloatArray(exps.size) { exps[it] / sum }
    }

    companion object {
        const val MODEL_FILE = "food_model.tflite"
        private const val LABELS_FILE = "food_labels.txt"
        private const val IMAGE_SIZE = 224
    }
}
