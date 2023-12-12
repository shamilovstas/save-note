package com.shamilovstas.text_encrypt.notes.compose

interface CipherScreenState {
    val cipherState: CipherState
}
enum class CipherState {
    Encrypted, Decrypted
}