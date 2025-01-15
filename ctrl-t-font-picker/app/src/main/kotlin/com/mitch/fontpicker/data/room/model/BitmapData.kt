package com.mitch.fontpicker.data.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bitmap_data",
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
data class BitmapData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fontId: Int, // References Font
    val bitmap: ByteArray // Bitmap binary data
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitmapData) return false

        return id == other.id &&
                fontId == other.fontId &&
                bitmap.contentEquals(other.bitmap) // Compare ByteArray contents
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + fontId
        result = 31 * result + bitmap.contentHashCode() // Use ByteArray content hash
        return result
    }
}
