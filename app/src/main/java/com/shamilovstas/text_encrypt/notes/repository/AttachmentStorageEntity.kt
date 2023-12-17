package com.shamilovstas.text_encrypt.notes.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachment_storage",
    foreignKeys = [
        ForeignKey(NoteEntity::class, parentColumns = ["id"], childColumns = ["note_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class AttachmentStorageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "rel_path")
    val relativePath: String,

    @ColumnInfo(name = "note_id")
    val noteId: Long
)