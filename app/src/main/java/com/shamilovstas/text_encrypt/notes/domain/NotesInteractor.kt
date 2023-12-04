package com.shamilovstas.text_encrypt.notes.domain

import com.shamilovstas.text_encrypt.TextEncryptor
import com.shamilovstas.text_encrypt.notes.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesInteractor @Inject constructor(
    private val encryptor: TextEncryptor,
    private val repository: NotesRepository
) {

    suspend fun save(note: Note, password: String): Note {
        if (note.content.isEmpty()) {
            throw ClearTextIsEmpty()
        }

        val encrypted = encryptor.encrypt(note.content, password)

        val date = note.createdDate ?: OffsetDateTime.now()
        val newNote = note.copy(content = encrypted, createdDate = date)
        val entity = newNote.toEntity()
        repository.saveNote(entity)
        return newNote
    }

    suspend fun saveEncrypted(note: Note): Note {
        if (note.content.isEmpty()) {
            throw ClearTextIsEmpty()
        }

        val date = note.createdDate ?: OffsetDateTime.now()
        val newNote = note.copy(createdDate = date)
        val entity = newNote.toEntity()
        repository.saveNote(entity)
        return newNote
    }

    fun decrypt(note: Note, password: String): Note {
        val decryptedContent = encryptor.decrypt(note.content, password)
        val newNote = note.copy(content = decryptedContent)
        return newNote
    }

    suspend fun getNote(id: Int): Note {
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