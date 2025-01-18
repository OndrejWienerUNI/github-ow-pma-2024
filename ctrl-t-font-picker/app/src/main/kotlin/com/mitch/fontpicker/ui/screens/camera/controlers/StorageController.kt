package com.mitch.fontpicker.ui.screens.camera.controlers

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.mitch.fontpicker.data.images.BitmapToolkit
import com.mitch.fontpicker.di.DependenciesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Handles file operations such as creating files, copying images, and clearing directories.
 */
class StorageController(
    private val dependenciesProvider: DependenciesProvider
) {
    val picturesDir: File
        get() = dependenciesProvider.picturesDir

    val thumbnailsDir: File
        get() = dependenciesProvider.thumbnailsDir

    private fun checkDirectory(directory: File) {
        val validDirectories = listOf(dependenciesProvider.picturesDir, dependenciesProvider.thumbnailsDir)

        if (directory !in validDirectories) {
            Timber.e("Invalid directory provided: ${directory.absolutePath}")
            throw IllegalArgumentException("Unsupported directory: ${directory.absolutePath}. " +
                    "Only the application's pictures or thumbnails directories are allowed.")
        }

        if (!directory.exists() || !directory.isDirectory) {
            Timber.e("Directory does not exist or is not valid: ${directory.absolutePath}")
            throw IllegalStateException("Directory is missing or invalid: ${directory.absolutePath}. " +
                    "Ensure it is created during app initialization.")
        }
    }

    /**
     * Creates a new unique file in the specified directory.
     *
     * @param directory The directory, e.g. [picturesDir] or [thumbnailsDir].
     * @return The created [File].
     */
    suspend fun newFile(directory: File): File = withContext(Dispatchers.IO) {
        checkDirectory(directory)
        val prefix: String = when (directory) {
            picturesDir -> "pd"
            thumbnailsDir -> "td"
            else -> {
                "uncategorized"
            }
        }
        val fileName = "${prefix}_n_${System.currentTimeMillis()}.jpg"
        return@withContext File(directory, fileName)
    }

    /**
     * Copies an image from the given [sourceUri] to the specified directory,
     * returning a [Uri] for the newly created file.
     *
     * @param context The application context.
     * @param sourceUri The source URI of the image to copy.
     * @param directory The directory, e.g. [picturesDir] or [thumbnailsDir].
     * @return The [Uri] of the copied image, or null if the operation fails.
     */
    suspend fun copyImageToDirectory(context: Context, sourceUri: Uri, directory: File): Uri? {
        return withContext(Dispatchers.IO) {
            checkDirectory(directory)
            val prefix: String = when (directory) {
                picturesDir -> "pd"
                thumbnailsDir -> "td"
                else -> "uncategorized"
            }
            val fileName = "${prefix}_c_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(directory, fileName)

            try {
                // Resolve the file path from the Uri (content:// or file://)
                val sourcePath = sourceUri.toFilePath(context)
                    ?: throw IllegalArgumentException("Unable to resolve file path from Uri: $sourceUri")

                // Copy the resolved file to the destination
                File(sourcePath).inputStream().use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Timber.d("Image copied to: ${destinationFile.absolutePath}")
                val fileUri = Uri.fromFile(destinationFile)

                // return file uri
                fileUri
            } catch (e: Exception) {
                Timber.e(e, "Error copying image to the directory.")

                // return a null
                null
            }
        }
    }

    private fun Uri.toFilePath(context: Context): String? {
        return when (scheme) {
            "content" -> {
                val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
                context.contentResolver.query(
                    this, projection, null, null, null
                )?.use { cursor ->
                    val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                    if (cursor.moveToFirst()) cursor.getString(columnIndex) else null
                }
            }
            "file" -> path
            else -> null
        }
    }


    /**
     * Clears all files in the specified directory.
     *
     * @param directory The directory, e.g. [picturesDir] or [thumbnailsDir].
     */
    suspend fun clearDirectory(directory: File) {
        withContext(Dispatchers.IO) {
            checkDirectory(directory)
            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()
                Timber.d("Clearing directory: ${directory.absolutePath}")
                files?.forEach { file ->
                    try {
                        if (file.exists() && file.delete()) {
                            Timber.d("Deleted file: ${file.absolutePath}")
                        } else {
                            Timber.e("Failed to delete file: ${file.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error deleting file: ${file.absolutePath}")
                    }
                }
            }
        }
    }

    /**
     * Replaces a JPEG file's content with a given bitmap.
     * Ensures the file is a JPEG before processing.
     *
     * @param jpegFile The JPEG file to be overwritten.
     * @param bitmap The bitmap to replace the contents of the JPEG file.
     *
     * @return The newly replaced JPEG file.
     *
     * @throws IllegalArgumentException If the provided file is not a JPEG.
     * @throws IOException If an error occurs during file writing.
     */
    fun replaceJpegFileWithBitmap(jpegFile: File, bitmap: Bitmap): File {
        val validJpegExtensions = listOf("jpg", "jpeg")

        val fileExtension = jpegFile.extension.lowercase()

        require(fileExtension in validJpegExtensions) {
            "Invalid file type: ${jpegFile.name}. Only JPEG files are supported."
        }

        val jpegCompatibleBitmap = BitmapToolkit.makeJpegCompatible(bitmap)

        FileOutputStream(jpegFile).use { outputStream ->
            val compressed = jpegCompatibleBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            if (!compressed) {
                throw IOException("Failed to compress bitmap into JPEG format.")
            }
        }

        return jpegFile
    }
}
