package com.shamilovstas.text_encrypt.files

import android.util.Log
import androidx.annotation.WorkerThread
import com.shamilovstas.text_encrypt.files.FileRepository.Companion.FILENAME_FROM_DATE_FORMATTER
import com.shamilovstas.text_encrypt.notes.domain.Note
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Singleton
class FileInteractor @Inject constructor(
    @InternalFilesDir private val baseDir: File
) {

    companion object {
        private const val TAG = "FileInteractor"
        const val NOTE_FILE_EXTENSION = "encn"
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
        Log.d(TAG, "export: millis: $millis\nmessage.size: ${messageDecoded.size}\nmessage: ${messageDecoded.toString(Charsets.UTF_8)}")
        outputStream.write(byteBuffer.array())
    }

    fun import(inputStream: InputStream) : Note {
        val dateByteData = ByteArray(Long.SIZE_BYTES)
        inputStream.read(dateByteData)
        val dateByteArray=  ByteBuffer.wrap(dateByteData)
        val millis = dateByteArray.getLong()
        Log.d(TAG, "import: millis: $millis")
        val array = ByteArray(Int.SIZE_BYTES)
        inputStream.read(array)
        val size = ByteBuffer.wrap(array).getInt()
        Log.d(TAG, "import: messageSize: $size")
        val messageByteArray = ByteArray(size)
        inputStream.read(messageByteArray)
        val content = Base64.encode(messageByteArray)
        Log.d(TAG, "import: message: ${messageByteArray.toString(Charsets.UTF_8)}")
        val note = Note(content = content, createdDate = OffsetDateTime.from(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())))
        return note
    }

    fun createExportFilename(note: Note): String {
        val createdDate = requireNotNull(note.createdDate)
        val name = FILENAME_FROM_DATE_FORMATTER.format(createdDate)
        return "$name.$NOTE_FILE_EXTENSION"
    }
}