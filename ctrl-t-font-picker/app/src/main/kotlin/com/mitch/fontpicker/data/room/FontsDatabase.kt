package com.mitch.fontpicker.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mitch.fontpicker.data.room.dao.BitmapDataDao
import com.mitch.fontpicker.data.room.dao.CategoryDao
import com.mitch.fontpicker.data.room.dao.FontDao
import com.mitch.fontpicker.data.room.dao.ImageUrlDao
import com.mitch.fontpicker.data.room.model.BitmapData
import com.mitch.fontpicker.data.room.model.Category
import com.mitch.fontpicker.data.room.model.Font
import com.mitch.fontpicker.data.room.model.ImageUrl

@Database(
    entities = [Font::class, ImageUrl::class, BitmapData::class, Category::class],
    version = 2,
    exportSchema = false
)
abstract class FontsDatabase : RoomDatabase() {
    abstract fun fontDao(): FontDao
    abstract fun imageUrlDao(): ImageUrlDao
    abstract fun bitmapDataDao(): BitmapDataDao
    abstract fun categoryDao(): CategoryDao
}
