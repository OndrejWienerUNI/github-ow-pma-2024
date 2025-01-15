package com.mitch.fontpicker.data.room.repository

import com.mitch.fontpicker.data.room.FontsDatabase
import com.mitch.fontpicker.data.room.model.BitmapData
import com.mitch.fontpicker.data.room.model.Category
import com.mitch.fontpicker.data.room.model.Font
import com.mitch.fontpicker.data.room.model.ImageUrl
import com.mitch.fontpicker.data.room.util.ComparisonUtils
import com.mitch.fontpicker.data.room.util.RecyclingAndRestorationUtils
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate")
class FontsDatabaseRepository(private val database: FontsDatabase) {

    companion object {
        const val CATEGORY_FAVORITES_NAME = "Favorites"
        const val CATEGORY_RECYCLE_BIN_NAME = "Recycle Bin"
    }

    private val favoritesCategoryId: Int by lazy {
        runBlocking { getOrCreateCategoryByName(CATEGORY_FAVORITES_NAME) }
    }

    private val recycleBinCategoryId: Int by lazy {
        runBlocking { getOrCreateCategoryByName(CATEGORY_RECYCLE_BIN_NAME) }
    }

    val recyclingAndRestorationUtils: RecyclingAndRestorationUtils
            = RecyclingAndRestorationUtils(database, favoritesCategoryId, recycleBinCategoryId)

    fun fontBeforeInsertion(title: String, url: String): Font {
        return Font(title = title, url = url, categoryId = favoritesCategoryId)
    }

    suspend fun shouldStartAsLiked(newFont: Font): Boolean {
        val existingFont = database.fontDao().findFontByTitleIgnoreCase(newFont.title)
        if (existingFont != null) {
            Timber.d("Comparing new font '${newFont.title}' (URL: ${newFont.url}) " +
                    "with existing font '${existingFont.title}' (URL: ${existingFont.url})")

            val comparisonResult = ComparisonUtils.compareFonts(
                newFont, existingFont, recycleBinCategoryId
            )

            Timber.d("Comparison result for '${newFont.title}' " +
                    "vs '${existingFont.title}': $comparisonResult")

            return when (comparisonResult) {
                ComparisonUtils.FontComparisonResult.SAME_TITLE_DIFFERENT_URL -> {
                    Timber.d("New font '${newFont.title}' starts as liked: " +
                            "Different URL but same title.")
                    true
                }
                ComparisonUtils.FontComparisonResult.DIFFERENT_TITLE_SAME_URL -> {
                    Timber.d("New font '${newFont.title}' starts as liked: " +
                            "Different title but same URL.")
                    true
                }
                ComparisonUtils.FontComparisonResult.COMPLETELY_IDENTICAL -> {
                    if (existingFont.categoryId == favoritesCategoryId) {
                        Timber.d("New font '${newFont.title}' " +
                                "is identical to an existing favorite. " +
                                "Marking existing font '${existingFont.title}' for recycling.")
                        recyclingAndRestorationUtils.markForRecycling(existingFont)
                        true
                    } else {
                        Timber.d("New font '${newFont.title}' is identical " +
                                "to an existing non-favorite. Starts unliked.")
                        false
                    }
                }
                else -> {
                    Timber.d("New font '${newFont.title}' does not match " +
                            "any significant condition. Starts unliked.")
                    false
                }
            }
        } else {
            Timber.d("No existing font found with title '${newFont.title}'. Starts unliked.")
        }
        return false
    }

    // Helper to handle the rule during insertion
    suspend fun handleIdenticalInRecycleBin(newFont: Font) {
        val existingFont = database.fontDao().findFontByTitleIgnoreCase(newFont.title)
        if (existingFont != null) {
            Timber.d("Checking if new font '${newFont.title}' (URL: ${newFont.url}) " +
                    "is identical to font in Recycle Bin '${existingFont.title}' (URL: ${existingFont.url})")

            val comparisonResult = ComparisonUtils.compareFonts(
                newFont, existingFont, recycleBinCategoryId
            )

            Timber.d("Comparison result for '${newFont.title}' " +
                    "vs '${existingFont.title}': $comparisonResult")

            if (comparisonResult == ComparisonUtils.FontComparisonResult.COMPLETELY_IDENTICAL) {
                if (existingFont.categoryId == recycleBinCategoryId) {
                    Timber.d("New font '${newFont.title}' is liked and identical " +
                            "to font in Recycle Bin. " +
                            "Marking font '${existingFont.title}' for restoration to Favorites.")
                    recyclingAndRestorationUtils.markForRestoration(existingFont)
                } else {
                    Timber.d("New font '${newFont.title}' is identical but not " +
                            "in Recycle Bin. No action needed.")
                }
            } else {
                Timber.d("New font '${newFont.title}' is not completely identical " +
                        "to any font in Recycle Bin. No action needed.")
            }
        } else {
            Timber.d("No existing font in Recycle Bin matches title '${newFont.title}'. " +
                    "No action needed.")
        }
    }

    suspend fun insertFontWithAssets(
        font: Font,
        imageUrls: List<ImageUrl>,
        bitmapData: List<BitmapData>
    ) {
        Timber.d("Attempting to insert Font: $font")

        val existingFont = database.fontDao().findFontByUrl(font.url)
        if (existingFont != null) {
            Timber.d("Replacing existing font with URL: ${font.url}. " +
                    "Old title: '${existingFont.title}', New title: '${font.title}'")
            database.fontDao().deleteFontById(existingFont.id)
        }

        val updatedFont = font.copy(categoryId = favoritesCategoryId)

        Timber.d("Inserting Font: $updatedFont with ${imageUrls.size} " +
                "Image URLs and ${bitmapData.size} BitmapData entries")
        require(imageUrls.size == bitmapData.size) {
            "Number of image URLs must match the number of bitmap data entries."
        }

        val fontId = database.fontDao().insert(updatedFont).toInt()
        val updatedImageUrls = imageUrls.map { it.copy(fontId = fontId) }
        val updatedBitmapData = bitmapData.map { it.copy(fontId = fontId) }

        database.imageUrlDao().insertAll(updatedImageUrls)
        database.bitmapDataDao().insertAll(updatedBitmapData)
        Timber.d("${updatedImageUrls.size} Image URLs and ${updatedBitmapData.size} " +
                "BitmapData entries inserted for Font ID: $fontId")
    }

    suspend fun attemptRecycling() {
        recyclingAndRestorationUtils.attemptRecycling()
    }

    fun dismissRecycling() {
        recyclingAndRestorationUtils.dismissRecycling()
    }

    suspend fun attemptRestoration() {
        recyclingAndRestorationUtils.attemptRestoration()
    }

    fun dismissRestoration() {
        recyclingAndRestorationUtils.dismissRestoration()
    }

    suspend fun getOrCreateCategoryByName(name: String): Int {
        val existingCategory = database.categoryDao().getCategoryByNameIgnoreCase(name)
        return existingCategory?.id ?: run {
            val newCategory = Category(name = name)
            database.categoryDao().insert(newCategory).toInt()
        }
    }

    private suspend fun moveToFavorites(font: Font) {
        recyclingAndRestorationUtils.moveToFavorites(font)
    }

    private suspend fun moveToRecycleBin(font: Font) {
        recyclingAndRestorationUtils.moveToRecycleBin(font)
    }

    suspend fun deleteRecycledFont(font: Font) {
        recyclingAndRestorationUtils.deleteRecycledFont(font)
    }

    suspend fun wipeRecycleBin() {
        recyclingAndRestorationUtils.wipeRecycleBin()
    }

    suspend fun deleteOldRecycledFonts() {
        recyclingAndRestorationUtils.deleteOldRecycledFonts()
    }
}
