package com.shamilovstas.text_encrypt.notes.domain

import android.util.Log
import com.shamilovstas.text_encrypt.notes.repository.NoteWithAttachments
import com.shamilovstas.text_encrypt.notes.repository.NotesRepository
import com.shamilovstas.text_encrypt.notes.repository.toEntity
import com.shamilovstas.text_encrypt.notes.repository.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesInteractor @Inject constructor(
    private val encryptor: TextEncryptor,
    private val fileEncryptor: FileEncryptor,
    private val repository: NotesRepository
) {

    suspend fun save(note: Note, password: String? = null): Note {
        if (note.content.isEmpty()) {
            throw ClearTextIsEmpty()
        }

        var content = note.content
        var attachments = note.attachments
        if (password != null) {
            Log.d("NotesInteractor", "encrypting note with description: '${note.description}'...")
            content = encryptor.encrypt(note.content, password)
            attachments = encryptAttachments(note.attachments, password)
        }


        val date = note.createdDate ?: OffsetDateTime.now()
        val newNote = note.copy(content = content, attachments = attachments, createdDate = date)
        val noteEntity = newNote.toEntity()
        val attachmentEntities = newNote.attachments.map { it.toEntity() }
        val groupedEntity = NoteWithAttachments(noteEntity, attachmentEntities)
        return repository.saveNote(groupedEntity).toModel()
    }


    //TODO add parallel computation
    private fun encryptAttachments(attachments: List<Attachment>, password: String): List<Attachment> {
        val encryptedUris = mutableListOf<Attachment>()
        for (attachment in attachments) {
            Log.d("NotesInteractor", "encrypting attachment '${attachment.filename}'...")
            val encryptedFileUri = fileEncryptor.encrypt(attachment.uri, password)
            encryptedUris.add(attachment.copy(uri = encryptedFileUri, isEncrypted = true))
        }
        return encryptedUris
    }

    //TODO add parallel computation
    private fun decryptAttachments(attachments: List<Attachment>, password: String): List<Attachment> {
        val encryptedUris = mutableListOf<Attachment>()
        for (attachment in attachments) {
            Log.d("NotesInteractor", "decrypting attachment '${attachment.filename}'...")
            val encryptedFileUri = fileEncryptor.decrypt(attachment.uri, password)
            encryptedUris.add(attachment.copy(uri = encryptedFileUri, isEncrypted = false))
        }
        return encryptedUris
    }

    fun decrypt(note: Note, password: String): Note {
        val decryptedContent = encryptor.decrypt(note.content, password)

        val attachments = decryptAttachments(note.attachments, password)
        val newNote = note.copy(content = decryptedContent, attachments = attachments)
        return newNote
    }

    suspend fun getNote(id: Long): Note {
        Log.d("NotesInteractor", "querying note with id $id")
        val noteEntity = repository.getNoteById(id)
        val note = noteEntity.toModel()
        return note
    }

    suspend fun getAllNotes(): Flow<List<Note>> {
        return repository.getAllNotes()
            .map { it.map { it.toModel() } }
    }

    suspend fun deleteNote(note: Note) {
        val entity = note.toEntity()
        repository.deleteNote(entity)
    }
}

class ClearTextIsEmpty : IllegalArgumentException("Clear text is empty")