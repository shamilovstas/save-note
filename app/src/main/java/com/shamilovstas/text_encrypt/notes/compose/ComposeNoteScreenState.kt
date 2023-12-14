package com.shamilovstas.text_encrypt.notes.compose

import android.net.Uri
import com.shamilovstas.text_encrypt.notes.domain.Attachment

data class ComposeNoteScreenState(
    val content: String? = null,
    val description: String = "",
    override val cipherState: CipherState = CipherState.Encrypted,
    val isDecryptionPossible: Boolean = false,
    val previousPassword: String? = null,
    val existingId: Int = 0,
    val attachments: List<Attachment> = listOf()
) : CipherScreenState
