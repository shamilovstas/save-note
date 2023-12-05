package com.shamilovstas.text_encrypt.files

import androidx.annotation.WorkerThread
import com.shamilovstas.text_encrypt.notes.domain.Note
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportInteractor @Inject constructor(
    @InternalFilesDir private val baseDir: File
) {

    companion object {
        private val FILENAME_FROM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyymmdd_hhmmss")
    }

    @WorkerThread
    fun export(note: Note, outputStream: OutputStream) {
        val serializedDate = DateTimeFormatter.ISO_DATE_TIME.format(note.createdDate)
        val content = note.content
        val writer = outputStream.writer()
        writer.write(serializedDate)
        writer.write(content)
        writer.close()
    }

    fun createExportFilename(note: Note): String {
        val createdDate = requireNotNull(note.createdDate)
        val name = FILENAME_FROM_DATE_FORMATTER.format(createdDate)
        val extension = ".encn"
        return name + extension
    }
}