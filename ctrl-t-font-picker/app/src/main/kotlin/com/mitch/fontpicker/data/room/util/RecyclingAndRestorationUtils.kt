package com.mitch.fontpicker.data.room.util

import com.mitch.fontpicker.data.room.FontsDatabase
import com.mitch.fontpicker.data.room.model.Font
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant

const val DELETION_THRESHOLD_SECONDS = 7L * 24 * 60 * 60

class RecyclingAndRestorationUtils(
    private val database: FontsDatabase,
    private val favoritesCategoryId: Int,
    private val recycleBinCategoryId: Int
) {
    private val markedForRecycling = mutableSetOf<Font>()
    private val markedForRestoration = mutableSetOf<Font>()

    suspend fun wipeRecycleBin() {
        val recycleBinFonts = database
            .fontDao().getFontsByCategory(recycleBinCategoryId).firstOrNull() ?: emptyList()
        database.fontDao().deleteAllByIds(recycleBinFonts.map { it.id })
        markedForRestoration.clear()
        Timber.d("Deleted all fonts from the Recycle Bin.")
    }

    suspend fun deleteRecycledFont(fontId: Int) {
        val font: Font? = database.fontDao().getFontById(fontId)
        if (font != null) {
            if (font.categoryId == recycleBinCategoryId) {
                database.fontDao().deleteFontById(fontId)
                Timber.d("Deleted font with ID $fontId from Recycle Bin.")
            } else {
                Timber.d("Font with ID $fontId is not in the Recycle Bin. Did not delete.")
            }
        } else {
            Timber.d("No font found based on id '$fontId' in permanent deletion attempt.")
        }
        markedForRestoration.remove(font)
    }

    suspend fun moveToRecycleBin(fontId: Int) {
        Timber.i("Attempting to move font to Recycle Bin: fonID=$fontId")
        val font: Font? = database.fontDao().getFontById(fontId)
        val updatedFont = font
            ?.copy(categoryId = recycleBinCategoryId)

        if (updatedFont != null) {
            Timber.i("Updated font for Recycle Bin: $updatedFont")
            database.fontDao().updateFont(updatedFont)
            scheduleDeletion(listOf(updatedFont), DELETION_THRESHOLD_SECONDS)
            markedForRecycling.remove(font)
            Timber.i("Font '${font.title}' successfully moved to Recycle Bin.")
        } else {
            Timber.i("Failed to find font with ID: $fontId in the database.")
        }
    }

    suspend fun moveToFavorites(fontId: Int) {
        Timber.i("Attempting to move font to Favorites: fonID=$fontId")
        val font: Font? = database.fontDao().getFontById(fontId)
        val updatedFont = font?.copy(categoryId = favoritesCategoryId)

        if (updatedFont != null) {
            Timber.i("Updated font for Favorites: $updatedFont")
            database.fontDao().updateFont(updatedFont)
            clearDeletionTimestamp(font)
            markedForRestoration.remove(font)
            Timber.i("Font '${font.title}' successfully moved to Favorites.")
        } else {
            Timber.i("Failed to find font with ID: $fontId in the database.")
        }
    }


    fun markForRecycling(font: Font) {
        markedForRecycling.add(font)
        Timber.d("Marked font '${font.title}' with ID ${font.id} for recycling.")
    }

    fun dismissRecycling() {
        markedForRecycling.clear()
        Timber.d("Dismissed all marked fonts for recycling.")
    }

    fun markForRestoration(font: Font) {
        markedForRestoration.add(font)
        Timber.d("Marked font '${font.title}' with ID ${font.id} " +
                "for restoration to Favorites.")
    }

    fun dismissRestoration() {
        markedForRestoration.clear()
        Timber.d("Dismissed all marked fonts for restoration.")
    }

    suspend fun attemptRecycling() {
        val fontsToRecycle = markedForRecycling.toList()
        fontsToRecycle.forEach { font ->
            if (font.categoryId == favoritesCategoryId) {
                moveToRecycleBin(font.id)
                Timber.d("Moved font '${font.title}' to the Recycle Bin.")
            }
        }
        markedForRecycling.clear()
    }

    suspend fun attemptRestoration() {
        val fontsToRestore = markedForRestoration.toList()
        fontsToRestore.forEach { font ->
            if (font.categoryId == recycleBinCategoryId) {
                moveToFavorites(font.id)
                Timber.d("Restored font '${font.title}' to Favorites.")
            }
        }
        markedForRestoration.clear()
    }

    private suspend fun scheduleDeletion(
        fontsEnteringRecycleBin: Collection<Font>,
        deletionThresholdSeconds: Long
    ) = withContext(Dispatchers.IO) {
        val currentTime = Instant.now().epochSecond
        fontsEnteringRecycleBin.forEach { font ->
            val updatedFont = database.fontDao().getFontById(font.id)?.copy(
                deletionTimestamp = currentTime + deletionThresholdSeconds
            )
            if (updatedFont != null) {
                database.fontDao().updateFont(updatedFont)
                Timber.i("Scheduled deletion for font: $updatedFont")
            } else {
                Timber.w("Font with ID ${font.id} not found in database. Skipping deletion scheduling.")
            }
        }
    }

    suspend fun deleteOldRecycledFonts() {
        val currentTime = Instant.now().epochSecond
        val recycleBinFonts = database.fontDao().getFontsByCategory(recycleBinCategoryId).firstOrNull() ?: emptyList()
        val fontsToDelete = recycleBinFonts.filter { font ->
            font.deletionTimestamp != null && font.deletionTimestamp <= currentTime
        }
        if (fontsToDelete.isNotEmpty()) {
            try {
                database.fontDao().deleteAllByIds(fontsToDelete.map { it.id })
                Timber.d("Deleted ${fontsToDelete.size} expired fonts from the Recycle Bin: ${fontsToDelete.map { it.title }}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete expired fonts from the Recycle Bin.")
            }
        } else {
            Timber.d("No fonts were expired for deletion in the Recycle Bin.")
        }
    }

    private suspend fun clearDeletionTimestamp(font: Font) = withContext(Dispatchers.IO) {
        val fetchedFont = database.fontDao().getFontById(font.id)
        if (fetchedFont != null) {
            val updatedFont = fetchedFont.copy(deletionTimestamp = null)
            database.fontDao().updateFont(updatedFont)
            Timber.d("Cleared deletion timestamp for font: $updatedFont")
        } else {
            Timber.w("Font with ID ${font.id} not found. Cannot clear deletion timestamp.")
        }
    }
}
