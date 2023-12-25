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
        val cipher = utils.cipher
        val salt = utils.generateRandom(cipher.blockSize)
        val key = utils.generateKey(password, salt)


        val iv = utils.generateIv(cipher.blockSize)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val ciphertext: ByteArray = cipher.doFinal(data.toByteArray())

        val message = salt + iv.iv + ciphertext

        return Base64.encode(message)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(data: String, password: String): String {
        val encText2 = Base64.decode(data)
        val cipher = utils.cipher
        checkMessageSize(encText2.size, cipher.blockSize)


        val salt = encText2.slice(0 until cipher.blockSize).toByteArray()
        var curIndex: Int = cipher.blockSize
        val ivBytes = encText2.slice(curIndex until curIndex + cipher.blockSize).toByteArray()
        curIndex += cipher.blockSize
        val key = utils.generateKey(password, salt)
        val iv = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)

        val message = encText2.slice(curIndex until encText2.size).toByteArray()
        val ciphertext: ByteArray = cipher.doFinal(message)
        return ciphertext.toString(Charsets.UTF_8)
    }

    private fun checkMessageSize(size: Int, blockSize: Int) {
        val minSize = EncryptionUtils.MIN_BLOCK_SIZE + blockSize * 2
        if (size < minSize) throw EncryptedMessageMalformed(size, minSize)
    }

}
