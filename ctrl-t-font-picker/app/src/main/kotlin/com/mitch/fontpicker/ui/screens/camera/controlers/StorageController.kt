package com.mitch.fontpicker.ui.screens.camera.controlers

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.mitch.fontpicker.di.DependenciesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

const val PICTURES_DIR: String = "PICTURES_DIR"
const val THUMBNAILS_DIR: String = "THUMBNAILS_DIR"

/**
 * Handles creating photo files, copying gallery images,
 * and clearing the pictures directory.
 */
class StorageController(
    private val dependenciesProvider: DependenciesProvider
) {

    /**
     * Creates a new unique file in the pictures directory.
     */
    suspend fun createPhotoFile(): File = withContext(Dispatchers.IO) {
        val picturesDir = dependenciesProvider.picturesDir
        if (!picturesDir.exists()) {
            throw IllegalStateException("Pictures directory does not exist. Ensure it's created on app start.")
        }
        val fileName = "fp_${System.currentTimeMillis()}.jpg"
        File(picturesDir, fileName)
    }

    /**
     * Copies an image from the given [sourceUri] to our pictures directory,
     * returning a [Uri] for the newly created file.
     */
    suspend fun copyImageToPicturesDir(context: Context, sourceUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            val picturesDir = dependenciesProvider.picturesDir
            val fileName = "gi_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(picturesDir, fileName)

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
                Timber.e(e, "Error copying image to the app's temporary directory.")
                null
            }
        }
    }

    /**
     * Clears the pictures directory (deletes all files).
     */
    suspend fun clearDirectory(dirIndicator: String) {
        withContext(Dispatchers.IO) {
            val dir = when (dirIndicator) {
                PICTURES_DIR -> dependenciesProvider.picturesDir
                THUMBNAILS_DIR -> dependenciesProvider.thumbnailsDir
                else -> {
                    Timber.e("Invalid directory indicator: $dirIndicator")
                    throw IllegalArgumentException("Directory indicator isn't allowed " +
                            "to be cleared: $dirIndicator. Only public constants defined " +
                            "in StorageController are accepted.")
                }
            }

            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles()
                Timber.d("Clearing directory: ${dir.absolutePath}")
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
            } else {
                Timber.e("The directory does not exist or is not a directory: ${dir.absolutePath}")
            }
        }
    }

}
