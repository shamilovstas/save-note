package com.shamilovstas.text_encrypt.notes.domain

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import javax.inject.Inject

class CopyNoteContentUseCase @Inject constructor() {

    fun perform(context: Context, note: Note) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("I'm a clip note", note.content)
        clipboard.setPrimaryClip(clip)
    }
}