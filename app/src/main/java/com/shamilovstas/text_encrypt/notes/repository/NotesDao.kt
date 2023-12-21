package com.shamilovstas.text_encrypt.notes.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NotesDao {
    @Insert(onConflict = REPLACE)
    abstract suspend fun insertNote(entity: NoteEntity): Long

    @Insert(onConflict = REPLACE)
    abstract suspend fun insertAttachment(attachmentEntity: AttachmentEntity): Long

    @Insert(onConflict = REPLACE)
    abstract suspend fun insertAttachments(attachments: List<AttachmentEntity>)

    @Query("SELECT * FROM notes ORDER BY created_date DESC")
    abstract fun getAllNotes(): Flow<List<NoteWithAttachments>>

    @Delete
    abstract fun delete(entity: NoteEntity)

//    @Query("SELECT notes.id, content, is_published, created_date, description FROM notes INNER JOIN attachments ON notes.id=attachments.note_id WHERE notes.id=:noteId")
    @Transaction
    @Query("SELECT * FROM notes WHERE notes.id=:noteId")
    abstract suspend fun getNoteById(noteId: Long): NoteWithAttachments

    @Transaction
    open suspend fun insertNote(entity: NoteWithAttachments): NoteWithAttachments {

        val updatedAttachments = mutableListOf<AttachmentEntity>()
        val noteId = insertNote(entity.note)
        entity.attachments.forEach {
            val attachmentWithId = it.copy(noteId = noteId)
            val attachmentId = insertAttachment(attachmentWithId)
            updatedAttachments.add(attachmentWithId.copy(id = attachmentId))
        }
        return NoteWithAttachments(entity.note.copy(id = noteId), updatedAttachments)
    }
}