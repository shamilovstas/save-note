package com.shamilovstas.text_encrypt.notes.domain

import com.shamilovstas.text_encrypt.notes.domain.exception.PasswordIsEmpty
import com.shamilovstas.text_encrypt.utils.loadFile
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.nio.file.Files
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class FileEncryptorTest {

    private val fileEncryptor = FileEncryptor(EncryptionUtils())

    @Test
    fun `should encrypted file be not empty when a file is encrypted`() {
        val inputFile = loadFile("cat_unitTest.png").openStream()
        val outputFile = Files.createTempFile("cat_encrypted", ".png.encrypted")
        val password = "myPassword123"
        fileEncryptor.encrypt(inputFile!!, outputFile.outputStream(), password)
        assertTrue(Files.size(outputFile) > 0)
    }

    @Test
    fun `should decrypted file be not empty when a file is decrypted`() {
        val inputFile = loadFile("cat_unitTest.png").openStream()
        val encryptedFile = Files.createTempFile("cat_encrypted", ".png.encrypted")
        val password = "myPassword123"
        fileEncryptor.encrypt(inputFile!!, encryptedFile.outputStream(), password)
        val decryptedFile = Files.createTempFile("cat_decrypted", ".png")
        fileEncryptor.decrypt(encryptedFile.inputStream(), decryptedFile.outputStream(), password)
        assertTrue(Files.size(decryptedFile) > 0)
    }

    @Test(expected = PasswordIsEmpty::class)
    fun `should throw an exception if password is empty`() {
        val inputFile = loadFile("cat_unitTest.png").openStream()
        val outputFile = Files.createTempFile("cat_encrypted", ".png.encrypted")
        val password = ""
        fileEncryptor.encrypt(inputFile!!, outputFile.outputStream(), password)
    }
}