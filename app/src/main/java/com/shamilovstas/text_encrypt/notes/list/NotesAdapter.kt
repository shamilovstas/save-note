package com.shamilovstas.text_encrypt.notes.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.databinding.ItemNoteBinding
import com.shamilovstas.text_encrypt.notes.DATE_FORMATTER
import com.shamilovstas.text_encrypt.notes.domain.Note

class NotesAdapter : ListAdapter<Note, NotesAdapter.NoteViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.noteCreateDate.text = DATE_FORMATTER.format(note.createdDate)
            binding.encryptedContent.text = note.content
            val publishedIconRes = if (note.isPublished) {
                R.drawable.cloud_done
            } else {
                R.drawable.cloud
            }
            binding.ivPublished.setImageDrawable(ContextCompat.getDrawable(binding.root.context, publishedIconRes))
            binding.tvAttachmentsCount.text = itemView.context.getString(R.string.note_attachments_count, 25) // TODO change to actual attachments count
        }
    }
}