package com.shamilovstas.text_encrypt.notes.repository

import androidx.room.Embedded
import androidx.room.Relation
import com.shamilovstas.text_encrypt.notes.domain.Note

data class NoteWithAttachments(
    @Embedded val note: NoteEntity,

    @Relation(parentColumn = "id", entityColumn = "note_id")
    val attachments: List<AttachmentEntity>
)

fun NoteWithAttachments.toModel(): Note {
    return Note(
        id = note.id,
        content = note.content,
        isPublished = note.isPublished,
        createdDate = note.createdDate,
        description = note.description,
        attachments = attachments.map { it.toModel() }
    )
}