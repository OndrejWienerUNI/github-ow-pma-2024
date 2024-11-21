package com.example.pma12_note_hub_database.data

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pma12_note_hub_database.databinding.ItemNoteBinding

class NoteAdapter(
    private val notes: List<Note>,
    private val onDeleteClick: (Note) -> Unit,
    private val onEditClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteContentPreview.text = note.content

            binding.icDelete.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes") { _, _ ->
                        onDeleteClick(note)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            binding.icEdit.setOnClickListener {
                onEditClick(note)
            }
        }
    }
}