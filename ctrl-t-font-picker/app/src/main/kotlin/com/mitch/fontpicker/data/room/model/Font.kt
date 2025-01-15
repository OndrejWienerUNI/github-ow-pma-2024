package com.mitch.fontpicker.data.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "font")
data class Font(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val categoryId: Int, // References the category table
    val deletionTimestamp: Long? = null // Epoch seconds
)
