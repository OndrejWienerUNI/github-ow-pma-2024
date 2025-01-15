package com.mitch.fontpicker.data.room

import android.content.Context
import androidx.room.Room

object FontsDatabaseInstance {

    @Volatile
    private var INSTANCE: FontsDatabase? = null

    fun getDatabase(context: Context): FontsDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                FontsDatabase::class.java,
                "fonts.db"
            )
                .fallbackToDestructiveMigration() // Consider proper migrations for production
                .build()
            INSTANCE = instance
            instance
        }
    }
}
