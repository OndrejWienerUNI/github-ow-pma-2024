package com.example.pma12_note_hub_database.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.pma12_note_hub_database.R
import com.example.pma12_note_hub_database.data.model.Note
import com.example.pma12_note_hub_database.data.repository.NoteRepository
import com.example.pma12_note_hub_database.databinding.NoteDialogFragmentBinding
import com.example.pma12_note_hub_database.utils.SpinnerUtils
import kotlinx.coroutines.launch


class NoteDialogFragment(
    private val note: Note? = null, // Null for new note
    private val onSave: (String, String, Int?) -> Unit, // Callback for saving
    private val fetchCategories: suspend () -> List<String>, // Suspend lambda for fetching categories
    private val noteRepository: NoteRepository // Repository for accessing database
) : DialogFragment() {

    private var _binding: NoteDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = NoteDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate fields if editing a note
        note?.let {
            binding.etNoteTitle.setText(it.title)
            binding.etNoteContent.setText(it.content)
        }

        // Set up category spinner
        setupCategorySpinner()

        // Handle save button click
        binding.btnSave.setOnClickListener {
            saveNote()
        }

        // Handle cancel button click
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        // Set the dialog width to 90% of the screen width
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.93).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupCategorySpinner() {
        lifecycleScope.launch {
            try {
                val categoryNames = fetchCategories() // Fetch categories

                val spinnerAdapter: ArrayAdapter<String> =
                    ArrayAdapter(requireContext(),
                        R.layout.centered_spinner_item,
                        categoryNames)

                SpinnerUtils.populateSpinner(
                    context = requireContext(),
                    spinner = binding.spCategory,
                    items = categoryNames,
                    defaultSelection = note?.categoryId?.toString() ?: "",
                    onItemSelected = { /* Handle selection if needed */ }
                )
                binding.spCategory.adapter = spinnerAdapter

            } catch (e: Exception) {
                e.printStackTrace() // Handle errors gracefully
            }
        }
    }

    private fun saveNote() {
        val title = binding.etNoteTitle.text.toString()
        val content = binding.etNoteContent.text.toString()
        val selectedCategory = binding.spCategory.selectedItem.toString()

        lifecycleScope.launch {
            try {
                val category = noteRepository.categoryDao.getCategoryByName(selectedCategory)
                val categoryId = category?.id

                if (title.isNotBlank() && content.isNotBlank()) {
                    onSave(title, content, categoryId)
                    dismiss()
                } else {
                    // Optionally, show an error to the user for invalid inputs
                    binding.etNoteTitle.error = "No Title"
                    binding.etNoteContent.error = "No Content"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val noteDialog: Dialog = Dialog(requireContext(), R.style.custom_dialog)
        return noteDialog
    }
}
