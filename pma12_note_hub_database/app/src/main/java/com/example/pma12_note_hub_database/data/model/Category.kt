package com.example.pma12_note_hub_database.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class Category(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String

)