package com.shamilovstas.text_encrypt.notes.repository

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.shamilovstas.text_encrypt.notes.domain.Attachment
import java.io.File


@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "filename")
    val filename: String,
    @ColumnInfo(name = "path")
    val path: String,
    @ColumnInfo(name = "note_id")
    val noteId: Long
)

fun AttachmentEntity.toModel(): Attachment {
    return Attachment(
        id = id,
        noteId = noteId,
        uri = Uri.fromFile(File(path)),
        filename = filename,
        isEncrypted = true
    )
}

fun Attachment.toEntity(): AttachmentEntity {
    return AttachmentEntity(
        id = id,
        noteId = noteId,
        path = requireNotNull(uri.path),
        filename = filename,
    )
}