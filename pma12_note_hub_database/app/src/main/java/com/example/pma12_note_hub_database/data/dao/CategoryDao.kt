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

    @Query("SELECT * FROM category ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    @Query("SELECT * FROM category WHERE id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Int): Category?

    // Resets auto increment for a specified table
    @Query("DELETE FROM sqlite_sequence WHERE name = :tableName")
    suspend fun resetAutoIncrement(tableName: String)

    // Deletes all categories from the table - be careful
    @Query("DELETE FROM category")
    suspend fun deleteAllCategories()
}