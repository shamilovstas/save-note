package com.shamilovstas.text_encrypt.files

import androidx.annotation.WorkerThread
import com.shamilovstas.text_encrypt.notes.domain.Note
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.time.Instant
import java.time.OffsetDateTime
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

    fun import(inputStream: InputStream) : Note {
        val dateByteData = ByteArray(Long.SIZE_BYTES)
        inputStream.read(dateByteData)
        val dateByteArray=  ByteBuffer.wrap(dateByteData)
        val millis = dateByteArray.getLong()

        val array = ByteArray(Int.SIZE_BYTES)
        inputStream.read(array, Long.SIZE_BYTES, Int.SIZE_BYTES)
        val size = ByteBuffer.wrap(array).getInt()
        val messageByteArray = ByteArray(size)
        inputStream.read(messageByteArray, Long.SIZE_BYTES + Int.SIZE_BYTES, size)
        val content = Base64.encode(messageByteArray)

        val note = Note(content = content, createdDate = OffsetDateTime.from(Instant.ofEpochMilli(millis)))
        return note
    }

    fun createExportFilename(note: Note): String {
        val createdDate = requireNotNull(note.createdDate)
        val name = FILENAME_FROM_DATE_FORMATTER.format(createdDate)
        val extension = ".encn"
        return name + extension
    }
}