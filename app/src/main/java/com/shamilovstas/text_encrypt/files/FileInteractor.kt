package com.shamilovstas.text_encrypt.files

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.WorkerThread
import com.shamilovstas.text_encrypt.notes.domain.Attachment
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.repository.AttachmentStorageRepository
import com.shamilovstas.text_encrypt.notes.repository.AttachmentStorageRepository.Companion.FILENAME_FROM_DATE_FORMATTER
import com.shamilovstas.text_encrypt.utils.createUniqueFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Singleton
class FileInteractor @Inject constructor(
    private val contentResolver: ContentResolver,
    private val attachmentStorageRepository: AttachmentStorageRepository
) {

    companion object {
        private const val TAG = "FileInteractor"
        const val NOTE_FILE_EXTENSION = "encn"
        private const val CONTENTS_FILE = "contents"
    }

    @WorkerThread
    fun export(note: Note, outputStream: OutputStream) {
        val zipOutputStream = ZipOutputStream(outputStream)
        exportNoteContent(note, zipOutputStream)

        for (attachment in note.attachments) {
            exportAttachment(attachment, zipOutputStream)
        }
        zipOutputStream.close()
    }

    private fun exportAttachment(attachment: Attachment, zipOutputStream: ZipOutputStream) {
        val zipEntry = ZipEntry(attachment.filename) // TODO handle name collision with 'contents' file
        zipOutputStream.putNextEntry(zipEntry)
        contentResolver.openInputStream(attachment.uri).use {
            requireNotNull(it).copyTo(zipOutputStream)
        }
        zipOutputStream.closeEntry()
    }

    private fun exportNoteContent(note: Note, zipOutputStream: ZipOutputStream) {
        val zipEntry = ZipEntry(CONTENTS_FILE)
        zipOutputStream.putNextEntry(zipEntry)

        val millis = note.createdDate?.toInstant()?.toEpochMilli() ?: 0L
        val messageDecoded = Base64.decode(note.content)

        val messageSize = Long.SIZE_BYTES + Int.SIZE_BYTES + messageDecoded.size
        val byteBuffer = ByteBuffer.allocate(messageSize)

        byteBuffer.putLong(millis)
        byteBuffer.putInt(messageDecoded.size)
        byteBuffer.put(messageDecoded)
        zipOutputStream.write(byteBuffer.array())
        zipOutputStream.closeEntry()
    }

    suspend fun import(inputStream: InputStream) : Note {
        val zipInputStream = ZipInputStream(inputStream)

        var zipEntry: ZipEntry? = zipInputStream.nextEntry

        var note: Note? = null
        val attachments: MutableList<Attachment> = mutableListOf()

        val tempMediaDir = attachmentStorageRepository.createAttachmentsTempDir()
        while (zipEntry != null) {
            if (zipEntry.name == CONTENTS_FILE) {
                note = importNoteContents(zipInputStream)
            } else {
                val attachment = importAttachment(zipEntry, zipInputStream, tempMediaDir)
                attachments.add(attachment)
            }
            zipEntry = zipInputStream.nextEntry
        }

        zipInputStream.close()

        if (note == null) {
            throw UnknownNoteFiletype()
        }

        val importedNote = requireNotNull(note).copy(attachments = attachments)
        return importedNote
    }

    private fun importAttachment(zipEntry: ZipEntry, zipInputStream: ZipInputStream, tempMediaDir: File): Attachment {

        val name = zipEntry.name
        val file = createUniqueFile(tempMediaDir, name)

        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.use {
            zipInputStream.copyTo(it)
        }
        val attachment = Attachment(noteId = 0, uri = Uri.fromFile(file), filename = name, isEncrypted = true)
        return attachment
    }

    private fun importNoteContents(zipInputStream: ZipInputStream): Note {
        val dateByteData = ByteArray(Long.SIZE_BYTES)
        zipInputStream.read(dateByteData)
        val dateByteArray=  ByteBuffer.wrap(dateByteData)
        val millis = dateByteArray.getLong()
        val array = ByteArray(Int.SIZE_BYTES)
        zipInputStream.read(array)
        val size = ByteBuffer.wrap(array).getInt()
        val messageByteArray = ByteArray(size)
        zipInputStream.read(messageByteArray)
        val content = Base64.encode(messageByteArray)
        val note = Note(content = content, createdDate = OffsetDateTime.from(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())))
        return note
    }

    fun createExportFilename(note: Note): String {
        val createdDate = requireNotNull(note.createdDate)
        val name = FILENAME_FROM_DATE_FORMATTER.format(createdDate)
        return "$name.$NOTE_FILE_EXTENSION"
    }
}