package com.example.pma12_note_hub_database.data.model

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.pma12_note_hub_database.R
import com.example.pma12_note_hub_database.data.dao.CategoryDao
import com.example.pma12_note_hub_database.databinding.ItemNoteBinding
import kotlinx.coroutines.launch

// Adapter for managing a list of notes in a RecyclerView; returns a NoteViewHolder for each item
class NoteAdapter(
    private val notes: List<Note>,
    private val onDeleteClick: (Note) -> Unit,
    private val onEditClick: (Note) -> Unit,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val categoryDao: CategoryDao // Required DAO from the database repo file
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    // Creates a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return NoteViewHolder(binding)
    }

    // Returns the total number of notes
    override fun getItemCount() = notes.size

    // Binds data to the given ViewHolder
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
    }

    // Nested ViewHolder class to bind individual note data
    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Binds a Note object's fields to the corresponding UI elements of the ViewHolder
        fun bind(note: Note) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteContentPreview.text = note.content

            // Resolve category name using the CategoryDao
            val categoryId = note.categoryId
            if (categoryId != null) {
                lifecycleScope.launch {
                    val category = categoryDao.getCategoryById(categoryId)
                    binding.tvNoteCategory.text = category?.name
                        ?: itemView.context.getString(R.string.cat_invalid_str)
                }
            } else {
                binding.tvNoteCategory.text = itemView.context.getString(R.string.cat_none_str)
            }

            // OnClick Listener for Delete
            binding.icDelete.setOnClickListener {
                val dialog = AlertDialog.Builder(itemView.context)
                    .setTitle(itemView.context.getString(R.string.delete_note_str))
                    .setMessage(itemView.context.getString(R.string.sure_to_delete_note_str))
                    .setPositiveButton(itemView.context.getString(R.string.yes_str)) { _, _ ->
                        onDeleteClick(note)
                    }
                    .setNegativeButton(itemView.context.getString(R.string.no_str), null)
                    .create()

                dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_rectangle)
                dialog.show()
            }

            // OnClick Listener for Edit
            binding.icEdit.setOnClickListener {
                onEditClick(note)
            }
        }
    }
}
