package com.example.pma12_note_hub_database.utils

import com.example.pma12_note_hub_database.ui.MainActivity.SortMode
import com.example.pma12_note_hub_database.data.model.Note

// Provides sorting logic for notes based on different modes
object SortingUtils {

    fun sortNotes(notes: List<Note>, sortMode: SortMode): List<Note> {
        return when (sortMode) {
            SortMode.RECENCY_DESCENDING -> notes.sortedByDescending { it.timestamp }
            SortMode.RECENCY_ASCENDING -> notes.sortedBy { it.timestamp }
            SortMode.TITLE_DESCENDING -> notes.sortedByDescending { it.title.lowercase() }
            SortMode.TITLE_ASCENDING -> notes.sortedBy { it.title.lowercase() }
        }
    }
}
