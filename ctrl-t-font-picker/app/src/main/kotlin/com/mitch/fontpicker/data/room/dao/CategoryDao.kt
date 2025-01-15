package com.mitch.fontpicker.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mitch.fontpicker.data.room.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("UNUSED")
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Query("SELECT * FROM category")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Query("SELECT id FROM category WHERE name = :name COLLATE NOCASE")
    suspend fun getCategoryIdByNameIgnoreCase(name: String): Int?

    @Query("SELECT * FROM category WHERE name = :name COLLATE NOCASE")
    suspend fun getCategoryByNameIgnoreCase(name: String): Category?

    @Query("UPDATE category SET name = :newName WHERE id = :id")
    suspend fun updateCategoryName(id: Int, newName: String)

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("DELETE FROM category")
    suspend fun deleteAllCategories()
}
