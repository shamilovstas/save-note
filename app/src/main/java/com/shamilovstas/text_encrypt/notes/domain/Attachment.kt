package com.shamilovstas.text_encrypt.notes.domain

import android.net.Uri

data class Attachment(
    val id: Long = 0,
    val noteId: Long,
    val uri: Uri,
    val filename: String,
    val isEncrypted: Boolean = false
) {
    val isDecrypted = !isEncrypted
}