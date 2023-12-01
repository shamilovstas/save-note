package com.shamilovstas.text_encrypt.notes.domain

import com.shamilovstas.text_encrypt.notes.repository.NoteEntity
import java.time.OffsetDateTime

data class Note(
    val id: Int = 0,
    val content: String,
    val isPublished: Boolean = false,
    val createdDate: OffsetDateTime
)

fun NoteEntity.toModel(): Note {
    return Note(
        id = id,
        content = content,
        isPublished = isPublished,
        createdDate = createdDate
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        isPublished = isPublished,
        createdDate = createdDate
    )
}