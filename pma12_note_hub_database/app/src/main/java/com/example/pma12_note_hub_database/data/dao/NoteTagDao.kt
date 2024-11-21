package com.example.pma12_note_hub_database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.pma12_note_hub_database.data.model.Note
import com.example.pma12_note_hub_database.data.model.NoteTagCrossRef
import com.example.pma12_note_hub_database.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteTagDao {

    @Insert
    suspend fun insert(noteTagCrossRef: NoteTagCrossRef)

    // Loads all tags from a single note and returns them as flow
    @Transaction
    @Query("SELECT * FROM tag INNER JOIN note_tag_cross_ref " +
            "ON tag.id = note_tag_cross_ref.tagId " +
            "WHERE note_tag_cross_ref.noteId = :noteId")
    fun getTagsForNote(noteId: Int): Flow<List<Tag>>

    // Loads all notes that have the same tag assigned to them
    @Transaction
    @Query("SELECT * FROM note INNER JOIN note_tag_cross_ref " +
            "ON note.id = note_tag_cross_ref.noteId " +
            "WHERE note_tag_cross_ref.tagId = :tagId")
    fun getNotesForTag(tagId: Int): Flow<List<Note>>
}