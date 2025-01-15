package com.mitch.fontpicker.data.room.util

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("UNUSED")
object DatabaseMigration {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Only use once in production
        }
    }
}
