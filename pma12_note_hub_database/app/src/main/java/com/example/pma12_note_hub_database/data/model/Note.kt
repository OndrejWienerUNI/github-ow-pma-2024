package com.example.pma12_note_hub_database.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val categoryId: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

