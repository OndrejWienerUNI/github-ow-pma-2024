package com.mitch.fontpicker.data.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import androidx.core.net.toFile
import com.mitch.fontpicker.di.DependenciesProvider
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

private const val MAX_DATABASE_BLOB_SIZE = 1 * 1024 * 1024 // 1 MB

class BitmapToolkit(
    private val dependenciesProvider: DependenciesProvider
) {
    // Instance-specific methods
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun download(url: String, destination: File): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val tempFile = File(destination, url.substringAfterLast("/"))
                val responseChannel = dependenciesProvider.httpClient.get(url).bodyAsChannel()

                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024) // 8 KB buffer
                    while (!responseChannel.isClosedForRead) {
                        val bytesRead = responseChannel.readAvailable(buffer)
                        if (bytesRead > 0) {
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }

                if (tempFile.exists()) {
                    Timber.d("Downloaded file: ${tempFile.absolutePath}")
                    Uri.fromFile(tempFile)
                } else {
                    Timber.e("File not found after download: $url")
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to download file: $url")
                null
            }
        }
    }

    suspend fun downloadAndProcess(
        url: String,
        tempDirectory: File,
        makeJpegCompatible: Boolean = false,
        backgroundColor: Int = Color.WHITE,
        targetWidth: Int? = null,
        targetHeight: Int? = null
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            // Perform disk-related operations in the IO thread pool
            try {
                if (!tempDirectory.exists() || !tempDirectory.isDirectory) {
                    throw IllegalStateException("Temp directory is invalid: ${tempDirectory.absolutePath}")
                }

                val downloadedFile = download(url, tempDirectory)?.toFile() ?: return@withContext null

                try {
                    val originalBitmap = BitmapFactory.decodeFile(downloadedFile.absolutePath)
                        ?: throw IllegalArgumentException("Failed to decode downloaded image.")

                    val resizedBitmap = if (targetWidth != null || targetHeight != null) {
                        resize(originalBitmap, targetWidth, targetHeight)
                    } else {
                        originalBitmap
                    }

                    if (makeJpegCompatible) {
                        makeJpegCompatible(resizedBitmap, backgroundColor)
                    } else {
                        resizedBitmap
                    }
                } finally {
                    deleteTempFile(downloadedFile)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to process image: $url")
                null
            }
        }
    }


    // Companion object for un-instantiated access
    companion object {
        fun encodeBinary(bitmap: Bitmap): ByteArray {
            return try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val binaryData = outputStream.toByteArray()

                if (binaryData.size > MAX_DATABASE_BLOB_SIZE) {
                    Timber.w("Encoded data exceeds size limit: ${binaryData.size} bytes.")
                }
                binaryData
            } catch (e: Exception) {
                Timber.e(e, "Failed to encode bitmap.")
                throw e
            }
        }

        fun decodeBinary(binaryData: ByteArray): Bitmap {
            return try {
                val bitmap = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.size)
                    ?: throw IllegalArgumentException("Failed to decode binary data into a bitmap.")
                bitmap
            } catch (e: Exception) {
                Timber.e(e, "Failed to decode binary data.")
                throw e
            }
        }

        /**
         * Return a bitmap based on a file path.
         *
         * @return A new Bitmap that has been processed.
         */
        fun getBitmapFromPath(path: String): Bitmap {
            val bitmap = BitmapFactory.decodeFile(path)
                ?: throw Exception("Failed to decode captured image.")

            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            val matrix = Matrix().apply {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                }
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        /**
         * Processes the given bitmap to enhance brightness and contrast, making blacks deeper
         * and whites brighter.
         *
         * @return A new Bitmap that has been processed.
         */
        fun makeHighContrastBw(bitmap: Bitmap): Bitmap? {
            val width = bitmap.width
            val height = bitmap.height
            val processedBitmap = bitmap.config?.let { Bitmap.createBitmap(width, height, it) }

            val canvas = processedBitmap?.let { Canvas(it) }
            val paint = Paint()

            val grayscaleMatrix = ColorMatrix().apply {
                setSaturation(0f)
            }

            val contrast = 1.4f // Adjust this for stronger contrast (1.0 = no change)
            val brightness = -20f // Adjust this for brightness shift (0 = no change)
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,   // Red
                    0f, contrast, 0f, 0f, brightness,   // Green
                    0f, 0f, contrast, 0f, brightness,   // Blue
                    0f, 0f, 0f, 1f, 0f                  // Alpha
                )
            )

            grayscaleMatrix.postConcat(contrastMatrix)
            paint.colorFilter = ColorMatrixColorFilter(grayscaleMatrix)

            canvas?.drawBitmap(bitmap, 0f, 0f, paint)

            return processedBitmap
        }

        /**
         * Processes the given bitmap to ensure it's JPEG-compatible by removing transparency
         * and applying a white background if necessary.
         *
         * @param bitmap The original bitmap.
         * @param backgroundColor The background color to apply if the bitmap has transparency.
         *
         * @return A Bitmap object that is JPEG-compatible.
         *
         * @throws IllegalArgumentException If the background color has transparency.
         */
        fun makeJpegCompatible(bitmap: Bitmap, backgroundColor: Int = Color.WHITE): Bitmap {

            require(Color.alpha(backgroundColor) == 255) {
                "Transparent background colors are not allowed for JPEG compatibility."
            }

            val jpegCompatibleBitmap = if (bitmap.hasAlpha()) {
                val bitmapNoTransparency = Bitmap.createBitmap(
                    bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmapNoTransparency)
                canvas.drawColor(backgroundColor)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                Timber.d("Made bitmap JPEG-compatible by adding a background color.")
                bitmapNoTransparency
            } else {
                Timber.d("Bitmap is already JPEG-compatible.")
                bitmap
            }

            return jpegCompatibleBitmap
        }

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

        fun invertImage(bitmap: Bitmap): Bitmap {
            return try {
                val colorMatrix = ColorMatrix().apply {
                    set(
                        floatArrayOf(
                            -1f, 0f, 0f, 0f, 255f,
                            0f, -1f, 0f, 0f, 255f,
                            0f, 0f, -1f, 0f, 255f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                }

                val paint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(colorMatrix)
                }

                val invertedBitmap = Bitmap.createBitmap(
                    bitmap.width,
                    bitmap.height,
                    bitmap.config ?: Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(invertedBitmap)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
                invertedBitmap
            } catch (e: Exception) {
                Timber.e(e, "Failed to invert image - returning original bitmap.")
                bitmap
            }
        }

        fun cleanUpBitmap(bitmap: Bitmap?) {
            try {
                if (bitmap != null && !bitmap.isRecycled) {
                    bitmap.recycle()
                    Timber.d("Bitmap successfully recycled.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error recycling Bitmap.")
            }
        }

        private fun deleteTempFile(file: File) {
            if (file.exists()) {
                if (file.delete()) {
                    Timber.d("Temporary file deleted: ${file.absolutePath}")
                } else {
                    Timber.e("Failed to delete temporary file: ${file.absolutePath}")
                }
            }
        }
    }
}
