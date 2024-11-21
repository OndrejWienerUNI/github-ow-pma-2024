package com.example.pma12_note_hub_database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.pma12_note_hub_database.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    // Loads all categories and returns them as flow
    @Query("SELECT * FROM category ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
}