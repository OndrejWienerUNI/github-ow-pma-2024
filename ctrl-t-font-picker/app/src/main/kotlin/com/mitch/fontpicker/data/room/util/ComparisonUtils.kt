package com.mitch.fontpicker.data.room.util

import com.mitch.fontpicker.data.room.model.Font
import timber.log.Timber

object ComparisonUtils {

    enum class FontComparisonResult {
        COMPLETELY_IDENTICAL,
        IDENTICAL_ONE_RECYCLED,
        SAME_TITLE_DIFFERENT_URL,
        DIFFERENT_TITLE_SAME_URL,
        NO_MATCH
    }

    fun compareFonts(font1: Font, font2: Font, recycleBinCategoryId: Int): FontComparisonResult {
        // Helper to normalize strings for comparison
        fun normalizeString(input: String?): String {
            return input?.trim()?.lowercase() ?: ""
        }

        // Normalize strings for comparison
        val title1 = normalizeString(font1.title)
        val title2 = normalizeString(font2.title)
        val url1 = normalizeString(font1.url)
        val url2 = normalizeString(font2.url)

        // Log the normalized strings for debugging
        Timber.d("Comparing normalized titles: '$title1' vs '$title2'")
        Timber.d("Comparing normalized URLs: '$url1' vs '$url2'")

        // Determine if titles and URLs are equal
        val titlesEqual = title1 == title2
        val urlsEqual = url1 == url2

        // Evaluate the comparison result
        return when {
            titlesEqual && urlsEqual -> {
                Timber.d("Titles and URLs match.")
                when {
                    font1.categoryId == font2.categoryId -> {
                        Timber.d("Both fonts are in the same category. Result: COMPLETELY_IDENTICAL")
                        FontComparisonResult.COMPLETELY_IDENTICAL
                    }
                    font1.categoryId == recycleBinCategoryId || font2.categoryId == recycleBinCategoryId -> {
                        Timber.d("One font is in the Recycle Bin. Result: IDENTICAL_ONE_RECYCLED")
                        FontComparisonResult.IDENTICAL_ONE_RECYCLED
                    }
                    else -> {
                        Timber.d("Titles and URLs match, but categories differ. Result: NO_MATCH")
                        FontComparisonResult.NO_MATCH
                    }
                }
            }
            titlesEqual -> {
                Timber.d("Titles match but URLs differ. Result: SAME_TITLE_DIFFERENT_URL")
                FontComparisonResult.SAME_TITLE_DIFFERENT_URL
            }
            urlsEqual -> {
                Timber.d("URLs match but titles differ. Result: DIFFERENT_TITLE_SAME_URL")
                FontComparisonResult.DIFFERENT_TITLE_SAME_URL
            }
            else -> {
                Timber.d("Neither titles nor URLs match. Result: NO_MATCH")
                FontComparisonResult.NO_MATCH
            }
        }
    }
}
