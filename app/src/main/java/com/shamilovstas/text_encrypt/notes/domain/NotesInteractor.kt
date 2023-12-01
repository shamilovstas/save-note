package com.shamilovstas.text_encrypt.notes.domain

import com.shamilovstas.text_encrypt.TextEncryptor
import com.shamilovstas.text_encrypt.notes.repository.NotesRepository
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesInteractor @Inject constructor(
    private val encryptor: TextEncryptor,
    private val repository: NotesRepository
) {

    suspend fun save(text: String?, password: String): Note {
        if (text.isNullOrEmpty()) {
            throw ClearTextIsEmpty()
        }

        val encrypted = encryptor.encrypt(text, password)
        val note = Note(content = encrypted, createdDate = OffsetDateTime.now())
        val entity = note.toEntity()
        repository.saveNote(entity)
        return note
    }
}

class ClearTextIsEmpty : IllegalArgumentException("Clear text is empty")