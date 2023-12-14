package com.shamilovstas.text_encrypt.notes.compose

import com.shamilovstas.text_encrypt.notes.domain.Note

data class ComposeNoteScreenState(
    val note: Note = Note(content = "", description = ""),
    override val cipherState: CipherState = CipherState.Encrypted,
    val isDecryptionPossible: Boolean = false,
    val previousPassword: String? = null,
) : CipherScreenState
