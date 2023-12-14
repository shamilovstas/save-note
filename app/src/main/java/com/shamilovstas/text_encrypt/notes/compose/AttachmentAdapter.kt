package com.shamilovstas.text_encrypt.notes.compose

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shamilovstas.text_encrypt.databinding.ItemAttachmentBinding
import com.shamilovstas.text_encrypt.notes.domain.Attachment

class AttachmentAdapter : ListAdapter<Attachment, AttachmentAdapter.AttachmentViewHolder>(
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
        return viewHolder
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AttachmentViewHolder(val binding: ItemAttachmentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(attachment: Attachment) = with(binding) {
            Log.d("AttachmentAdapter", "uri: ${attachment.uri.toString()}, name: ${attachment.filename}")
            binding.attachmentPreview.load(attachment.uri)
            binding.attachmentDesc.setText(attachment.filename)
        }

    }
}