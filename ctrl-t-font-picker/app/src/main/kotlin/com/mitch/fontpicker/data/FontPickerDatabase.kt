package com.mitch.fontpicker.data

import androidx.room.RoomDatabase

// see at https://developer.android.com/training/data-storage/room#database
/*@Database(
    entities = [
        // MyEntity::class
    ],
    version = 1
)*/
abstract class FontPickerDatabase : RoomDatabase() {
    // abstract fun myDao(): MyDao
}