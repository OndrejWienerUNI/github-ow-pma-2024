package com.mitch.fontpicker.ui.screens.camera.controlers

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.mitch.fontpicker.di.DependenciesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

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
                else -> {
                    "uncategorized"
                }
            }
            val fileName = "${prefix}_c_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(directory, fileName)

            try {
                context.contentResolver.openInputStream(sourceUri).use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                }
                Timber.d("Image copied to: ${destinationFile.absolutePath}")
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    destinationFile
                )
            } catch (e: Exception) {
                Timber.e(e, "Error copying image to the directory.")
                null
            }
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
}
