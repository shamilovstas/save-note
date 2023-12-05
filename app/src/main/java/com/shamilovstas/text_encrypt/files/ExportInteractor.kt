package com.shamilovstas.text_encrypt.files

import androidx.annotation.WorkerThread
import com.shamilovstas.text_encrypt.notes.domain.Note
import java.io.File
import java.io.OutputStream
import java.nio.ByteBuffer
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Singleton
class ExportInteractor @Inject constructor(
    @InternalFilesDir private val baseDir: File
) {

    companion object {
        private val FILENAME_FROM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyymmdd_hhmmss")
    }

    @WorkerThread
    fun export(note: Note, outputStream: OutputStream) {
        val millis = note.createdDate?.toInstant()?.toEpochMilli() ?: 0L
        val messageDecoded = Base64.decode(note.content)

        val messageSize = Long.SIZE_BYTES + Int.SIZE_BYTES + messageDecoded.size
        val byteBuffer = ByteBuffer.allocate(messageSize)

        byteBuffer.putLong(millis)
        byteBuffer.putInt(messageDecoded.size)
        byteBuffer.put(messageDecoded)
        outputStream.write(byteBuffer.array())
    }

    fun createExportFilename(note: Note): String {
        val createdDate = requireNotNull(note.createdDate)
        val name = FILENAME_FROM_DATE_FORMATTER.format(createdDate)
        val extension = ".encn"
        return name + extension
    }
}