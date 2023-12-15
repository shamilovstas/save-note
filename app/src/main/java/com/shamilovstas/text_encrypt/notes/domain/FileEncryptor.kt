package com.shamilovstas.text_encrypt.notes.domain

import android.content.ContentResolver
import android.net.Uri
import com.shamilovstas.text_encrypt.files.FileRepository
import com.shamilovstas.text_encrypt.utils.getFilename
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileEncryptor @Inject constructor(
    private val contentResolver: ContentResolver,
    private val utils: EncryptionUtils,
    private val fileRepository: FileRepository
) {

    private val BUFFER_LENGTH = 1024

    fun encrypt(uri: Uri, password: String): Uri {
        val filename = uri.getFilename(contentResolver)
        val outputFile = fileRepository.createEncryptedFile(filename)

        val salt = utils.generateSalt()
        val key = utils.generateKey(password, salt)
        val cipher = utils.cipher
        val iv = utils.generateIv()
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)


        contentResolver.openInputStream(uri).use { inputStream ->
            requireNotNull(inputStream)
            FileOutputStream(outputFile).use { outputStream ->

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
        }
        return Uri.fromFile(outputFile)
    }

    fun decrypt(uri: Uri, password: String): Uri {
        val filename = uri.getFilename(contentResolver)
        val outputFile = fileRepository.createDecryptedFile(filename)
        contentResolver.openInputStream(uri).use { inputStream ->
            requireNotNull(inputStream)
            FileOutputStream(outputFile).use { outputStream ->

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
        return Uri.fromFile(outputFile)
    }
}