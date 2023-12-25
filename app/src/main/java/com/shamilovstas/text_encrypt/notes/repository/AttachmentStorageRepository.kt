package com.shamilovstas.text_encrypt.notes.repository

import android.content.ContentResolver
import android.net.Uri
import com.shamilovstas.text_encrypt.files.InternalFilesDir
import com.shamilovstas.text_encrypt.files.TemporaryFilesDir
import com.shamilovstas.text_encrypt.notes.domain.Attachment
import com.shamilovstas.text_encrypt.notes.domain.FileEncryptor
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.utils.createUniqueFile
import com.shamilovstas.text_encrypt.utils.digest
import com.shamilovstas.text_encrypt.utils.getFilename
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalStdlibApi::class)
@Singleton
class AttachmentStorageRepository @Inject constructor(
    @InternalFilesDir private val baseDir: File,
    private val attachmentStorageDao: AttachmentStorageDao,
    private val fileEncryptor: FileEncryptor,
    @TemporaryFilesDir private val cacheDir: File,
    private val contentResolver: ContentResolver
) {

    companion object {
        val FILENAME_FROM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyymmdd_hhmmss")
        private const val TEMP_DIR_NAME_LENGTH = 36
    }

    suspend fun deleteAttachments(note: Note) {
        coroutineScope {
            val storage = attachmentStorageDao.findAttachmentStorageForNote(note.id) ?: return@coroutineScope
            val dir = File(baseDir, storage.relativePath)
            if (dir.exists()) {
                suspendCoroutine {
                    it.resume(dir.deleteRecursively())
                }
            }
        }
    }

    suspend fun createNoteDir(baseDir: File, note: Note): File {
        val dir = createUniqueFile(baseDir) { digest(TEMP_DIR_NAME_LENGTH) }

        val dirname = dir.name
        if (!dir.mkdirs()) {
            throw IllegalStateException("Couldn't create directory with name ${dir.path}")
        }

        val attachmentStorageEntity = AttachmentStorageEntity(relativePath = dirname, noteId = note.id)
        attachmentStorageDao.insertAttachmentStorage(attachmentStorageEntity)
        return dir
    }

    suspend fun createAttachmentsDir(note: Note): File {
        return createNoteDir(baseDir, note)
    }

    suspend fun createAttachmentsTempDir(): File {
        val dir = createUniqueFile(cacheDir) { digest(TEMP_DIR_NAME_LENGTH) }

        if (!dir.mkdirs()) {
            throw IllegalStateException("Couldn't create temp directory with name ${dir.path}")
        }
        return dir
    }

    suspend fun encryptNoteAttachments(note: Note, password: String): List<Attachment> {
        var attachmentsDir = getAttachmentDir(note)
        if (attachmentsDir == null) {
            attachmentsDir = createAttachmentsDir(note)
        }

        deleteExistingFiles(attachmentsDir)

        val encryptedUris = mutableListOf<Attachment>()
        for (attachment in note.attachments) {

            val filename = attachment.filename
            val outputFile = createEncryptedFile(attachmentsDir, filename)

            contentResolver.openInputStream(attachment.uri).use { inputStream ->
                requireNotNull(inputStream)
                FileOutputStream(outputFile).use { outputStream ->
                    fileEncryptor.encrypt(inputStream, outputStream, password)
                }
            }
            val encryptedFileUri = Uri.fromFile(outputFile)
            encryptedUris.add(attachment.copy(uri = encryptedFileUri, isEncrypted = true))
        }
        return encryptedUris
    }

    //TODO add parallel computation
    suspend fun decryptAttachments(note: Note, password: String): List<Attachment> {

        val encryptedUris = mutableListOf<Attachment>()
        val baseDir = createAttachmentsTempDir()
        for (attachment in note.attachments) {
            val filename = attachment.filename
            val outputFile = File(baseDir, filename)

            contentResolver.openInputStream(attachment.uri).use { inputStream ->
                requireNotNull(inputStream)
                FileOutputStream(outputFile).use { outputStream ->
                    fileEncryptor.decrypt(inputStream, outputStream, password)
                }
            }
            val encryptedFileUri = Uri.fromFile(outputFile)
            encryptedUris.add(attachment.copy(uri = encryptedFileUri, isEncrypted = false))
        }
        return encryptedUris
    }

    private fun deleteExistingFiles(dir: File) {
        dir.walkTopDown().drop(1).fold(true) { res, it -> (it.delete() || !it.exists()) && res }
    }
    suspend fun getAttachmentDir(note: Note): File? {
        val attachmentStorageEntity =
            attachmentStorageDao.findAttachmentStorageForNote(note.id) ?: return null

        val dir = File(baseDir, attachmentStorageEntity.relativePath)
        return dir
    }

    private fun createEncryptedFile(baseDir: File, originalFilename: String): File {
        return File(baseDir, "$originalFilename.encf")
    }
}