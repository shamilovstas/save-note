package com.shamilovstas.text_encrypt.notes.domain

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.shamilovstas.text_encrypt.R
import javax.inject.Inject

class CopyNoteContentUseCase @Inject constructor() {

    fun perform(context: Context, note: Note) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.note_copied_label, note.description), note.content)
        clipboard.setPrimaryClip(clip)
    }
}