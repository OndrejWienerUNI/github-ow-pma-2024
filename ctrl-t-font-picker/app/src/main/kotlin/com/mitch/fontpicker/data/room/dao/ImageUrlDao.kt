package com.mitch.fontpicker.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mitch.fontpicker.data.room.model.ImageUrl
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("UNUSED")
interface ImageUrlDao {
    @Insert
    suspend fun insert(imageUrl: ImageUrl): Long

    @Insert
    suspend fun insertAll(imageUrls: List<ImageUrl>): List<Long>

    @Query("SELECT * FROM image_url WHERE fontId = :fontId ORDER BY id ASC")
    fun getImageUrlsForFont(fontId: Int): Flow<List<ImageUrl>>

    @Query("SELECT * FROM image_url")
    fun getAllImageUrls(): Flow<List<ImageUrl>>

    @Query("DELETE FROM image_url WHERE fontId = :fontId")
    suspend fun deleteImageUrlsByFontId(fontId: Int)

    @Delete
    suspend fun delete(imageUrl: ImageUrl)

    @Query("DELETE FROM image_url")
    suspend fun deleteAllImageUrls()
}
