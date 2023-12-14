package com.shamilovstas.text_encrypt.notes.domain

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@Singleton
class TextEncryptor @Inject constructor(
    private val utils: EncryptionUtils
) {


    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(data: String, password: String): String {
        if (data.isEmpty()) throw IllegalArgumentException("Clear text cannot be empty")
        val salt = utils.generateSalt()
        val key = utils.generateKey(password, salt)

        val cipher = utils.cipher

        val iv = utils.generateIv()
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val ciphertext: ByteArray = cipher.doFinal(data.toByteArray())

        val message = salt + iv.iv + ciphertext

        val base64Encoded = Base64.encode(message)
        return base64Encoded
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(data: String, password: String): String {
        val encText2 = Base64.decode(data)
        checkMessageSize(encText2.size)

        val cipher = utils.cipher

        val salt = encText2.slice(0 until EncryptionUtils.SALT_LENGTH).toByteArray()
        var curIndex: Int = EncryptionUtils.SALT_LENGTH
        val ivBytes = encText2.slice(curIndex until curIndex + EncryptionUtils.IV_LENGTH).toByteArray()
        curIndex += EncryptionUtils.IV_LENGTH
        val key = utils.generateKey(password, salt)
        val iv = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)

        val message = encText2.slice(curIndex until encText2.size).toByteArray()
        val ciphertext: ByteArray = cipher.doFinal(message)
        return ciphertext.toString(Charsets.UTF_8)
    }

    private fun checkMessageSize(size: Int) {
        val minSize = EncryptionUtils.MIN_BLOCK_SIZE + EncryptionUtils.SALT_LENGTH + EncryptionUtils.IV_LENGTH
        if (size < minSize) throw EncryptedMessageMalformed(size, minSize)
    }

}
