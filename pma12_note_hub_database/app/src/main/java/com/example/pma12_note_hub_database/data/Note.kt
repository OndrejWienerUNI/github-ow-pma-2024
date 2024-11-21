package com.example.pma12_note_hub_database.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")

data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val categoryId: Int? = null
)
