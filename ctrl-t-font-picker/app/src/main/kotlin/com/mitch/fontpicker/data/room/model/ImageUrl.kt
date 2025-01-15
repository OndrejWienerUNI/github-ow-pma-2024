package com.mitch.fontpicker.data.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "image_url",
    foreignKeys = [
        ForeignKey(
            entity = Font::class,
            parentColumns = ["id"],
            childColumns = ["fontId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["fontId"])]
)
data class ImageUrl(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fontId: Int, // References Font
    val url: String
)
