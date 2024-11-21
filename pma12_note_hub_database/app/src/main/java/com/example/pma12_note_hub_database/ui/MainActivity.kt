package com.example.pma12_note_hub_database.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pma12_note_hub_database.R
import com.example.pma12_note_hub_database.data.model.Category
import com.example.pma12_note_hub_database.data.model.Note
import com.example.pma12_note_hub_database.data.model.NoteAdapter
import com.example.pma12_note_hub_database.data.model.NoteHubDatabase
import com.example.pma12_note_hub_database.data.model.NoteHubDatabaseInstance
import com.example.pma12_note_hub_database.data.model.Tag
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
        // insertSampleNotes()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // noteAdapter = NoteAdapter(
        //     emptyList(),
        //     onDeleteClick = { note -> deleteNote(note) },
        //     onEditClick = { note -> editNote(note) }
        // )
        // binding.recyclerView.adapter = noteAdapter
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
            .setTitle(R.string.add_note_str)
            .setView(dialogView)
            .setPositiveButton(R.string.save_str) { _, _ ->
                val title = titleEditText.text.toString()
                val content = contentEditText.text.toString()
                addNoteToDatabase(title, content)
            }
            .setNegativeButton(R.string.cancel_str, null)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_rectangle)
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

    private fun editNote(note: Note) {
        val dialogBinding = DialogAddNoteBinding.inflate(layoutInflater)
        val titleEditText = dialogBinding.etNoteTitle
        val contentEditText = dialogBinding.etNoteContent

        titleEditText.setText(note.title)
        contentEditText.setText(note.content)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.edit_note_str)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save_str) { _, _ ->
                val updatedTitle = titleEditText.text.toString()
                val updatedContent = contentEditText.text.toString()

                lifecycleScope.launch {
                    val updatedNote = note.copy(title = updatedTitle, content = updatedContent)
                    database.noteDao().update(updatedNote)
                    loadNotes()
                }
            }
            .setNegativeButton(getString(R.string.cancel_str), null)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_rectangle)
        dialog.show()
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            database.noteDao().delete(note)
            loadNotes()
        }
    }

    @Suppress("unused")
    private fun insertSampleNotes() {
        lifecycleScope.launch {
            val existingNotes = database.noteDao().getAllNotes().first()
            val existingNoteTitles = existingNotes.map { it.title }

            val sampleNotes = listOf(
                Note(title = "Note 1", content = "Contents of Note 1"),
                Note(title = "Note 2", content = "Contents of Note 2"),
                Note(title = "Note 3", content = "Contents of Note 3")
            )

            sampleNotes
                .filter { it.title !in existingNoteTitles }
                .forEach { database.noteDao().insert(it) }
        }
    }

    private fun insertDefaultCategories() {
        lifecycleScope.launch {
            val existingCategories = database.categoryDao().getAllCategories().first()
            val existingCategoryNames = existingCategories.map { it.name }

            val defaultCategories = listOf(
                Category(name = getString(R.string.cat_work_str)),
                Category(name = getString(R.string.cat_personal_str)),
                Category(name = getString(R.string.cat_ideas_str))
            )

            defaultCategories
                .filter { it.name !in existingCategoryNames }
                .forEach { database.categoryDao().insert(it) }
        }
    }

    private fun insertDefaultTags() {
        lifecycleScope.launch {
            val existingTags = database.tagDao().getAllTags().first()
            val existingTagNames = existingTags.map { it.name }

            val defaultTags = listOf(
                Tag(name = getString(R.string.tag_important_str)),
                Tag(name = getString(R.string.tag_quick_task_str)),
                Tag(name = getString(R.string.tag_project_str)),
                Tag(name = getString(R.string.tag_idea_str))
            )

            defaultTags
                .filter { it.name !in existingTagNames }
                .forEach { database.tagDao().insert(it) }
        }
    }

}
