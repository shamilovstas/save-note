package com.shamilovstas.text_encrypt.notes.list

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.databinding.ItemNoteBinding
import com.shamilovstas.text_encrypt.notes.MEDIUM_DATE_FORMATTER
import com.shamilovstas.text_encrypt.notes.domain.Note

class NotesAdapter(
    private val onClickListener: (Note) -> Unit = {},
    private val onDeleteClickListener: (Note) -> Unit = {},
    private val onShareClickListener: (Note) -> Unit = {},
    private val onCopyClickListener: (Note) -> Unit = {}
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
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

        val noteViewHolder = NoteViewHolder(binding)
        binding.root.setOnClickListener {
            val position = noteViewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val data = getItem(position)
                onClickListener.invoke(data)
            }
        }

        binding.menuNote.setOnClickListener {
            val position = noteViewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val data = getItem(position)
                showMenu(binding.menuNote, data)
            }
        }
        return noteViewHolder
    }

    private fun showMenu(v: View, item: Note) {
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.item_note_popup_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.share_note -> onShareClickListener.invoke(item)
                R.id.delete_note -> onDeleteClickListener.invoke(item)
                R.id.copy_note -> onCopyClickListener.invoke(item)
                else -> throw IllegalStateException("Handler for this option is missing")
            }
            return@setOnMenuItemClickListener true
        }
        popup.show()
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.noteCreateDate.text = MEDIUM_DATE_FORMATTER.format(note.createdDate)
            binding.encryptedContent.text = note.content
            val publishedIconRes = if (note.isPublished) {
                R.drawable.cloud_done
            } else {
                R.drawable.cloud
            }
            binding.ivPublished.setImageDrawable(ContextCompat.getDrawable(binding.root.context, publishedIconRes))

            val attachmentsCount = note.attachments.size
            binding.tvAttachmentsCount.text = itemView.context.getString(R.string.note_attachments_count, attachmentsCount)
            if (note.description.isNotEmpty()) {
                binding.tvNoteDescription.visibility = View.VISIBLE
                binding.tvNoteDescription.text = note.description
            }
        }
    }
}