package com.mitch.fontpicker.data.room.repository

import com.mitch.fontpicker.di.DependenciesProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber

/**
 * Example repository for local DB interactions, e.g. storing favorite fonts in Room.
 */

 /**
  * Database sits at
  *
  *     override val database: FontPickerDatabase by lazy {
  *         Room.databaseBuilder(
  *             context,
  *             FontPickerDatabase::class.java,
  *             "fontpicker.db"
  *         ).build()
  *     }
  *
  * in dependenciesProvider. Access the rest accordingly
  */

@Suppress("UNUSED")
class FontPickerDatabaseRepository(
    private val dependenciesProvider: DependenciesProvider
) {

    fun getFavoriteFonts(): Flow<List<String>> {
        return flowOf(emptyList())
    }

    suspend fun addFavoriteFont(fontName: String) {
        Timber.d("Added favorite font: $fontName")
    }

    suspend fun removeFavoriteFont(fontName: String) {
        Timber.d("Removed favorite font: $fontName")
    }
}
