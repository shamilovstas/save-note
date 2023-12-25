package com.shamilovstas.text_encrypt.notes.domain

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileEncryptor @Inject constructor(
    private val utils: EncryptionUtils,
) {

    fun encrypt(inputStream: InputStream, outputStream: OutputStream, password: String) {

        val cipher = utils.cipher
        val salt = utils.generateRandom(cipher.blockSize)
        val key = utils.generateKey(password, salt)
        val iv = utils.generateIv(cipher.blockSize)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)


        outputStream.write(salt + iv.iv)
        val cipherOutputStream = CipherOutputStream(outputStream, cipher)
        inputStream.copyTo(cipherOutputStream)
        cipherOutputStream.close()
    }

    fun decrypt(inputStream: InputStream, outputStream: OutputStream, password: String) {
        val cipher = utils.cipher

        val saltBuffer = ByteArray(cipher.blockSize)
        inputStream.read(saltBuffer)
        val ivBuffer = ByteArray(cipher.blockSize)
        inputStream.read(ivBuffer)

        val key = utils.generateKey(password, saltBuffer)
        val iv = IvParameterSpec(ivBuffer)

        cipher.init(Cipher.DECRYPT_MODE, key, iv)

        val cipherOutputStream = CipherOutputStream(outputStream, cipher)
        inputStream.copyTo(cipherOutputStream)
        cipherOutputStream.close()
    }
}