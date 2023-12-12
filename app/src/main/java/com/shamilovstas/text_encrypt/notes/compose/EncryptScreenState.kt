package com.shamilovstas.text_encrypt.notes.compose

import com.shamilovstas.text_encrypt.notes.domain.Note

data class EncryptScreenState(
    val note: Note = Note(),
    val previousPassword: String? = null,
    override val cipherState: CipherState = CipherState.Decrypted
): CipherScreenState
