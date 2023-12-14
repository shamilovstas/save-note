package com.shamilovstas.text_encrypt.files

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

    fun createOutputFile(originalFilename: String): File {
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
}