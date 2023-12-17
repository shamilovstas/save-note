package com.shamilovstas.text_encrypt.notes.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AttachmentStorageDao {

    @Insert
    suspend fun insertAttachmentStorage(attachmentStorageEntity: AttachmentStorageEntity)

    @Query("SELECT * FROM attachment_storage WHERE note_id=:noteId")
    suspend fun findAttachmentStorageForNote(noteId: Long): AttachmentStorageEntity?
}