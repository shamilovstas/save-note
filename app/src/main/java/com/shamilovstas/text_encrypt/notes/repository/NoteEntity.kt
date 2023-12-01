package com.shamilovstas.text_encrypt.notes.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = -1,

    @ColumnInfo(name = "content", typeAffinity = ColumnInfo.TEXT)
    val content: String,

    @ColumnInfo(name = "is_published")
    val isPublished: Boolean,

    @ColumnInfo(name = "created_date")
    val createdDate: OffsetDateTime
)