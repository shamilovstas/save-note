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

/*    @Insert(onConflict = REPLACE)
    abstract fun insertNote(entities: List<NoteEntity>)*/

    @Query("SELECT id, content, is_published, created_date, description FROM notes ORDER BY created_date DESC")
    abstract fun getAllNotes(): Flow<List<NoteEntity>>

    @Delete
    abstract fun delete(entity: NoteEntity)

    @Update
    abstract fun update(entity: NoteEntity)

    @Query("SELECT id, content, is_published, created_date, description FROM notes WHERE id=:noteId")
    abstract fun getNoteById(noteId: Int): NoteEntity

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