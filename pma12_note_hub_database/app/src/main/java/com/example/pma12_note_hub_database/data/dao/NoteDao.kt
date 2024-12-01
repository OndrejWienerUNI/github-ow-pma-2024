package com.example.pma12_note_hub_database.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pma12_note_hub_database.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    // Loads all notes from database and returns them as flow
    @Query("SELECT * FROM note ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    // Deletes all notes from the table - be careful
    @Query("DELETE FROM note")
    suspend fun deleteAllNotes()

    // Loads all notes from a certain category and returns them as flow
    @Query("SELECT * FROM note WHERE categoryId = :categoryId")
    fun getNotesByCategoryId(categoryId: Int): Flow<List<Note>>

    // Update a note's timestamp - this should be called on addition
    @Query("UPDATE note SET timestamp = :timestamp WHERE id = :noteId")
    suspend fun updateTimestamp(noteId: Int, timestamp: Long)

    // Resets auto increment for a specified table
    @Query("DELETE FROM sqlite_sequence WHERE name = :tableName")
    suspend fun resetAutoIncrement(tableName: String)
}