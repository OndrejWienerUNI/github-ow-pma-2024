package com.example.pma12_note_hub_database.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View
import android.widget.Spinner
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
    private var isNameAscending = true
    private var currentCategory: String = ""

    private enum class SortMode {
        RECENCY_DESCENDING,
        RECENCY_ASCENDING,
        TITLE_DESCENDING,
        TITLE_ASCENDING
    }

    private var currentSortMode: SortMode = SortMode.RECENCY_DESCENDING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If needed, wipeDatabase() can be used to delete everything in the database,
        // but the structure will stay the same (use adb or versions when adding columns)

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
        setupUI()

        binding.fabAddNote.setOnClickListener {
            addNote()
        }
    }

    private fun setupUI() {
        setupFilterSpinner()
        setupSortButtons()
        getString(R.string.all_categories_str)
    }

    private fun setupFilterSpinner() {
        lifecycleScope.launch {
            val categories = database.categoryDao().getAllCategories().first()
            val categoryNames = categories.map { it.name }.toMutableList()
            categoryNames.add(0, getString(R.string.all_categories_str))

            val adapter = ArrayAdapter(this@MainActivity,
                R.layout.custom_spiner_item, categoryNames)
            adapter.setDropDownViewResource(R.layout.custom_spiner_item)
            binding.spFilterCategory.adapter = adapter

            // Sets up an on-item listener
            binding.spFilterCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    currentCategory = categoryNames[position]
                    loadNotes()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
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

    private fun addNoteToDatabase(title: String, content: String, categoryId: Int) {
        lifecycleScope.launch {
            val newNote = Note(
                title = title,
                content = content,
                categoryId = categoryId,
                timestamp = System.currentTimeMillis()
            )
            database.noteDao().insert(newNote)
            loadNotes()
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            var notes = if (currentCategory == getString(R.string.all_categories_str)) {
                database.noteDao().getAllNotes().first()
            } else {
                val category = database.categoryDao().getCategoryByName(currentCategory)
                if (category != null) {
                    database.noteDao().getNotesByCategoryId(category.id).first()
                } else {
                    emptyList()
                }
            }

            // Apply sorting based on the current mode
            notes = when (currentSortMode) {
                SortMode.RECENCY_DESCENDING -> notes.sortedByDescending { it.timestamp }
                SortMode.RECENCY_ASCENDING -> notes.sortedBy { it.timestamp }
                SortMode.TITLE_DESCENDING ->
                    notes.sortedWith(compareByDescending { it.title.lowercase() })
                SortMode.TITLE_ASCENDING ->
                    notes.sortedWith(compareBy { it.title.lowercase() })
            }

            // Update RecyclerView
            noteAdapter = NoteAdapter(
                notes = notes,
                onDeleteClick = { note -> deleteNote(note) },
                onEditClick = { note -> editNote(note) },
                lifecycleScope = lifecycleScope,
                database = database
            )
            binding.recyclerView.adapter = noteAdapter
        }
    }

    private suspend fun populateCategorySpinner(
        spinner: Spinner,
        selectedCategoryId: Int? = null
    ): String {
        val noneCategoryText = getString(R.string.cat_none_str)
        val invalidCategoryText = getString(R.string.cat_invalid_str)

        val categories = database.categoryDao().getAllCategories().first()
        val categoryNames = categories.map { it.name }.toMutableList()

        val currentCategoryName = if (selectedCategoryId == null) {
            noneCategoryText
        } else {
            val category = database.categoryDao().getCategoryById(selectedCategoryId)
            category?.name ?: invalidCategoryText
        }

        if (!categoryNames.contains(currentCategoryName)) {
            categoryNames.add(0, currentCategoryName) // Add current category if missing
        }

        val adapter = ArrayAdapter(
            this@MainActivity,
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(categoryNames.indexOf(currentCategoryName))

        return currentCategoryName
    }

    private fun showNoteDialog(
        note: Note? = null,
        onSave: (String, String, Int?) -> Unit
    ) {
        val dialogBinding = DialogAddNoteBinding.inflate(layoutInflater)
        val titleEditText = dialogBinding.etNoteTitle
        val contentEditText = dialogBinding.etNoteContent
        val spinnerCategory = dialogBinding.spCategory

        // Set existing note details if editing
        if (note != null) {
            titleEditText.setText(note.title)
            contentEditText.setText(note.content)
        }

        lifecycleScope.launch {
            val categoryId = note?.categoryId
            populateCategorySpinner(spinnerCategory, categoryId)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (note == null) R.string.add_note_str else R.string.edit_note_str)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save_str) { _, _ ->
                val title = titleEditText.text.toString()
                val content = contentEditText.text.toString()
                val selectedCategory = spinnerCategory.selectedItem.toString()

                lifecycleScope.launch {
                    val category = database.categoryDao().getCategoryByName(selectedCategory)
                    val categoryId = category?.id ?: if (selectedCategory == getString(R.string.cat_none_str)) null else note?.categoryId
                    onSave(title, content, categoryId)
                }
            }
            .setNegativeButton(getString(R.string.cancel_str), null)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_rectangle)
        dialog.show()
    }

    private fun addNote() {
        showNoteDialog { title, content, categoryId ->
            addNoteToDatabase(title, content, categoryId ?: 0)
        }
    }

    private fun editNote(note: Note) {
        showNoteDialog(note) { updatedTitle, updatedContent, updatedCategoryId ->
            val updatedNote = note.copy(
                title = updatedTitle,
                content = updatedContent,
                categoryId = updatedCategoryId,
                timestamp = System.currentTimeMillis()
            )
            lifecycleScope.launch {
                database.noteDao().update(updatedNote)
                loadNotes()
            }
        }
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

    // Deletes everything in the database - be careful
    @Suppress("unused")
    private fun wipeDatabase() {
        lifecycleScope.launch {
            database.noteDao().deleteAllNotes()
            database.categoryDao().deleteAllCategories()
            resetAutoIncrement("note")
            resetAutoIncrement("category")
        }
    }

    private fun resetAutoIncrement(tableName: String) {
        lifecycleScope.launch {
            database.openHelper.writableDatabase.execSQL("DELETE FROM sqlite_sequence WHERE name = '$tableName'")
        }
    }


}
