package com.shamilovstas.text_encrypt.notes.compose

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.databinding.ItemAttachmentBinding
import com.shamilovstas.text_encrypt.notes.domain.Attachment

class AttachmentAdapter(
    private val onAttachmentClick: (Attachment) -> Unit = {}
) : ListAdapter<Attachment, AttachmentAdapter.AttachmentViewHolder>(
    DIFF_CALLBACK
) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Attachment>() {
            override fun areItemsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val binding = ItemAttachmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = AttachmentViewHolder(binding)

        binding.root.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onAttachmentClick.invoke(getItem(position))
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AttachmentViewHolder(val binding: ItemAttachmentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(attachment: Attachment) = with(binding) {
            Log.d("AttachmentAdapter", "uri: ${attachment.uri.toString()}, name: ${attachment.filename}")

            if (attachment.isEncrypted) {
                binding.attachmentPreview.load(ContextCompat.getDrawable(itemView.context, R.drawable.lock))
            } else {
                binding.attachmentPreview.load(attachment.uri) {
                    this.error(R.drawable.file)
                }
            }
            binding.attachmentDesc.setText(attachment.filename)
        }

    }
}