package com.shamilovstas.text_encrypt.importdata

import com.shamilovstas.text_encrypt.notes.compose.CipherScreenState
import com.shamilovstas.text_encrypt.notes.compose.CipherState

data class ImportMessageScreenState(
    val content: String? = null,
    val description: String = "",
    override val cipherState: CipherState = CipherState.Encrypted,
    val isDecryptionPossible: Boolean = false
) : CipherScreenState
