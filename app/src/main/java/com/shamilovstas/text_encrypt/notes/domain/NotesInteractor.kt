package com.shamilovstas.text_encrypt.notes.domain

import com.shamilovstas.text_encrypt.notes.repository.AttachmentStorageRepository
import com.shamilovstas.text_encrypt.notes.repository.NotesRepository
import com.shamilovstas.text_encrypt.notes.repository.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesInteractor @Inject constructor(
    private val encryptor: TextEncryptor,
    private val repository: NotesRepository,
    private val attachmentStorageRepository: AttachmentStorageRepository
) {

    suspend fun save(note: Note, password: String? = null): Note {
        if (note.content.isEmpty()) {
            throw ClearTextIsEmpty()
        }

        var title = note.title
        var content = note.content
        var attachments = note.attachments
        val date = note.createdDate ?: OffsetDateTime.now()

        if (password != null) {
            if (title.isNotEmpty()) {
                title = encryptor.encrypt(title, password)
            }
            content = encryptor.encrypt(note.content, password)
        }
        val encryptedNote = repository.saveNote(note.copy(title = title, content = content, createdDate = date))

        if (password != null) {
            attachments = attachmentStorageRepository.encryptNoteAttachments(encryptedNote, password)
        }
        repository.saveAttachments(attachments)

        return encryptedNote.copy(attachments = attachments)
    }

    suspend fun markShared(note: Note) {
        repository.markShared(note)
    }

    suspend fun decrypt(note: Note, password: String): Note {
        var title = note.title
        if (note.title.isNotEmpty()) {
            title = encryptor.decrypt(title, password)
        }
        val decryptedContent = encryptor.decrypt(note.content, password)

        val attachments = attachmentStorageRepository.decryptAttachments(note, password)
        return note.copy(title = title, content = decryptedContent, attachments = attachments)
    }

    suspend fun getNote(id: Long): Note {
        val noteEntity = repository.getNoteById(id)
        return noteEntity.toModel()
    }

    fun getAllNotes(): Flow<List<Note>> {
        return repository.getAllNotes()
            .map { it.map { it.toModel() } }
    }

    suspend fun deleteNote(note: Note) {
        repository.deleteNote(note)
    }
}

class ClearTextIsEmpty : IllegalArgumentException("Clear text is empty")