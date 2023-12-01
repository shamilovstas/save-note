package com.shamilovstas.text_encrypt.notes.repository

import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val notesDao: NotesDao
) {


    fun getAllNotes(): List<NoteEntity> {
        return notesDao.getAllNotes();
    }

    fun updateNote(note: NoteEntity) {
        notesDao.update(note)
    }

    fun deleteNote(note: NoteEntity) {
        notesDao.delete(note)
    }

}