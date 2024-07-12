package com.shamilovstas.text_encrypt.notes.domain

import com.shamilovstas.text_encrypt.notes.domain.exception.PasswordIsEmpty
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class TextEncryptorTest {
    private val encryptor = TextEncryptor(EncryptionUtils())

    @Test
    fun `should have initial text after encryption and decryption`() {
        val text = "This is my text"
        val password = "myPassword123"

        val encryptedText = encryptor.encrypt(text, password)
        val decryptedText = encryptor.decrypt(encryptedText, password)

        assertEquals(text, decryptedText)
    }

    @Test(expected = ClearTextIsEmpty::class)
    fun `should throw an exception if clear text is empty`() {
        val emptyText = ""
        val password = "myPassword123"

        encryptor.encrypt(emptyText, password)
    }

    @Test(expected = PasswordIsEmpty::class)
    fun `should throw an exception if password is empty`() {
        val emptyText = "This is my text"
        val password = ""

        encryptor.encrypt(emptyText, password)
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test(expected = EncryptedMessageMalformed::class)
    fun `should throw an exception if encrypted text is malformed`() {
        val malformedEncryptedText = "a" // encrypted text is always longer than 'EncryptionUtils.MIN_BLOCK_SIZE + blockSize * 2'
        val password = "myPassword123"
        encryptor.decrypt(Base64.encode(malformedEncryptedText.toByteArray()), password)
    }
}