package com.example.pma12_note_hub_database.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pma12_note_hub_database.R
import com.example.pma12_note_hub_database.data.model.Category
import com.example.pma12_note_hub_database.data.model.Note
import com.example.pma12_note_hub_database.data.model.NoteAdapter
import com.example.pma12_note_hub_database.data.model.NoteHubDatabase
import com.example.pma12_note_hub_database.data.model.NoteHubDatabaseInstance
import com.example.pma12_note_hub_database.data.repository.NoteRepository
import com.example.pma12_note_hub_database.utils.SortingUtils
import com.example.pma12_note_hub_database.databinding.ActivityMainBinding
import com.example.pma12_note_hub_database.utils.SpinnerUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var noteRepository: NoteRepository

    private var currentSortMode = SortMode.RECENCY_DESCENDING
    private var currentCategory: String = ""

    enum class SortMode {
        RECENCY_DESCENDING,
        RECENCY_ASCENDING,
        TITLE_DESCENDING,
        TITLE_ASCENDING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If needed, wipeDatabase() can be used to delete everything in the database,
        // but the structure will stay the same (use adb or versions when adding columns)

        // Initialize repository
        val noteHubDatabase: NoteHubDatabase = NoteHubDatabaseInstance.getDatabase(this)
        noteRepository = NoteRepository(database=noteHubDatabase)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        setupUI()
        loadNotes()

        binding.fabAddNote.setOnClickListener {
            showNoteDialog() // Opens dialog for adding a note
        }
    }

    private fun setupUI() {
        setupFilterSpinner()
        setupSortButtons()
        getString(R.string.all_categories_str)
    }

    private fun setupFilterSpinner() {
        lifecycleScope.launch {
            val categories = noteRepository.getAllCategories()
            val categoryNames = categories.map { it.name }.toMutableList()
            categoryNames.add(0, getString(R.string.all_categories_str))

            val spinnerAdapter: ArrayAdapter<String> =
                ArrayAdapter(this@MainActivity,
                    R.layout.centered_spinner_item, categoryNames)

            // Use SpinnerUtils to populate and handle selection
            SpinnerUtils.populateSpinner(
                context = this@MainActivity,
                spinner = binding.spFilterCategory,
                items = categoryNames,
                defaultSelection = getString(R.string.all_categories_str),
                onItemSelected = { selectedCategory ->
                    currentCategory = selectedCategory
                    loadNotes() // Reload notes based on the selected category
                }
            )
            binding.spFilterCategory.adapter=spinnerAdapter
        }
    }

    private fun setupSortButtons() {
        binding.btnSortedBy.setOnClickListener {
            // Cycle through the sort modes
            currentSortMode = when (currentSortMode) {
                SortMode.RECENCY_DESCENDING -> SortMode.RECENCY_ASCENDING
                SortMode.RECENCY_ASCENDING -> SortMode.TITLE_DESCENDING
                SortMode.TITLE_DESCENDING -> SortMode.TITLE_ASCENDING
                SortMode.TITLE_ASCENDING -> SortMode.RECENCY_DESCENDING
            }
            // Update the button text based on the current mode
            val buttonText = when (currentSortMode) {
                SortMode.RECENCY_DESCENDING -> getString(R.string.sorted_recency_dsc)
                SortMode.RECENCY_ASCENDING -> getString(R.string.sorted_recency_asc)
                SortMode.TITLE_DESCENDING -> getString(R.string.sorted_title_dsc)
                SortMode.TITLE_ASCENDING -> getString(R.string.sorted_title_asc)
            }
            binding.btnSortedBy.text = buttonText

            loadNotes()
        }
        // Initialize the button text to match the initial sort mode
        binding.btnSortedBy.text = getString(R.string.sorted_recency_dsc)
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            // Fetch all notes, filtered by category if selected
            val notes = if (currentCategory == getString(R.string.all_categories_str)) {
                noteRepository.getAllNotes()
            } else {
                val category: Category? = noteRepository.getCategoryByName(currentCategory)
                if (category != null) {
                    noteRepository.getNotesByCategory(category.id)
                } else {
                    emptyList()
                }
            }

            // Apply sorting based on the current mode
            val sortedNotes = SortingUtils.sortNotes(notes, currentSortMode)

            // Update RecyclerView
            noteAdapter = NoteAdapter(
                notes = sortedNotes,
                onDeleteClick = { note -> deleteNote(note) },
                onEditClick = { note -> showNoteDialog(note) },
                lifecycleScope = lifecycleScope,
                categoryDao = noteRepository.categoryDao // Pass only the DAO
            )
            binding.recyclerView.adapter = noteAdapter
        }
    }

    @OptIn(UnstableApi::class)
    private fun showNoteDialog(note: Note? = null) {
        if (!::noteRepository.isInitialized) {
            Log.e("MainActivity", "NoteRepository is not initialized")
            return
        }

        val dialog = NoteDialogFragment(
            note = note,
            onSave = { title, content, categoryId ->
                lifecycleScope.launch {
                    if (note == null) {
                        // Adding a new note
                        noteRepository.addNote(
                            Note(
                                title = title,
                                content = content,
                                categoryId = categoryId,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } else {
                        // Editing an existing note
                        val updatedNote = note.copy(
                            title = title,
                            content = content,
                            categoryId = categoryId,
                            timestamp = System.currentTimeMillis()
                        )
                        noteRepository.updateNote(updatedNote)
                    }
                    loadNotes()
                }
            },
            fetchCategories = { // Suspended lambda passed directly
                noteRepository.getAllCategories().map { it.name }
            },
            noteRepository = noteRepository
        )
        dialog.show(supportFragmentManager, "NoteDialog")
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            noteRepository.deleteNote(note)
            loadNotes()
        }
    }

    // For wiping all contents without changing table structure - be careful
    @Suppress("unused")
    private fun wipeDatabase() {
        lifecycleScope.launch {
            noteRepository.wipeDatabase()
            loadNotes()
        }
    }

}
