package com.shamilovstas.text_encrypt.files

import android.os.Environment
import android.util.Log
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    @InternalFilesDir private val baseDir: File
) {

    companion object {
        val FILENAME_FROM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyymmdd_hhmmss")
    }

    fun createEncryptedFile(originalFilename: String): File {
        val filenameParts = originalFilename.split('.')
        val basename = filenameParts.first()
        val extension = filenameParts.drop(1).joinToString(separator = ".") { it }
        val dateTime = LocalDateTime.now()
        var filename = "${FILENAME_FROM_DATE_FORMATTER.format(dateTime)}_${originalFilename}.encf"
        var outputFile = File(baseDir, filename)

        var index = 0
        while (!outputFile.createNewFile()) {
            index ++
            filename = "${FILENAME_FROM_DATE_FORMATTER.format(dateTime)}_${basename}($index).$extension.enfc"
            outputFile = File(baseDir, filename)
        }

        return outputFile
    }

    fun createDecryptedFile(encryptedFilename: String): File {
        Log.d("FileRepository", "encryptedFilename: $encryptedFilename")

        val parts = encryptedFilename.split(".").dropLast(1)
        var suffix: String? = null

        val filename = parts.first()

        if (parts.size > 1) {
            suffix = parts.drop(1).joinToString(separator = ".") { it }
        }

        val temp = File.createTempFile(filename, suffix)
        return temp
    }
}