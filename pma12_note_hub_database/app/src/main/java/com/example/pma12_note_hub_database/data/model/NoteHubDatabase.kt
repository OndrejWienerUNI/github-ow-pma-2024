package com.example.pma12_note_hub_database.data.model

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pma12_note_hub_database.data.dao.CategoryDao
import com.example.pma12_note_hub_database.data.dao.NoteDao
import com.example.pma12_note_hub_database.data.dao.NoteTagDao
import com.example.pma12_note_hub_database.data.dao.TagDao

@Database(
    entities = [Note::class, Category::class, Tag::class, NoteTagCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class NoteHubDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun noteTagDao(): NoteTagDao

}