package com.shamilovstas.text_encrypt.notes.repository

class NotesRepository(
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