package com.mitch.fontpicker.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mitch.fontpicker.data.room.model.BitmapData
import com.mitch.fontpicker.data.room.model.Font
import com.mitch.fontpicker.data.room.model.ImageUrl
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("UNUSED")
interface FontDao {
    @Insert
    suspend fun insert(font: Font): Long

    @Transaction
    suspend fun insertFontWithAssets(
        font: Font,
        imageUrls: List<ImageUrl>,
        bitmapData: List<BitmapData>,
        imageUrlDao: ImageUrlDao,
        bitmapDataDao: BitmapDataDao
    ) {
        val fontId = insert(font).toInt()
        val updatedImageUrls = imageUrls.map { it.copy(fontId = fontId) }
        val updatedBitmapData = bitmapData.map { it.copy(fontId = fontId) }

        imageUrlDao.insertAll(updatedImageUrls)
        bitmapDataDao.insertAll(updatedBitmapData)
    }

    @Insert
    suspend fun insertAll(fonts: List<Font>): List<Long>

    @Query("SELECT * FROM font WHERE id = :id")
    suspend fun getFontById(id: Int): Font?

    @Query("SELECT * FROM font")
    fun getAllFonts(): Flow<List<Font>>

    @Query("SELECT * FROM font WHERE categoryId = :categoryId ORDER BY title ASC")
    fun getFontsByCategory(categoryId: Int): Flow<List<Font>>

    @Query("SELECT * FROM font WHERE title = :title COLLATE NOCASE LIMIT 1")
    suspend fun findFontByTitleIgnoreCase(title: String): Font?

    @Query("SELECT * FROM font WHERE url = :url LIMIT 1")
    suspend fun findFontByUrl(url: String): Font?

    @Update
    suspend fun updateFont(font: Font)

    @Query("DELETE FROM font WHERE id IN (:ids)")
    suspend fun deleteAllByIds(ids: List<Int>)

    @Query("DELETE FROM font WHERE id = :id")
    suspend fun deleteFontById(id: Int)

    @Delete
    suspend fun delete(font: Font)

    @Update
    suspend fun updateAll(fonts: List<Font>)

    @Query("DELETE FROM font")
    suspend fun deleteAllFonts()
}