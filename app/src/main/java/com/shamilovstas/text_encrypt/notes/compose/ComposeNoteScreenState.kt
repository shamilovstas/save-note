package com.shamilovstas.text_encrypt.notes.compose

data class ComposeNoteScreenState(
    val content: String? = null,
    val description: String = "",
    override val cipherState: CipherState = CipherState.Encrypted,
    val isDecryptionPossible: Boolean = false,
    val previousPassword: String? = null,
    val existingId: Int = 0
) : CipherScreenState
