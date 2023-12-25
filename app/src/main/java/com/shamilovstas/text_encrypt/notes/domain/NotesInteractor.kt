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

        var content = note.content
        var attachments = note.attachments
        val date = note.createdDate ?: OffsetDateTime.now()

        if (password != null) {
            content = encryptor.encrypt(note.content, password)
        }
        val encryptedNote = repository.saveNote(note.copy(content = content, createdDate = date))

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
        val decryptedContent = encryptor.decrypt(note.content, password)

        val attachments = attachmentStorageRepository.decryptAttachments(note, password)
        val newNote = note.copy(content = decryptedContent, attachments = attachments)
        return newNote
    }

    suspend fun getNote(id: Long): Note {
        val noteEntity = repository.getNoteById(id)
        val note = noteEntity.toModel()
        return note
    }

    suspend fun getAllNotes(): Flow<List<Note>> {
        return repository.getAllNotes()
            .map { it.map { it.toModel() } }
    }

    suspend fun deleteNote(note: Note) {
        repository.deleteNote(note)
    }
}

class ClearTextIsEmpty : IllegalArgumentException("Clear text is empty")