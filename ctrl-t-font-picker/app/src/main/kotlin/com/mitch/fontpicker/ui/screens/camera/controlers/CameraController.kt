package com.mitch.fontpicker.ui.screens.camera.controlers

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Handles CameraX logic: binding camera preview, flipping lens, capturing photos, etc.
 */
@OptIn(ExperimentalZeroShutterLag::class)
class CameraController {

    var lensFacing: Int = CameraSelector.LENS_FACING_BACK
        private set

    private var imageCapture: ImageCapture? = null
    var preview: Preview? = null
        private set

    /**
     * Initializes or rebinds the camera with the current lensFacing.
     */
    suspend fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner
    ): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = withContext(Dispatchers.IO) {
                cameraProviderFuture.get()
            }
            cameraProvider.unbindAll()

            // Setup preview use case
            val previewUseCase = Preview.Builder().build().also {
                preview = it
            }

            // Setup image capture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
                .build()

            // Select the lens
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            // Bind them
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                imageCapture
            )

            Timber.d("Camera initialized with lensFacing: $lensFacing")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load and bind camera provider.")
            Result.failure(e)
        }
    }

    /**
     * Flips the camera lens (back <-> front), re-initializes the camera.
     */
    suspend fun flipCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner
    ): Result<Unit> {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        Timber.d("Flipping camera, new lensFacing: $lensFacing")
        return initializeCamera(context, lifecycleOwner)
    }

    /**
     * Captures a photo to the [outputFile].
     */
    suspend fun capturePhoto(
        context: Context,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.Main) {
        val captureUseCase = imageCapture ?: return@withContext Result.failure(
            IllegalStateException("ImageCapture is not initialized.")
        )

        try {
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            val deferredResult = CompletableDeferred<File>()

            captureUseCase.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Timber.d("Photo captured successfully: ${outputFile.absolutePath}")
                        deferredResult.complete(outputFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Timber.e(exception, "Photo capture failed.")
                        deferredResult.completeExceptionally(exception)
                    }
                }
            )

            val file = deferredResult.await()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
