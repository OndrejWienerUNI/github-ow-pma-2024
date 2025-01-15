package com.mitch.fontpicker.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mitch.fontpicker.data.room.model.BitmapData
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("UNUSED")
interface BitmapDataDao {
    @Insert
    suspend fun insert(bitmapData: BitmapData): Long

    @Insert
    suspend fun insertAll(bitmapDataList: List<BitmapData>): List<Long>

    @Query("SELECT * FROM bitmap_data WHERE fontId = :fontId ORDER BY id ASC")
    fun getBitmapDataForFont(fontId: Int): Flow<List<BitmapData>>

    @Query("SELECT * FROM bitmap_data")
    fun getAllBitmapData(): Flow<List<BitmapData>>

    @Query("DELETE FROM bitmap_data WHERE fontId = :fontId")
    suspend fun deleteBitmapDataByFontId(fontId: Int)

    @Delete
    suspend fun delete(bitmapData: BitmapData)

    @Query("DELETE FROM bitmap_data")
    suspend fun deleteAllBitmapData()
}
