package com.shamilovstas.text_encrypt.notes.domain

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileEncryptor @Inject constructor(
    private val utils: EncryptionUtils,
) {

    private val BUFFER_LENGTH = 1024

    fun encrypt(inputStream: InputStream, outputStream: OutputStream, password: String) {

        val salt = utils.generateSalt()
        val key = utils.generateKey(password, salt)
        val cipher = utils.cipher
        val iv = utils.generateIv()
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)

        outputStream.write(salt + iv.iv)

        val buffer = ByteArray(BUFFER_LENGTH)
        var readBytes = 0

        do {
            readBytes = inputStream.read(buffer)

            if (readBytes != -1) {
                val encryptedBytes = cipher.update(buffer, 0, readBytes)
                outputStream.write(encryptedBytes)
            }
        } while (readBytes != -1)

        outputStream.write(cipher.doFinal())
    }

    fun decrypt(inputStream: InputStream, outputStream: OutputStream, password: String) {
        val saltBuffer = ByteArray(EncryptionUtils.SALT_LENGTH)
        inputStream.read(saltBuffer)

        val ivBuffer = ByteArray(EncryptionUtils.IV_LENGTH)
        inputStream.read(ivBuffer)

        val salt = saltBuffer
        val key = utils.generateKey(password, salt)
        val cipher = utils.cipher
        val iv = IvParameterSpec(ivBuffer)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val buffer = ByteArray(BUFFER_LENGTH)
        var readBytes = 0

        do {
            readBytes = inputStream.read(buffer)
            if (readBytes != -1) {
                val encryptedBytes = cipher.update(buffer, 0, readBytes)
                outputStream.write(encryptedBytes)
            }
        } while (readBytes != -1)

        outputStream.write(cipher.doFinal()) // bad decrypt
    }
}