package com.mitch.fontpicker.data.room.repository

import com.mitch.fontpicker.di.DependenciesProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber

/**
 * Example repository for local DB interactions, e.g. storing favorite fonts in Room.
 */
@Suppress("UNUSED")
class FontDatabaseRepository(
    private val dependenciesProvider: DependenciesProvider
) {
    // Suppose dependenciesProvider.db.fontDao() is how you'd get your DAO...
    // For now, just placeholders:

    fun getFavoriteFonts(): Flow<List<String>> {
        // Return a Flow from your DB if you have it.
        return flowOf(emptyList())
    }

    suspend fun addFavoriteFont(fontName: String) {
        Timber.d("Added favorite font: $fontName")
        // dependenciesProvider.db.fontDao().insert(...)
    }

    suspend fun removeFavoriteFont(fontName: String) {
        Timber.d("Removed favorite font: $fontName")
        // dependenciesProvider.db.fontDao().delete(...)
    }
}
