package com.shamilovstas.text_encrypt.notes.compose

import com.shamilovstas.text_encrypt.notes.domain.Attachment
import com.shamilovstas.text_encrypt.notes.domain.Note
import java.time.OffsetDateTime

class MutableNoteViewState(
    var id: Long = 0,
    var content: String,
    var description: String,
    var isPublished: Boolean = false,
    var createdDate: OffsetDateTime? = null,
    var attachments: List<Attachment> = listOf()
)

fun Note.toViewState(): MutableNoteViewState {
    return MutableNoteViewState(
        id = id,
        content = content,
        description = description,
        isPublished = isPublished,
        createdDate = createdDate,
        attachments = attachments.toMutableList()
    )
}

fun MutableNoteViewState.toModel(): Note {
    return Note(
        id = id,
        content = content,
        description = description,
        isPublished = isPublished,
        createdDate = createdDate,
        attachments = attachments
    )
}