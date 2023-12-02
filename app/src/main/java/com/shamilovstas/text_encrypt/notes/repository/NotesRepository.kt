package com.shamilovstas.text_encrypt.notes.repository

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val notesDao: NotesDao
) {
    suspend fun saveNote(entity: NoteEntity) {
        notesDao.insert(entity)
    }
    fun getAllNotes(): Flow<List<NoteEntity>> {
        return notesDao.getAllNotes();
    }

    fun updateNote(note: NoteEntity) {
        notesDao.update(note)
    }

    fun deleteNote(note: NoteEntity) {
        notesDao.delete(note)
    }

    suspend fun getNoteById(noteId: Int): NoteEntity {
        return notesDao.getNoteById(noteId)
    }
}