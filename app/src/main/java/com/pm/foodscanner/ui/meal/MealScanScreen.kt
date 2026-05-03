package com.pm.foodscanner.ui.meal

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pm.foodscanner.ui.common.CameraPermissionWrapper
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import kotlin.math.min

private const val CROP_RATIO = 0.75f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScanScreen(
    viewModel: MealViewModel,
    onBack: () -> Unit,
    onResult: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.analysis, uiState.error) {
        if (uiState.analysis != null || uiState.error != null) {
            onResult()
        }
    }

    CameraPermissionWrapper {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Meal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isCapturing) {
                val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

                DisposableEffect(Unit) {
                    onDispose { cameraExecutor.shutdown() }
                }

                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    ctx as androidx.lifecycle.LifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture
                                )
                            } catch (e: Exception) {
                                Log.e("MealScan", "Camera binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                CropOverlay()

                Text(
                    text = "Place your meal inside the frame",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                FloatingActionButton(
                    onClick = {
                        imageCapture?.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val rawBitmap = image.toBitmap()
                                    val rotationDegrees = image.imageInfo.rotationDegrees

                                    val oriented = if (rotationDegrees != 0) {
                                        val matrix = Matrix()
                                        matrix.postRotate(rotationDegrees.toFloat())
                                        val rotated = Bitmap.createBitmap(
                                            rawBitmap, 0, 0,
                                            rawBitmap.width, rawBitmap.height,
                                            matrix, true
                                        )
                                        rawBitmap.recycle()
                                        rotated
                                    } else {
                                        rawBitmap
                                    }

                                    val cropped = cropCenter(oriented)
                                    if (cropped !== oriented) oriented.recycle()

                                    val stream = ByteArrayOutputStream()
                                    cropped.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                                    val bytes = stream.toByteArray()
                                    cropped.recycle()
                                    image.close()
                                    viewModel.analyzeImage(bytes)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("MealScan", "Capture failed", exception)
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                        .size(72.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (uiState.isAnalyzing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Text(
                        text = "Analyzing your meal...",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 80.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun CropOverlay() {
    val borderColor = MaterialTheme.colorScheme.primary
    val cornerRadiusDp = 20.dp
    val cornerRadiusPx = with(LocalDensity.current) { cornerRadiusDp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        val cropSize = min(canvasW, canvasH) * CROP_RATIO
        val left = (canvasW - cropSize) / 2f
        val top = (canvasH - cropSize) / 2f

        val cutoutPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(left, top, left + cropSize, top + cropSize),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            )
        }

        clipPath(cutoutPath, clipOp = ClipOp.Difference) {
            drawRect(Color.Black.copy(alpha = 0.55f))
        }

        drawRoundRect(
            color = borderColor,
            topLeft = Offset(left, top),
            size = Size(cropSize, cropSize),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

private fun cropCenter(bitmap: Bitmap): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    val cropSize = (min(w, h) * CROP_RATIO).toInt()
    val left = (w - cropSize) / 2
    val top = (h - cropSize) / 2
    return Bitmap.createBitmap(bitmap, left, top, cropSize, cropSize)
}
