package com.example.pma12_note_hub_database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.pma12_note_hub_database.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert
    suspend fun insert(tag: Tag)

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    // Loads all tags in database and returns them as flow
    @Query("SELECT * FROM tag ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>
}