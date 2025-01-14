package com.mitch.fontpicker.data.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.jvm.javaio.copyTo
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

object ImageToolkit {

    private const val MAX_DATABASE_BLOB_SIZE = 1 * 1024 * 1024 // 1 MB

    /**
     * Encodes an image file into a binary format.
     *
     * @param imageFile The image file to encode (PNG, JPG, BMP, etc.).
     * @return A byte array representing the encoded image.
     * @throws IllegalArgumentException if the file cannot be read or is not an image.
     */
    fun encodeBinary(imageFile: File): ByteArray {
        return try {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                ?: throw IllegalArgumentException("Failed to decode file: ${imageFile.absolutePath}")

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val binaryData = outputStream.toByteArray()

            if (binaryData.size > MAX_DATABASE_BLOB_SIZE) {
                Timber.w("Encoded file exceeds database storage limit: ${binaryData.size} bytes.")
            }

            Timber.d("Encoding successful: File=${imageFile.name}, Size=${binaryData.size} bytes.")
            binaryData
        } catch (e: Exception) {
            Timber.e(e, "Encoding failed for file: ${imageFile.absolutePath}")
            throw e
        }
    }

    /**
     * Decodes a binary array back into a Bitmap.
     *
     * @param binaryData The byte array to decode.
     * @return A Bitmap representing the decoded image.
     */
    fun decodeBinary(binaryData: ByteArray): Bitmap {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.size)
                ?: throw IllegalArgumentException("Failed to decode binary data into an image.")

            Timber.d("Decoding successful: Binary size=${binaryData.size} bytes.")
            bitmap
        } catch (e: Exception) {
            Timber.e(e, "Decoding failed for binary data of size: ${binaryData.size} bytes.")
            throw e
        }
    }

    /**
     * Downloads an image from a URL to a temporary file.
     *
     * @param httpClient Ktor HttpClient instance.
     * @param url The image URL.
     * @return The downloaded file, or null if the download failed.
     */
    suspend fun download(httpClient: HttpClient, url: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val tempFile = File.createTempFile("image", null)
                val responseChannel = httpClient.get(url).bodyAsChannel()
                tempFile.outputStream().use { output ->
                    while (!responseChannel.isClosedForRead) {
                        val buffer = ByteArray(1024)
                        val bytesRead = responseChannel.readAvailable(buffer)
                        if (bytesRead > 0) {
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }
                Timber.d("Download successful: $url -> ${tempFile.absolutePath}")
                tempFile
            } catch (e: Exception) {
                Timber.e(e, "Failed to download image from URL: $url")
                null
            }
        }
    }

    /**
     * Converts a Bitmap to JPEG format with an optional background color.
     *
     * @param bitmap The input Bitmap.
     * @param backgroundColor The background color (default: white).
     * @return A JPEG Bitmap.
     */
    fun convertToJpg(bitmap: Bitmap, backgroundColor: Int = Color.WHITE): Bitmap {
        return if (bitmap.hasAlpha()) {
            val bitmapWithBg = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmapWithBg)
            canvas.drawColor(backgroundColor)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            Timber.d("Converted bitmap to JPEG with background color.")
            bitmapWithBg
        } else {
            bitmap
        }
    }

    /**
     * Resizes a Bitmap to the specified height or width while maintaining the aspect ratio.
     *
     * @param bitmap The input Bitmap.
     * @param targetWidth The target width (optional).
     * @param targetHeight The target height (optional).
     * @return A resized Bitmap.
     * @throws IllegalArgumentException if both targetWidth and targetHeight are provided.
     */
    fun resize(bitmap: Bitmap, targetWidth: Int? = null, targetHeight: Int? = null): Bitmap {
        if (targetWidth != null && targetHeight != null) {
            throw IllegalArgumentException("Both targetWidth and targetHeight cannot be provided simultaneously.")
        }

        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val width = targetWidth ?: (targetHeight!! * aspectRatio).toInt()
        val height = targetHeight ?: (targetWidth!! / aspectRatio).toInt()

        Timber.d("Resizing bitmap to width=$width, height=$height.")
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * Downloads and processes an image, optionally resizing and converting it.
     *
     * @param httpClient Ktor HttpClient instance.
     * @param url The image URL.
     * @param destination The file to save the processed image.
     * @param convertToJpg Whether to convert the image to JPEG (default: false).
     * @param backgroundColor The background color for JPEG conversion (default: white).
     * @param targetWidth The target width (optional, null by default).
     * @param targetHeight The target height (optional, null by default).
     * @return True if successful, false otherwise.
     */
    suspend fun downloadAndProcess(
        httpClient: HttpClient,
        url: String,
        destination: File,
        convertToJpg: Boolean = false,
        backgroundColor: Int = Color.WHITE,
        targetWidth: Int? = null,
        targetHeight: Int? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val downloadedFile = download(httpClient, url) ?: return@withContext false
                val originalBitmap = BitmapFactory.decodeFile(downloadedFile.absolutePath)
                    ?: throw IllegalArgumentException("Failed to decode downloaded image.")

                val resizedBitmap = if (targetWidth != null || targetHeight != null) {
                    resize(originalBitmap, targetWidth, targetHeight)
                } else {
                    originalBitmap
                }

                val finalBitmap = if (convertToJpg) {
                    convertToJpg(resizedBitmap, backgroundColor)
                } else {
                    resizedBitmap
                }

                destination.outputStream().use { output ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }

                Timber.d("Image successfully processed and saved: ${destination.absolutePath}")
                downloadedFile.delete()
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to process image: $url")
                false
            }
        }
    }
}
