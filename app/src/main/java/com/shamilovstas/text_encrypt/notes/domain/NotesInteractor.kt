package com.shamilovstas.text_encrypt.notes.domain

import com.shamilovstas.text_encrypt.TextEncryptor
import com.shamilovstas.text_encrypt.notes.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesInteractor @Inject constructor(
    private val encryptor: TextEncryptor,
    private val repository: NotesRepository
) {

    suspend fun save(note: Note, password: String): Note {
        if (note.content.isNullOrEmpty()) {
            throw ClearTextIsEmpty()
        }

        val encrypted = encryptor.encrypt(note.content, password)
        val note = note.copy(content = encrypted, createdDate = OffsetDateTime.now())
        val entity = note.toEntity()
        repository.saveNote(entity)
        return note
    }

    fun decrypt(note: Note, password: String): Note {
        val decryptedContent = encryptor.decrypt(note.content, password)
        val note = note.copy(content = decryptedContent)
        return note
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
}

class ClearTextIsEmpty : IllegalArgumentException("Clear text is empty")