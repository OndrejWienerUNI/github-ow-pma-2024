package com.mitch.fontpicker.data.encode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

object ImageBinaryEncoder {

    private const val MAX_DATABASE_BLOB_SIZE = 1 * 1024 * 1024 // 1 MB

    /**
     * Encodes an image file into a binary format.
     *
     * @param imageFile The image file to encode (PNG, JPG, BMP, etc.).
     * @return A byte array representing the encoded image.
     * @throws IllegalArgumentException if the file cannot be read or is not an image.
     */
    fun encode(imageFile: File): ByteArray {
        return try {
            // Validate file extension (optional, but adds clarity)
            val supportedExtensions = listOf("png", "jpg", "jpeg", "bmp", "webp")
            val fileExtension = imageFile.extension.lowercase()

            if (fileExtension.lowercase() !in supportedExtensions) {
                Timber.w("Unsupported file extension: $fileExtension. Attempting to decode anyway.")
            }

            // Decode the file into a Bitmap
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                ?: throw IllegalArgumentException("Failed to decode file: ${imageFile.absolutePath}")

            // Compress the Bitmap into a byte array
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val binaryData = outputStream.toByteArray()

            // Log a warning if the binary data size exceeds the database limit
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
     * @throws IllegalArgumentException if the binary data cannot be decoded into an image.
     */
    fun decode(binaryData: ByteArray): Bitmap {
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
}
