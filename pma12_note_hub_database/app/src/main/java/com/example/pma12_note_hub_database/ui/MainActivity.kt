package com.example.pma12_note_hub_database.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pma12_note_hub_database.R
import com.example.pma12_note_hub_database.data.Category
import com.example.pma12_note_hub_database.data.Note
import com.example.pma12_note_hub_database.data.NoteAdapter
import com.example.pma12_note_hub_database.data.NoteHubDatabase
import com.example.pma12_note_hub_database.data.NoteHubDatabaseInstance
import com.example.pma12_note_hub_database.data.Tag
import com.example.pma12_note_hub_database.databinding.ActivityMainBinding
import com.example.pma12_note_hub_database.databinding.DialogAddNoteBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var database: NoteHubDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Database init
        database = NoteHubDatabaseInstance.getDatabase(this)

        // Insert default tags and categories to database
        insertDefaultCategories()
        insertDefaultTags()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        loadNotes()

        binding.fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun addNoteToDatabase(title: String, content: String) {
        lifecycleScope.launch {
            val newNote = Note(title = title, content = content)
            database.noteDao().insert(newNote)
            loadNotes()
        }
    }

    private fun showAddNoteDialog() {
        val dialogBinding = DialogAddNoteBinding.inflate(layoutInflater)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val titleEditText = dialogBinding.etNoteTitle
        val contentEditText = dialogBinding.etNoteContent

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEditText.text.toString()
                val content = contentEditText.text.toString()
                addNoteToDatabase(title, content)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            database.noteDao().getAllNotes().collect { notes ->
                noteAdapter = NoteAdapter(
                    notes,
                    onDeleteClick = { note -> deleteNote(note) },
                    onEditClick = { note -> editNote(note) }
                )
                binding.recyclerView.adapter = noteAdapter
            }
        }
    }

    private fun insertSampleNotes() {
        lifecycleScope.launch {
            val sampleNotes = listOf(
                Note(title = "Note 1", content = "Contents of Note 1"),
                Note(title = "Note 2", content = "Contents of Note 2"),
                Note(title = "Note 3", content = "Contents of Note 3")
            )
            sampleNotes.forEach { database.noteDao().insert(it) }
        }
    }

    private fun editNote(note: Note) {
        val dialogBinding = DialogAddNoteBinding.inflate(layoutInflater)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val titleEditText = dialogBinding.etNoteTitle
        val contentEditText = dialogBinding.etNoteContent

        titleEditText.setText(note.title)
        contentEditText.setText(note.content)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedTitle = titleEditText.text.toString()
                val updatedContent = contentEditText.text.toString()

                lifecycleScope.launch {
                    val updatedNote = note.copy(title = updatedTitle, content = updatedContent)
                    database.noteDao().update(updatedNote)
                    loadNotes()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            database.noteDao().delete(note)
            loadNotes()
        }
    }

    private fun insertDefaultCategories() {
        lifecycleScope.launch {
            val existingCategories = database.categoryDao().getAllCategories().first()
            if (existingCategories.isEmpty()) {
                val defaultCategories = listOf(
                    Category(name = "Work"),
                    Category(name = "Personal"),
                    Category(name = "Ideas")
                )
                defaultCategories.forEach { database.categoryDao().insert(it) }
            }
        }
    }

    private fun insertDefaultTags() {
        lifecycleScope.launch {
            val existingTags = database.tagDao().getAllTags().first()
            if (existingTags.isEmpty()) {
                val defaultTags = listOf(
                    Tag(name = "Important"),
                    Tag(name = "Quick Task"),
                    Tag(name = "Project"),
                    Tag(name = "Idea")
                )
                defaultTags.forEach { database.tagDao().insert(it) }
            }
        }
    }
}
