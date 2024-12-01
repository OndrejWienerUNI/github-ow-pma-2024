package com.example.pma12_note_hub_database.data.repository

import com.example.pma12_note_hub_database.data.dao.CategoryDao
import com.example.pma12_note_hub_database.data.dao.NoteDao
import com.example.pma12_note_hub_database.data.dao.NoteTagDao
import com.example.pma12_note_hub_database.data.dao.TagDao
import com.example.pma12_note_hub_database.data.model.Category
import com.example.pma12_note_hub_database.data.model.Note
import com.example.pma12_note_hub_database.data.model.NoteHubDatabase
import com.example.pma12_note_hub_database.data.model.Tag
import kotlinx.coroutines.flow.first

// Handles data operations and abstracts the database logic
class NoteRepository(private val database: NoteHubDatabase) {

    val categoryDao: CategoryDao get() = database.categoryDao()
    val noteDao: NoteDao get() = database.noteDao()
    val noteTagDao: NoteTagDao get() = database.noteTagDao()
    val tagDao: TagDao get() = database.tagDao()

    // Note-related methods
    suspend fun getAllNotes() = database.noteDao().getAllNotes().first()

    suspend fun getNotesByCategory(categoryId: Int) =
        noteDao.getNotesByCategoryId(categoryId).first()

    suspend fun addNote(note: Note) {
        noteDao.insert(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.update(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }

    // Category-related methods
    suspend fun getAllCategories() = database.categoryDao().getAllCategories().first()

    suspend fun getCategoryByName(name: String) =
        categoryDao.getCategoryByName(name)

    suspend fun addCategory(category: Category) {
        categoryDao.insert(category)
    }

    // Tag-related methods
    suspend fun getAllTags() = database.tagDao().getAllTags().first()

    suspend fun addTag(tag: Tag) {
        tagDao.insert(tag)
    }

    // Database manipulation
    suspend fun wipeDatabase() {
        database.noteDao().deleteAllNotes()
        database.categoryDao().deleteAllCategories()
        resetNotesAutoIncrement()
        resetCategoriesAutoIncrement()
    }

    private suspend fun resetNotesAutoIncrement() {
        noteDao.resetAutoIncrement("note")
    }

    private suspend fun resetCategoriesAutoIncrement() {
        categoryDao.resetAutoIncrement("category")
    }
}
