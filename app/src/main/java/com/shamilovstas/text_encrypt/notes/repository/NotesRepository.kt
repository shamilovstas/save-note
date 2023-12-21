package com.shamilovstas.text_encrypt.notes.repository

import com.shamilovstas.text_encrypt.notes.domain.Attachment
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.domain.toEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val notesDao: NotesDao,
    private val attachmentStorageRepository: AttachmentStorageRepository
) {

    suspend fun saveNote(note: Note): Note {
        val noteEntity = note.toEntity()
        val id = notesDao.insertNote(noteEntity)

        val attachments = note.attachments.map { it.copy(noteId = id) }
        return note.copy(id = id, attachments = attachments)
    }

    suspend fun saveAttachments(attachments: List<Attachment>) {
        notesDao.insertAttachments(attachments.map { it.toEntity() })
    }

    fun getAllNotes(): Flow<List<NoteWithAttachments>> {
        return notesDao.getAllNotes();
    }

    suspend fun deleteNote(note: Note) {
        attachmentStorageRepository.deleteAttachments(note)
        val entity = note.toEntity()
        notesDao.delete(entity)
    }

    suspend fun getNoteById(noteId: Long): NoteWithAttachments {
        return notesDao.getNoteById(noteId)
    }
}