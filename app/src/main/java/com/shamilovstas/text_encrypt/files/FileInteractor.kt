package com.shamilovstas.text_encrypt.files

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.WorkerThread
import com.shamilovstas.text_encrypt.notes.domain.Attachment
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.repository.AttachmentStorageRepository
import com.shamilovstas.text_encrypt.notes.repository.AttachmentStorageRepository.Companion.FILENAME_FROM_DATE_FORMATTER
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
import kotlin.coroutines.suspendCoroutine
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
        private const val CONTENTS_FILE = "__contents__"
    }

    @WorkerThread
    suspend fun export(note: Note, outputStream: OutputStream) {
        suspendCoroutine {
            val zipOutputStream = ZipOutputStream(outputStream)
            exportNoteContent(note, zipOutputStream)

            val sanitizedAttachments = sanitizeAttachments(note)

            for (attachment in sanitizedAttachments) {
                exportAttachment(attachment, zipOutputStream)
            }
            zipOutputStream.close()
            it.resumeWith(Result.success(Unit))
        }
    }

    private fun sanitizeAttachments(note: Note): List<Attachment> {
        val sanitizedAttachments = note.attachments.toMutableList()
        val hasContentsFilename = sanitizedAttachments.find { it.filename == CONTENTS_FILE }
        if (hasContentsFilename != null) {
            sanitizedAttachments.remove(hasContentsFilename)
            val sanitizedFilename = "_${hasContentsFilename.filename}"
            val uniqueSanitizedFilename = getUniqueFilename(sanitizedFilename, sanitizedAttachments.map { it.filename }.toSet())
            val renamedAttachment = hasContentsFilename.copy(filename = uniqueSanitizedFilename)
            sanitizedAttachments.add(renamedAttachment)
        }
        return sanitizedAttachments
    }

    fun getUniqueFilename(original: String, existingFilenames: Set<String>): String {
        val filenameParts = original.split('.')
        val basename = filenameParts.first()
        val originalExtension = filenameParts.drop(1).joinToString(separator = ".") { it }
        var index = 0

        var newFilename: String
        do {
            newFilename = if (index == 0) {
                original
            } else {
                val extension = if (originalExtension.isNotEmpty()) {
                    ".$originalExtension"
                } else {
                    ""
                }
                "${basename}($index)$extension"
            }
            index ++
        } while (existingFilenames.contains(newFilename))

        return newFilename
    }

    private fun exportAttachment(attachment: Attachment, zipOutputStream: ZipOutputStream) {
        val zipEntry = ZipEntry(attachment.filename)
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

        val messageSize = Long.SIZE_BYTES + Int.SIZE_BYTES + messageDecoded.size + Int.SIZE_BYTES + note.description.length
        val byteBuffer = ByteBuffer.allocate(messageSize)

        byteBuffer.putLong(millis)
        byteBuffer.putInt(messageDecoded.size)
        byteBuffer.put(messageDecoded)
        byteBuffer.putInt(note.description.length)
        byteBuffer.put(note.description.toByteArray(Charsets.UTF_8))
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
        val file = File(tempMediaDir, name)

        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.use {
            zipInputStream.copyTo(it)
        }
        val attachment = Attachment(noteId = 0, uri = Uri.fromFile(file), filename = name, isEncrypted = true)
        return attachment
    }

    private fun importNoteContents(zipInputStream: ZipInputStream): Note {
        val dateBuffer = ByteArray(Long.SIZE_BYTES)
        zipInputStream.read(dateBuffer)
        val dateByteArray=  ByteBuffer.wrap(dateBuffer)
        val millis = dateByteArray.getLong()

        val noteLengthBuffer = ByteArray(Int.SIZE_BYTES)
        zipInputStream.read(noteLengthBuffer)
        val noteLength = ByteBuffer.wrap(noteLengthBuffer).getInt()
        val messageByteArray = ByteArray(noteLength)
        zipInputStream.read(messageByteArray)
        val content = Base64.encode(messageByteArray)

        val descriptionLengthBuffer = ByteArray(Int.SIZE_BYTES)
        zipInputStream.read(descriptionLengthBuffer)
        val descriptionLength = ByteBuffer.wrap(descriptionLengthBuffer).getInt()
        val descriptionBuffer = ByteArray(descriptionLength)
        zipInputStream.read(descriptionBuffer)
        val description = descriptionBuffer.toString(Charsets.UTF_8)
        val note = Note(content = content, description = description, createdDate = OffsetDateTime.from(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())))
        return note
    }

    fun createExportFilename(note: Note): String {
        val createdDate = requireNotNull(note.createdDate)
        val name = FILENAME_FROM_DATE_FORMATTER.format(createdDate)
        return "$name.$NOTE_FILE_EXTENSION"
    }
}