package com.mitch.fontpicker.ui.screens.camera.controlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.mitch.fontpicker.BuildConfig
import com.mitch.fontpicker.data.api.FontResult
import com.mitch.fontpicker.data.api.WhatFontIsApiRepository
import com.mitch.fontpicker.di.DependenciesProvider
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.Base64


class FontRecognitionApiController(
    private val dependenciesProvider: DependenciesProvider,
    private val context: Context
) {

    private val fontRepository by lazy {
        WhatFontIsApiRepository(
            httpClient = dependenciesProvider.httpClient,
            apiKey = BuildConfig.WHAT_FONT_IS_API_KEY
        )
    }

    suspend fun processImage(imageUri: Uri): Result<List<FontResult>> {
        return try {
            Timber.d("Starting image processing for URI: $imageUri")

            // Convert image to Base64
            val base64Image = withContext(Dispatchers.IO) {
                imageUri.toBase64()
            } ?: throw IllegalArgumentException("Failed to read image data from URI.")

            Timber.d("Base64 conversion successful. Length: ${base64Image.length} characters.")

            // Send image to the API
            Timber.d("Sending image data to WhatFontIs API.")
            val fontResults = fontRepository.identifyFont(base64Image)

            Timber.d("API response received: $fontResults")

            // Handle the case where no fonts are detected
            if (fontResults.isEmpty()) {
                Timber.d(
                    "No fonts detected in the API response. " +
                            "This could also indicate a whitelisted response " +
                            "(e.g., 'No chars found'). Returning empty list."
                )
                return Result.success(emptyList())
            }

            Timber.d("Font recognition successful. Fonts found: ${fontResults.size}")
            Result.success(fontResults)
        } catch (e: HttpRequestTimeoutException) {
            // Handle timeout error gracefully
            Timber.e("Request timed out. the database " +
                    "is either unreachable or took too long to respond.")
            Result.failure(Exception("Request timed out. Please try again."))
        } catch (e: Exception) {
            // Handle all other errors
            Timber.e(e, "Error during image processing.")
            Result.failure(e)
        }
    }

    private fun resizeImage(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val ratio = minOf(
                maxWidth.toFloat() / originalBitmap.width,
                maxHeight.toFloat() / originalBitmap.height
            )

            val width = (originalBitmap.width * ratio).toInt()
            val height = (originalBitmap.height * ratio).toInt()

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            Timber.e(e, "Failed to resize image.")
            null
        }
    }

    private suspend fun Uri.toBase64(
        maxWidth: Int = 1200,
        maxHeight: Int = 1200): String? = withContext(Dispatchers.IO)
    {
        try {
            Timber.d("Starting image resize and Base64 conversion for URI: $this")
            val resizedImage = resizeImage(context, this@toBase64, maxWidth, maxHeight)
                ?: throw IllegalArgumentException("Failed to resize image.")

            val base64 = Base64.getEncoder().encodeToString(resizedImage)
            Timber.d("Base64 conversion completed successfully. " +
                    "Length: ${base64.length} characters.")
            base64
        } catch (e: Exception) {
            Timber.e(e, "Failed to resize and convert image to Base64.")
            null
        }
    }
}
