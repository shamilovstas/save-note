package com.shamilovstas.text_encrypt.notes.domain

import android.net.Uri

data class Attachment(
    val uri: Uri,
    val filename: String,
    val isEncrypted: Boolean = false
)