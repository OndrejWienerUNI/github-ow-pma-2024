package com.example.pma12_note_hub_database.data.model

import android.content.Context
import androidx.room.Room

object NoteHubDatabaseInstance {

    @Volatile
    private var INSTANCE: NoteHubDatabase? = null

    fun getDatabase(context: Context): NoteHubDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                NoteHubDatabase::class.java,
                "note_hub_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}