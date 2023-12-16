package com.shamilovstas.text_encrypt.notes.repository

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val notesDao: NotesDao
) {

    suspend fun saveNote(entity: NoteWithAttachments): NoteWithAttachments {
        return notesDao.insertNote(entity)
    }

    fun getAllNotes(): Flow<List<NoteWithAttachments>> {
        return notesDao.getAllNotes();
    }

    fun updateNote(note: NoteEntity) {
        notesDao.update(note)
    }

    fun deleteNote(note: NoteEntity) {
        notesDao.delete(note)
    }

    suspend fun getNoteById(noteId: Long): NoteWithAttachments {
        return notesDao.getNoteById(noteId)
    }
}