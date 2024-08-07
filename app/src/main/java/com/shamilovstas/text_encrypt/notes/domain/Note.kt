package com.shamilovstas.text_encrypt.notes.domain

import com.shamilovstas.text_encrypt.notes.repository.NoteEntity
import java.time.OffsetDateTime

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val isPublished: Boolean = false,
    val createdDate: OffsetDateTime? = null,
    val description: String = "",
    val attachments: List<Attachment> = listOf()
)

fun NoteEntity.toModel(): Note {
    return Note(
        id = id,
        content = content,
        title = title,
        isPublished = isPublished,
        createdDate = createdDate,
        description = description
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        title = title,
        isPublished = isPublished,
        createdDate = requireNotNull(createdDate),
        description = description
    )
}
